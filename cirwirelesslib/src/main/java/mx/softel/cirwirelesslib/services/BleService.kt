package mx.softel.cirwirelesslib.services

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import mx.softel.cirwirelesslib.constants.BleConstants
import mx.softel.cirwirelesslib.constants.Constants
import mx.softel.cirwirelesslib.extensions.toHex
import mx.softel.cirwirelesslib.utils.CommandUtils
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class BleService: Service() {

    // BLE MANAGER
    private var bleManager              : BluetoothManager?                 = null
    private var bleAdapter              : BluetoothAdapter?                 = null
    private var firmware                : String?                           = null
    private var bleDevice               : BluetoothDevice?                  = null
    private var accessPointList         : ArrayList<ByteArray>?             = null

    // UUID's
    private var uuidService             : UUID?                             = null
    private var characteristicNotify    : BluetoothGattCharacteristic?      = null
    private var characteristicWrite     : BluetoothGattCharacteristic?      = null
    private var characteristicDeviceInfo: BluetoothGattCharacteristic?      = null
    private var notificationDescriptor  : BluetoothGattDescriptor?          = null

    // GATT
    private var bleGatt                 : BluetoothGatt?                    = null
    private var bleGattConnections      : ArrayList<BluetoothGatt>          = ArrayList()
    private var bleGattCallbacks        : ArrayList<BluetoothGattCallback>  = ArrayList()

    // FLAGS - STATES
    private var descriptorEnabled       : Boolean                           = false
    //private var stopTimer               : Boolean                           = false
    private var correctFirmware         : Boolean                           = false
    private var isNotifying             : Boolean                           = false
    private var waitResponse            : Boolean                           = false
    private var currentState            : StateMachine                      = StateMachine.UNKNOWN

    // HANDLERS
    private var connectionObserver      : Handler                           = Handler()
    private var runnableTimeoutTaskList : ArrayList<Runnable>               = ArrayList()
    /*private var runnableTimeoutTask     : Runnable                          = object : Runnable {
        override fun run() {
            disconnectBleDevice(DisconnectionReason.TIME_OUT_DISCONNECTION)
            if (!stopTimer) connectionObserver.postDelayed(this, 1_000)
        }
    }*/


    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        bleDevice = intent?.extras?.get(Constants.EXTRA_DEVICE) as BluetoothDevice
        if (bleDevice != null) connectBleDevice(bleDevice!!)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        if (initBleService()) {
            Log.d(TAG, "Se inició correctamente el servicio BLE")
        } else {
            Log.w(TAG, "Ocurrió un problema al iniciar el servicio BLE")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        unregisterRunnableTimeoutTask()
        disconnectBleDevice(DisconnectionReason.NORMAL_DISCONNECTION)
    }


    private fun initBleService(): Boolean {
        Log.d(TAG, "initBleService")

        // Inicializamos el manager y el adaptador BLE
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bleManager != null) bleAdapter = bleManager!!.adapter
        return (bleAdapter != null)
    }


    /************************************************************************************************/
    /**     BLUETOOTH                                                                               */
    /************************************************************************************************/
    private fun connectBleDevice(device: BluetoothDevice) {
        Log.d(TAG, "connectBleDevice")

        descriptorEnabled   = false

        bleGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(applicationContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(applicationContext, false, gattCallback)
        }

        /*
         * Añadimos la conexión y el callback al arreglo de
         * conexiones abiertas con el fin de realizar la
         * desconexión centralizada del dispositivo
         */
        bleGattCallbacks.add(gattCallback)
        bleGattConnections.add(bleGatt!!)
    }


    fun disconnectBleDevice(disconnectionReason: DisconnectionReason) {
        Log.d(TAG, "disconnectBleDevice -> Reason($disconnectionReason)")

        try {
            // Limpiamos las conexiones creadas
            cleanBleConnections()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    /************************************************************************************************/
    /**     AUXILIARES                                                                              */
    /************************************************************************************************/
    private fun cleanBleConnections() {
        Log.d(TAG, "cleanBleConnections")
        if (!bleGattConnections.isNullOrEmpty()) {
            for (gatt in bleGattConnections) {
                // Cerramos las conexiones una a una
                gatt.apply {
                    disconnect()
                    close()
                }
            }
            uuidService                 = null
            characteristicNotify        = null
            characteristicWrite         = null
            characteristicDeviceInfo    = null


            // Limpiamos el arreglo de las conexiones una vez cerradas
            bleGattConnections.clear()
        }
    }


    private fun unregisterRunnableTimeoutTask() {
        Log.d(TAG, "unregisterRunnableTimeoutTask")
        for (runnable in runnableTimeoutTaskList) {
            connectionObserver.removeCallbacks(runnable)
        }
        runnableTimeoutTaskList.clear()
    }

    private fun disconnectionReasonCode(status: Int): DisconnectionReason
            = when (status) {
        133     -> DisconnectionReason.ERROR_133
        257     -> DisconnectionReason.ERROR_257
        else    -> DisconnectionReason.DISCONNECTION_OCURRED
    }


    /************************************************************************************************/
    /**     CHARACTERISTICS                                                                         */
    /************************************************************************************************/
    private fun getCommCharacteristics(service: BluetoothGattService) {
        Log.d(TAG, "getCommCharacteristics")
        val uuid = service.uuid.toString()

        if (uuid == BleConstants.CIR_NAMA_SERVICE_UUID) {
            uuidService = service.uuid
            Log.e(TAG, "Sevicio de comunicación encontrado $uuidService")

            // Solicitamos e inicializamos las características de COMUNICACIÓN
            val characteristics = service.characteristics
            for (char in characteristics) {
                when (char.uuid.toString()) {
                    BleConstants.CIR_NAMA_NOTIFY_UUID -> {
                        Log.e(TAG, "NOTIFICACIÓN ${char.uuid}")
                        characteristicNotify = char

                        // Obtenemos los descriptores de la notificación
                        for (desc in char.descriptors) {
                            Log.e(TAG, "DESCRIPTOR: ${desc.uuid}")
                            if (desc.uuid.toString() == BleConstants.NOTIFICATION_DESCRIPTOR) {
                                notificationDescriptor = desc
                            }
                        }

                    }
                    BleConstants.CIR_NAMA_WRITE_UUID -> {
                        Log.e(TAG, "ESCRITURA ${char.uuid}")
                        characteristicWrite = char
                    }
                }
            }
        }
    }

    private fun getInfoCharacteristics(service: BluetoothGattService) {
        Log.d(TAG, "getInfoCharacteristics")
        val uuid = service.uuid.toString()

        if (uuid == BleConstants.DEVICE_INFO_SERVICE_UUID) {
            // Solicitamos e inicializamos las características de INFORMACIÓN
            val characteristics = service.characteristics
            for (char in characteristics) {
                if (char.uuid.toString() == BleConstants.DEVICE_INFO_UUID) {
                    Log.e(TAG, "DEVICE_INFO ${char.uuid}")
                    characteristicDeviceInfo = char
                }
            }
        }
    }

    private fun writeToDescriptor(command: ByteArray) {
        if (notificationDescriptor != null) {
            notificationDescriptor!!.value = command
            bleGatt!!.writeDescriptor(notificationDescriptor)
        }
    }


    private fun enableCharacteristicNotification(enable: Boolean)
            = bleGatt!!.setCharacteristicNotification(characteristicNotify, enable)


    /************************************************************************************************/
    /**     COMMANDS                                                                                */
    /************************************************************************************************/
    private fun getFirmwareData() {
        Log.d(TAG, "getFirmwareData")
        if (characteristicDeviceInfo != null) {
            bleGatt!!.readCharacteristic(characteristicDeviceInfo)
        }
    }

    private fun receivedCommand(response: ByteArray): ReceivedCmd {
        Log.d(TAG, "receivedCommand")
        return when (response[4]) {
            POLEO           -> ReceivedCmd.POLEO
            STATUS          -> ReceivedCmd.STATUS
            REFRESH_AP_OK   -> ReceivedCmd.REFRESH_AP
            GET_AP_MACS     -> ReceivedCmd.GET_AP
            else            -> ReceivedCmd.UNKNOWN
        }
    }

    private fun initPoleCmd() {
        Log.d(TAG, "initPoleCmd")

        enableCharacteristicNotification(true)
        writeToDescriptor(DISABLE_NOTIFICATION)
        isNotifying  = false
        currentState = StateMachine.POLING
    }

    private fun sendRefreshApCmd() {
        Log.d(TAG, "sendRefreshApCmd")

        if (!waitResponse) {
            val cmd = CommandUtils.refreshAccessPointsCmd()
            Log.e(TAG, "CMD ACCESS POINTS: ${cmd.toHex()}")

            // Cargamos y escribimos en la característica
            val flag = characteristicWrite!!.setValue(cmd)
            Log.d(TAG, flag.toString())
            if (flag) {
                bleGatt!!.writeCharacteristic(characteristicWrite)
                waitResponse = true
                currentState = StateMachine.REFRESHING_AP
            }
        }
    }

    private fun getApDataCmd(characteristic: BluetoothGattCharacteristic) {
        Log.e(TAG, "getApDataCmd -> ${characteristic.value.toHex()}")
        waitResponse = false
        val response = receivedCommand(characteristic.value)

        if (!waitResponse && response == ReceivedCmd.POLEO) {
            val cmd = CommandUtils.getAccessPointsCmd()
            Log.e(TAG, "CMD ACCESS POINTS: ${cmd.toHex()}")

            // Cargamos y escribimos en la característica
            val flag = characteristicWrite!!.setValue(cmd)
            Log.d(TAG, flag.toString())
            if (flag) {
                bleGatt!!.writeCharacteristic(characteristicWrite)
                waitResponse = true
                currentState = StateMachine.GET_AP
            }
        }
    }

    private fun getApList(characteristic: BluetoothGattCharacteristic): ArrayList<ByteArray>? {
        Log.d(TAG , "getApList -> ${characteristic.value.toHex()}")
        waitResponse = false
        val response = receivedCommand(characteristic.value)
        val apArray = arrayListOf<ByteArray>()

        // Si la respuesta es GET_AP y el tamaño del paquete es 2B...
        if (response == ReceivedCmd.GET_AP && characteristic.value[3] == 0x2b.toByte()) {
            //val mac = ByteArray(6)

            // TODO: Ejecutar comandoas AT de WIFI para configurar y leer los AP's

            currentState = StateMachine.STANDBY
        }

        return if (apArray.isEmpty()) null else apArray
    }



    /************************************************************************************************/
    /**     CALLBACKS                                                                               */
    /************************************************************************************************/
    private val gattCallback = object : BluetoothGattCallback()  {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i(TAG, "onConnectionStateChange -> Status($status) -> NewState($newState)")

            // Si ocurre el error 133 o el error 257...
            if (status == DisconnectionReason.ERROR_133.code
                || status == DisconnectionReason.ERROR_257.code) {
                disconnectBleDevice(disconnectionReasonCode(status))
            }

            // Si ya se encuentra conectado...
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e(TAG, "Conectado!!!!!!")
                bleGatt!!.discoverServices()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i(TAG, "onMTuChanged")

            // Solicitando la versión de Firmware de la tarjeta
            getFirmwareData()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i(TAG, "onServicesDiscovered -> Status($status)")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Analizando los servicios encontrados
                val services = gatt?.services!!
                for (service in services.iterator()) {
                    getCommCharacteristics(service)
                    getInfoCharacteristics(service)
                }

                // Actualizamos el MTU de la comunicación
                bleGatt!!.requestMtu(300)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?,
                                           characteristic: BluetoothGattCharacteristic?,
                                           status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i(TAG, "onCharacteristicWrite -> ${characteristic!!.value.toHex()}")
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?,
                                          characteristic: BluetoothGattCharacteristic?,
                                          status: Int) {
            Log.i(TAG, "onCharacteristicRead -> ${characteristic?.uuid}")

            // Si se leyó correctamente la característica...
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // Lectura de DEVICE_INFO_UUID
                if (characteristic!!.uuid.toString() == BleConstants.DEVICE_INFO_UUID) {
                    val one = characteristic.value[1]
                    val two = characteristic.value[2]
                    val three = characteristic.value[3]
                    firmware = "$one.$two.$three"

                    correctFirmware = (firmware == BleConstants.FIRMWARE_346)
                    Log.e(TAG, "FIRMWARE $firmware -> CORRECT $correctFirmware")
                    if (correctFirmware) initPoleCmd()
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?,
                                             characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic == null) return
            val uuid = characteristic.uuid

            // STATE MACHINE ACCESS POINTS *********************************************************
            when (currentState) {
                StateMachine.POLING         -> sendRefreshApCmd()
                StateMachine.REFRESHING_AP  -> getApDataCmd(characteristic)
                StateMachine.GET_AP         -> getApList(characteristic)
                StateMachine.STANDBY        -> Log.d(TAG, "STANDBY (Poling)")
                else                        -> Log.d(TAG, "Status desconocido")
            }
            // *************************************************************************************

        }

        override fun onDescriptorRead(gatt: BluetoothGatt?,
                                      descriptor: BluetoothGattDescriptor?,
                                      status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
            Log.i(TAG, "onDescriptorRead")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?,
                                       descriptor: BluetoothGattDescriptor?,
                                       status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.i(TAG, "onDescriptorWrite")

            // Si aún no está poleando, se levanta el descriptor
            if (!isNotifying) {
                Log.e(TAG, "HABILITANDO DESCRIPTOR")
                writeToDescriptor(ENABLE_NOTIFICATION)
                isNotifying     = true
            }
        }

    }



    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    override fun onBind(intent: Intent?): IBinder? = null


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = BleService::class.java.simpleName

        // Discriminantes de respuestas
        private const val POLEO         = 0xC5.toByte()
        private const val STATUS        = 0xC1.toByte()
        private const val REFRESH_AP_OK = 0x48.toByte()
        private const val GET_AP_MACS   = 0x4A.toByte()

        // Validaciones
        private const val SIX_ACCESS_POINTS = 0x2B.toByte()

        // Constantes útiles
        private const val MAX_ALLOWED_CONNECTIONS = 8
        private val DISABLE_NOTIFICATION= byteArrayOf(0x00)
        private val ENABLE_NOTIFICATION = byteArrayOf(0x01)
    }
}