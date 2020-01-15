package mx.softel.cirwirelesslib.services

import android.app.Activity
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import mx.softel.cirwirelesslib.constants.*
import mx.softel.cirwirelesslib.enums.*
import mx.softel.cirwirelesslib.extensions.toHex
import mx.softel.cirwirelesslib.utils.CommandUtils
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList


class BleService: Service() {

    // INTERFACES
    private lateinit var activity       : OnBleConnection
    private lateinit var ctx            : Context

    // BLE MANAGER
    private var bleManager              : BluetoothManager?                 = null
    private var bleAdapter              : BluetoothAdapter?                 = null
    private var firmware                : String?                           = null

    // UUID's
    var uuidService             : UUID?                             = null
    var characteristicNotify    : BluetoothGattCharacteristic?      = null
    var characteristicWrite     : BluetoothGattCharacteristic?      = null
    var characteristicDeviceInfo: BluetoothGattCharacteristic?      = null
    var notificationDescriptor  : BluetoothGattDescriptor?          = null

    // GATT
    var bleGatt                 : BluetoothGatt?                    = null
    private var bleGattConnections      : ArrayList<BluetoothGatt>          = ArrayList()
    private var bleGattCallbacks        : ArrayList<BluetoothGattCallback>  = ArrayList()

    // FLAGS - STATES
    private var correctFirmware         : Boolean                           = false
    var currentState            : StateMachine                      = StateMachine.UNKNOWN
    private var isDescriptorOn          : Boolean                           = false

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
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")

