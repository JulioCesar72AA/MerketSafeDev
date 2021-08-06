package mx.softel.scanblelib.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.util.Log
import mx.softel.scanblelib.extensions.toHexValue
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BleDevice(scanResult: ScanResult,
                private var availableBeacons: HashMap<String, String>) {

    // BLE ESCANEADOS (PROPIEDADES PUBLICAS AL FRONT END)
    private val rssi            : Int               = scanResult.rssi
    private val name            : String?           = scanResult.device.name
    private val bleDevice       : BluetoothDevice   = scanResult.device
    private val beaconDevice    : ScanRecord?       = scanResult.scanRecord
    private val bleMacAddress   : String            = scanResult.device.address
    private lateinit var modelName : String

    // FORMATO DEL BEACON
    private val BLE_DEVICE_NOT_ENCRYPTED    = 0     // El dispositivo contiene información sin encriptar
    private val BLE_DEVICE_ENCRYPTED        = 1     // El dispositivo contiene información encriptada

    // AUXILIARES
    private  var beaconDeviceString         = ""    // Beacon transformado en cadena
    internal var deviceBeaconType           = ""    // Es la versión de beacon perteneciente al firmware
    private  var deviceBeaconIsEncrypted    = ""    // Nos indica si el beacon está o no encriptado

    // FLAGS
    private var isEncrypted                 = 0


    /************************************************************************************************/
    /**     CONSTRUCTORES                                                                           */
    /************************************************************************************************/
    init {
        modelName = getCirModelName(getBeaconId(scanResult.scanRecord!!.bytes))
        analizeBeacon()
    }

    /************************************************************************************************/
    /**     GETTERS                                                                                 */
    /************************************************************************************************/
    fun getBeaconDeviceString()     : String            = beaconDeviceString
    fun getDeviceBeaconIsEncrypted(): String            = deviceBeaconIsEncrypted
    fun getBeaconType()             : String            = deviceBeaconType
    fun isEncrypted()               : Boolean           = isEncrypted != 0
    fun getRssi()                   : Int               = rssi
    fun getMac()                    : String            = bleMacAddress
    fun getName()                   : String            = name ?: "NULL"
    fun getBleDevice()              : BluetoothDevice   = bleDevice
    fun getScanRecord()             : ScanRecord?       = beaconDevice
    fun getDeviceModelName()        : String            = modelName



    /************************************************************************************************/
    /**     METODOS                                                                                 */
    /************************************************************************************************/
    private fun analizeBeacon() {
        try {
            isEncrypted = BLE_DEVICE_NOT_ENCRYPTED

            // Obtenemos el Beacon del dispositivo
            val beaconValues = ArrayList<String>()
            val beaconDeviceBytes = beaconDevice!!.bytes

            // Guardamos una versión legible en hexadecimal (String)
            beaconDeviceString = beaconDeviceBytes!!.toHexValue()

            // Obtenemos el identificador del beacon, para identificar si es nuevo o viejo
            val subOld = beaconDeviceString.substring(
                BEGIN_OFFSET_FOR_OLD_BEACON,
                END_OFFSET_FOR_OLD_BEACON
            )
            val subNew = beaconDeviceString.substring(
                BEGIN_OFFSET_FOR_NEW_BEACON,
                END_OFFSET_FOR_NEW_BEACON
            )
            val oldBeaconType = (PREFIX_FOR_BLE_BEACONS + subOld)
            val newBeaconType = (PREFIX_FOR_BLE_BEACONS + subNew)

            // Identificamos el tipo de Beacon del dispositivo
            val auxList = when {
                availableBeacons.contains(oldBeaconType) -> availableBeacons[oldBeaconType]!!.split("\\|")
                availableBeacons.contains(newBeaconType) -> availableBeacons[newBeaconType]!!.split("\\|")
                else -> emptyList()
            }

            for (data in auxList) { beaconValues.add(data) }

            if (beaconValues.isNotEmpty()) {
                isEncrypted         = Integer.valueOf(beaconValues[0])
                deviceBeaconType    = beaconValues[1]
            }

            // Asignamos el valor de la información si está o no encriptado
            deviceBeaconIsEncrypted =
                if (isEncrypted == BLE_DEVICE_ENCRYPTED) BEACON_IS_ENCRYPTED
                else BEACON_IS_NOT_ENCRYPTED

        } catch (ex: Exception) {
            ex.stackTrace
        }
    }


    private fun getBeaconId (beacon: ByteArray): String =
        "0x${byteArrayOf(beacon[5], beacon[6]).toHexValue().toUpperCase(Locale.ROOT)}"


    private fun getCirModelName (beaconId: String) : String = when (beaconId) {
        "0x000B" -> "CIR Wireless"
        "0x000C" -> "CIR Wireless"
        "0x000D" -> "CIR RS232"
        "0x000E" -> "CIR RS232"
        else -> "NO AVAILABLE"
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = BleDevice::class.java.simpleName

        // BEACON CONSTANTS
        private const val BEGIN_OFFSET_FOR_OLD_BEACON = 14
        private const val END_OFFSET_FOR_OLD_BEACON   = 18
        private const val BEGIN_OFFSET_FOR_NEW_BEACON = 10
        private const val END_OFFSET_FOR_NEW_BEACON   = 14
        private const val PREFIX_FOR_BLE_BEACONS      = "0x"
        private const val BEACON_IS_ENCRYPTED         = "0x8005"
        private const val BEACON_IS_NOT_ENCRYPTED     = "5"
    }

}