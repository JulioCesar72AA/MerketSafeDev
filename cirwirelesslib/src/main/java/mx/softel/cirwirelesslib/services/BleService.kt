package mx.softel.cirwirelesslib.services

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import mx.softel.cirwirelesslib.constants.Constants
import java.lang.Exception
import kotlin.concurrent.thread

class BleService: Service() {

    // BLE MANAGER
    private var bleManager              : BluetoothManager?                 = null
    private var bleAdapter              : BluetoothAdapter?                 = null
    var bleDevice               : BluetoothDevice?                  = null

    // GATT
    private var bleGatt                 : BluetoothGatt?                    = null
    private var bleGattConnections      : ArrayList<BluetoothGatt>          = ArrayList()
    private var bleGattCallbacks        : ArrayList<BluetoothGattCallback>  = ArrayList()

    // FLAGS - STATES
    private var descriptorEnabled       : Boolean                           = false
    private var stopTimer               : Boolean                           = false
    private var currentState            : ActualState                       = ActualState.DISCONNECTED

    // HANDLERS
    private var connectionObserver      : Handler                           = Handler()
    private var runnableTimeoutTaskList : ArrayList<Runnable>               = ArrayList()
    private var runnableTimeoutTask     : Runnable                          = object : Runnable {
        override fun run() {
            disconnectBleDevice(DisconnectionReason.TIME_OUT_DISCONNECTION)
            if (!stopTimer) connectionObserver.postDelayed(this, 1_000)
        }
    }


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
        currentState        = ActualState.CONNECTING

        bleGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(applicationContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(applicationContext, false, gattCallback)
        }

        /*
        Añadimos la conexión y el callback al arreglo de
        conexiones abiertas con el fin de realizar la
        desconexión centralizada del dispositivo
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
            // TODO: Volver a null las características encontradas


            // Limpiamos el arreglo de las conexiones una vez cerradas
            bleGattConnections.clear()
        }
    }


    private fun unregisterRunnableTimeoutTask() {
        Log.d(TAG, "unregisterRunnableTimeoutTask")
        for (runnable in runnableTimeoutTaskList) {
            connectionObserver!!.removeCallbacks(runnable)
        }
        runnableTimeoutTaskList.clear()
    }

    private fun disconnectionReasonCode(status: Int): DisconnectionReason
            = when (status) {
        133     -> DisconnectionReason.ERROR_133
        257     -> DisconnectionReason.ERROR_257
        else    -> DisconnectionReason.DISCONNECTION_OCURRED
    }

    private fun actualStateCode(status: Int): ActualState
            = when (status) {
        0       -> ActualState.DISCONNECTED
        1       -> ActualState.CONNECTING
        2       -> ActualState.CONNECTED
        3       -> ActualState.DISCONNECTING
        else    -> ActualState.UNKNOWN
    }



    /************************************************************************************************/
    /**     CALLBACKS                                                                               */
    /************************************************************************************************/
    private val gattCallback = object : BluetoothGattCallback()  {

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.i(TAG, "onConnectionStateChange -> Status($status) -> NewState -> $newState)")

            if (status == DisconnectionReason.ERROR_133.code
                || status == DisconnectionReason.ERROR_257.code) {
                //disconnectBleDevice(disconnectionReasonCode(status))
            }

            //bleGatt!!.connect()
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Conectado!!!!!!")
                bleGatt!!.discoverServices()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            Log.i(TAG, "onMTuChanged")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.i(TAG, "onServicesDiscovered")
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?,
                                           characteristic: BluetoothGattCharacteristic?,
                                           status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.i(TAG, "onCharacteristicWrite")
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?,
                                          characteristic: BluetoothGattCharacteristic?,
                                          status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.i(TAG, "onCharacteristicRead")
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?,
                                             characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            Log.i(TAG, "onCharacteristicChanged")
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

        private const val MAX_ALLOWED_CONNECTIONS = 8


    }
}