        // Iniciamos el servicio
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
        stopBleService()
    }

    /**
     * ## initBleService
     * Inicializa el [bleManager] y lo asocia con su adaptador, para
     * dar por iniciado el servicio
     *
     * @return Estatus del servicio (iniciado o no)
     */
    private fun initBleService(): Boolean {
        Log.d(TAG, "initBleService")

        // Inicializamos el manager y el adaptador BLE
        bleManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bleManager != null) bleAdapter = bleManager!!.adapter
        return (bleAdapter != null)
    }

    /**
     * ## stopBleService
     * Desconecta los dispositivos actualmente conectados.
     * Detiene el servicio.
     */
    fun stopBleService() {
        stopSelf()
    }

    /**
     * ## registerActivity
     * Genera una instancia de la actividad que está asociada a la comunicación
     * y la castea a la interface implementada internamente para la conexión
     * y asociación de métodos entre aplicación y servicio
     *
     * @param act Actividad asociada al servicio
     */
    fun registerActivity(act: Activity) {
        activity = act as OnBleConnection
        ctx = act
    }


    /************************************************************************************************/
    /**     BLUETOOTH                                                                               */
    /************************************************************************************************/

    /**
     * ## connectBleDevice
     * Realiza la conexión con el dispositivo
     *
     * @param device
     */
    fun connectBleDevice(device: BluetoothDevice) {
        Log.d(TAG, "connectBleDevice")

        // Realizamos la conexión utilizando la instancia de la actividad registrada
        bleGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(ctx, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(ctx, false, gattCallback)
        }

        /*
         * Añadimos la conexión y el callback al arreglo de
         * conexiones abiertas con el fin de realizar la
         * desconexión centralizada del dispositivo
         */
        bleGattCallbacks.add(gattCallback)
        bleGattConnections.add(bleGatt!!)
    }


    /**
     * ## disconnetionBleDevice
     * Desconecta el/los dispositivo(s) conectados
     *
     * @param disconnectionReason Código de desconexión para manejo de errores
     */
    fun disconnectBleDevice(disconnectionReason: DisconnectionReason) {
        Log.d(TAG, "disconnectBleDevice -> Reason($disconnectionReason)")

        try {
            // Limpiamos las conexiones creadas
            activity.connectionStatus(ActualState.DISCONNECTING,
                ActualState.DISCONNECTED,
                disconnectionReason)
            cleanBleConnections()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * ## discoverDeviceServices
     * Inicia el escaneo a profundidad de los servicios y características
     * de comunicación con el dispositivo
     */
    fun discoverDeviceServices() {
        Log.d(TAG, "discoverDeviceServices")
        bleGatt?.discoverServices()
    }


    /************************************************************************************************/
    /**     AUXILIARES                                                                              */
    /************************************************************************************************/

    /**
     * ## cleanBleConnections
     * Elimina las conexiones de los dispositivos asociados e
     * inicializa en null las variables asociadas a características,
     * servicios y descriptores
     */
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
            isDescriptorOn              = false

            // Limpiamos el arreglo de las conexiones una vez cerradas
            bleGattConnections.clear()
        }
    }

    /**
     * ## unregisterRunnableTimeoutTask
     * Elimina los callbacks asociados al timeout del servicio
     */
    private fun unregisterRunnableTimeoutTask() {
        Log.d(TAG, "unregisterRunnableTimeoutTask")
        for (runnable in runnableTimeoutTaskList) {
            connectionObserver.removeCallbacks(runnable)
        }
        runnableTimeoutTaskList.clear()
    }

    /**
     * ## disconnectionReasonCode
     * Parsea el Status de la desconexión en un código del Enum [DisconnectionReason]
     *
     * @param status Código entregado por onConnectionStateChanged
     * @return Elemento de [DisconnectionReason]
     */
    private fun disconnectionReasonCode(status: Int): DisconnectionReason
            = when (status) {
        133     -> DisconnectionReason.ERROR_133
        257     -> DisconnectionReason.ERROR_257
        else    -> DisconnectionReason.DISCONNECTION_OCURRED
    }


    /************************************************************************************************/
    /**     CHARACTERISTICS                                                                         */
    /************************************************************************************************/

    /**
     * ## getCommCharacteristics
     * Obtiene las características del servicio
     *
     * @param service Servicio Bluetooth del dispositivo conectado
     */
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

    /**
     * ## getInfoCharacteristics
     * Obtiene las características del servicio e inicializa los datos
     * del [BleConstants.DEVICE_INFO_SERVICE_UUID] para validar el firmware
     * de la tarjeta en cuestión
     *
     * @param service Servicio Bluetooth del dispositivo conectado
     */
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

    /**
     * ## writeToDescriptor
     * Escribe en el descriptor inicializado un arreglo de Bytes que quedan
     * definidos como [ENABLE_NOTIFICATION] y [DISABLE_NOTIFICATION].
     * Nos permite Iniciar o desactivar el descriptor
     * @param command
     */
    fun writeToDescriptor(command: ByteArray) {
        if (notificationDescriptor != null) {
            notificationDescriptor!!.value = command
            bleGatt!!.writeDescriptor(notificationDescriptor)
        }
    }

    /**
     * ## enableCharacteristicNotification
     * Activa o desactiva la notificación en la característica designada
     * por [characteristicNotify], según [enable]
     *
     * @param enable Bander para activar-desactivar la notificación
     */
    private fun enableCharacteristicNotification(enable: Boolean)
            = bleGatt!!.setCharacteristicNotification(characteristicNotify, enable)


    /************************************************************************************************/
    /**     COMMANDS                                                                                */
    /************************************************************************************************/

    /**
     * ## getFirmwareData
     * Realiza una lectura de [characteristicDeviceInfo] para posteriormente
     * obtener el Firmware en [BluetoothGattCallback.onCharacteristicRead]
     */
    fun getFirmwareData() {
        Log.d(TAG, "getFirmwareData")
        if (characteristicDeviceInfo != null) {
            bleGatt!!.readCharacteristic(characteristicDeviceInfo)
        }
    }

    /**
     * ## receivedCommand
     * A partir de la respuesta recibida del dispositivo, parsea la
     * respuesta a un elemento de [ReceivedCmd]
     *
     * @param response Arreglo de bytes de la respuesta del dispositivo
     * @return Elemento [ReceivedCmd] con la descripción de la respuesta
     */
    private fun receivedCommand(response: ByteArray): ReceivedCmd {
        val cmd = when (response[4]) {
            POLEO           -> ReceivedCmd.POLEO
            STATUS          -> ReceivedCmd.STATUS
            REFRESH_AP_OK   -> ReceivedCmd.REFRESH_AP_OK
            GET_AP          -> ReceivedCmd.GET_AP
            AT_OK           -> ReceivedCmd.AT_OK
            AT_NOK          -> ReceivedCmd.AT_NOK
            WAIT_RESPONSE   -> ReceivedCmd.WAIT_AP
            WIFI_SSID_OK    -> ReceivedCmd.WIFI_SSID_OK
            WIFI_SSID_FAIL  -> ReceivedCmd.WIFI_SSID_FAIL
            WIFI_PASS_OK    -> ReceivedCmd.WIFI_PASS_OK
            WIFI_PASS_FAIL  -> ReceivedCmd.WIFI_PASS_FAIL
            WIFI_STATUS     -> ReceivedCmd.WIFI_STATUS
            else            -> ReceivedCmd.UNKNOWN
        }
        Log.d(TAG, "receivedCommand -> $cmd")
        return cmd
    }

    /**
     * ## actualState
     * Castea el status de [Int] a [ActualState]
     *
     * @param status Valor entero del status recibido
     * @return Elemento [ActualState] con la descripción del status
     */
    private fun actualState(status: Int): ActualState {
        return when (status) {
            0       -> ActualState.DISCONNECTED
            1       -> ActualState.CONNECTING
            2       -> ActualState.CONNECTED
            3       -> ActualState.DISCONNECTING
            else    -> ActualState.UNKNOWN
        }
    }

    private fun wifiStatus(response: ByteArray): WifiStatus {
        return when (response[5]) {
            0.toByte() -> WifiStatus.WIFI_CONFIGURING
            1.toByte() -> WifiStatus.WIFI_NOT_CONNECTED
            2.toByte() -> WifiStatus.WIFI_SSID_FAILED
            3.toByte() -> WifiStatus.WIFI_CONNECTING
            4.toByte() -> WifiStatus.WIFI_CONNECTED
            5.toByte() -> WifiStatus.WIFI_IP_FAILED
            6.toByte() -> WifiStatus.WIFI_GET_LOCATION
            7.toByte() -> WifiStatus.WIFI_INTERNET_READY
            else       -> WifiStatus.WIFI_TRANSMITING
        }
    }

    /**
     * ## initPoleCmd
     * Habilita la notificación en el dispositivo para permitir
     * la comunicación por poleo
     */
    fun initPoleCmd() {
        Log.d(TAG, "initPoleCmd")

        enableCharacteristicNotification(true)
        writeToDescriptor(DISABLE_NOTIFICATION)
        currentState = StateMachine.POLING
    }

    /**
     * ## sendRefreshApCmd
     * Ejecuta el comando de actualización de AccesPoints en el dispositivo
     */
    fun sendRefreshApCmd() {
        Log.d(TAG, "sendRefreshApCmd")

        val cmd = CommandUtils.refreshAccessPointsCmd()

        // Cargamos y escribimos en la característica
        val flag = characteristicWrite!!.setValue(cmd)
        if (flag) {
            bleGatt!!.writeCharacteristic(characteristicWrite)
            currentState = StateMachine.REFRESH_AP
        }
    }

    /**
     * ## getMacListCmd
     * Ejecuta el comando para pedir los AccessPoints que el dispositivo almacenó (6)
     */
    fun getMacListCmd() {
        Log.d(TAG, "getMacListCmd")

        val cmd = CommandUtils.getAccessPointsCmd()

        // Cargamos y escribimos en la característica
        val flag = characteristicWrite!!.setValue(cmd)
        if (flag) {
            bleGatt!!.writeCharacteristic(characteristicWrite)
            currentState = StateMachine.GET_AP
        }
    }

    fun sendSsidCmd(ssid: String) {
        Log.d(TAG, "sendSsidCmd")

        val cmd = CommandUtils.setSsidCmd(ssid)

        // Cargamos y escribimos en la característica
        val flag = characteristicWrite!!.setValue(cmd)
        if (flag) {
            bleGatt!!.writeCharacteristic(characteristicWrite)
            currentState = StateMachine.WIFI_CONFIG
        }
    }

    fun sendPasswordCmd(password: String) {
        Log.d(TAG, "sendSsidCmd")

        val cmd = CommandUtils.setPasswordCmd(password)

        // Cargamos y escribimos en la característica
        val flag = characteristicWrite!!.setValue(cmd)
        if (flag) {
            bleGatt!!.writeCharacteristic(characteristicWrite)
            currentState = StateMachine.WIFI_CONFIG
        }
    }

    fun sendStatusWifiCmd() {
        Log.d(TAG, "sendStatusWifiCmd")

        val cmd = CommandUtils.getWifiStatusCmd()

        // Cargamos y escribimos en la característica
        val flag = characteristicWrite!!.setValue(cmd)
        if (flag) {
            bleGatt!!.writeCharacteristic(characteristicWrite)
            currentState = StateMachine.WIFI_CONFIG
        }
    }

    /**
     * ## fromResponseGetMacList
     * A partir de la respuesta obtenida por [getMacListCmd] parsea la respuesta
     * para convertir el arreglo de bytes en una lista de Strings conteniendo las
     * direcciones MAC leídas por el dispositivo
     *
     * @param response Arreglo de Bytes que responde el dispositivo
     * @return Lista de MAC's visibles para el dispositivo
     */
    fun fromResponseGetMacList(response: ByteArray): ArrayList<String>? {
        Log.d(TAG, "fromResponseGetMacList")

        // Validamos que el tamaño de la respuesta sea correcto para parsear los datos
        if (response[3] != SIX_ACCESS_POINTS) {
            Log.e(TAG, "El tamaño de bytes de la respuesta es incompatible")
            return null
        }

        val list = ArrayList<String>()
        val byteElement = ByteArray(6)
        var macString: String

        for (i in 0..5) {
            // Iteramos por cada elemento de las MAC del arreglo
            for (j in 0..5) {
                byteElement[j] = response[(6 * i) + (j + 5)]
            }
            // Casteamos el ByteArray en un String para el array final
            macString = byteElement.toHex().replace(" ", ":")
            Log.d(TAG, "macString: $macString")
            list.add(macString)
        }
        return list
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
                || status == DisconnectionReason.ERROR_257.code
                || status == DisconnectionReason.CONNECTION_FAILED.code) {
                val reason = disconnectionReasonCode(status)
                disconnectBleDevice(reason)
                activity.connectionStatus(actualState(status),
                                          actualState(newState),
                                          reason)
            }

            // Si ya se encuentra conectado...
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e(TAG, "Conectado!!!!!!")
                activity.connectionStatus(actualState(status),
                                          actualState(newState),
                        null)
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
            //super.onCharacteristicWrite(gatt, characteristic, status)
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

                    if (!correctFirmware)
                        disconnectBleDevice(DisconnectionReason.FIRMWARE_UNSOPPORTED)

                    initPoleCmd()
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?,
                                             characteristic: BluetoothGattCharacteristic?) {
            //super.onCharacteristicChanged(gatt, characteristic)
            if (characteristic == null) return
            Log.e(TAG, "RESPUESTA -> ${characteristic.value.toHex()}")

            if (currentState == StateMachine.WIFI_STATUS) {
                activity.wifiStatus(currentState,
                                    characteristic.value,
                                    wifiStatus(characteristic.value))
            } else {
                activity.commandState(currentState,
                                      characteristic.value,
                                      receivedCommand(characteristic.value))
            }


            // STATE MACHINE ACCESS POINTS *********************************************************
            /*when (currentState) {
                StateMachine.POLING             -> sendRefreshApCmd()
                StateMachine.REFRESH_AP_OK      -> refreshingWifi(characteristic)
                StateMachine.WIFI_CONFIG        -> refreshingWifi(characteristic)
                StateMachine.AT_WAIT_RESPONSE   -> refreshingWifi(characteristic)
                StateMachine.STANDBY            -> Log.d(TAG, "STANDBY (Poling) ${characteristic.value.toHex()}")
                else                            -> Log.d(TAG, "Status desconocido")
            }*/
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
            if (!isDescriptorOn) {
                Log.e(TAG, "HABILITANDO DESCRIPTOR")
                writeToDescriptor(ENABLE_NOTIFICATION)
                isDescriptorOn = true
            }
        }

    }



    /************************************************************************************************/
    /**     BINDER                                                                                  */
    /************************************************************************************************/
    override fun onBind(intent: Intent?): IBinder? = binder

    private val binder : IBinder = LocalBinder()

    class LocalBinder : Binder() {
        fun getService() : BleService = BleService()
    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    interface OnBleConnection {
        fun connectionStatus(status: ActualState,
                             newState: ActualState,
                             disconnectionReason: DisconnectionReason?)
        fun commandState(state: StateMachine,
                         response: ByteArray,
                         command: ReceivedCmd)
        fun wifiStatus(state: StateMachine,
                       response: ByteArray,
                       wifiStatus: WifiStatus)
        //fun sendCommand(data: ByteArray)
        //fun onBleResponse(data: ByteArray)
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = BleService::class.java.simpleName

        // Discriminantes de respuestas
        private const val WIFI_SSID_OK          = 0x22.toByte()
        private const val WIFI_SSID_FAIL        = 0x23.toByte()
        private const val WIFI_PASS_OK          = 0x25.toByte()
        private const val WIFI_PASS_FAIL        = 0x26.toByte()
        private const val WIFI_STATUS           = 0x28.toByte()
        private const val WAIT_RESPONSE         = 0x36.toByte()
        private const val REFRESH_AP_OK         = 0x48.toByte()
        private const val GET_AP                = 0x4A.toByte()
        private const val AT_OK                 = 0x4C.toByte()
        private const val AT_NOK                = 0x4D.toByte()
        private const val STATUS                = 0xC1.toByte()
        private const val POLEO                 = 0xC5.toByte()

        // Validaciones
        private const val SIX_ACCESS_POINTS     = 0x2B.toByte()

        // Constantes útiles
        const val MAX_ALLOWED_CONNECTIONS             = 8
        private val DISABLE_NOTIFICATION    = byteArrayOf(0x00)
        private val ENABLE_NOTIFICATION     = byteArrayOf(0x01)

    }
}