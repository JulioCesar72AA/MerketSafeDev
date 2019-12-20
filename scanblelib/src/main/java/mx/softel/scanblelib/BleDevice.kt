package mx.softel.scanblelib

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import mx.softel.scanblelib.extensions.toHexValue

class BleDevice(var ctx: Context,
                scanResult: ScanResult,
                var availableBeacons: HashMap<String, String>) {

    // FORMATO DEL BEACON
    private val BLE_DEVICE_NOT_ENCRYPTED    = 0     // El dispositivo contiene información sin encriptar
    private val BLE_DEVICE_ENCRYPTED        = 1     // El dispositivo contiene información encriptada

    // AUXILIARES
    private var beaconDeviceString          = ""    // Beacon transformado en cadena
    private var deviceBeaconType            = ""    // Es la versión de beacon perteneciente al firmware
    private var deviceBeaconIsEncrypted     = ""    // Nos indica si el beacon está o no encriptado

    // FLAGS
    private var isEncrypted                 = 0

    // BLE ESCANEADOS
    private val bleDevice       : BluetoothDevice   = scanResult.device
    private val beaconDevice    : ScanRecord?       = scanResult.scanRecord
    private val bleMacAddress   : String            = scanResult.device.address



    /************************************************************************************************/
    /**     CONSTRUCTORES                                                                           */
    /************************************************************************************************/
    init {
        analizeBeacon()
    }


    /************************************************************************************************/
    /**     METODOS                                                                                 */
    /************************************************************************************************/
    private fun analizeBeacon() {
        Log.d(TAG, "analizeBeacon")
        try {
            isEncrypted = BLE_DEVICE_NOT_ENCRYPTED

            // Obtenemos el Beacon del dispositivo
            val beaconValues = ArrayList<String>()
            val beaconDeviceBytes = beaconDevice!!.bytes

            // Guardamos una versión legible en hexadecimal (String)
            beaconDeviceString = beaconDeviceBytes!!.toHexValue()

            // Obtenemos el identificador del beacon, para identificar si es nuevo o viejo
            val subOld = beaconDeviceString.substring(BEGIN_OFFSET_FOR_OLD_BEACON,
                                                             END_OFFSET_FOR_OLD_BEACON)
            val subNew = beaconDeviceString.substring(BEGIN_OFFSET_FOR_NEW_BEACON,
                                                             END_OFFSET_FOR_NEW_BEACON)
            val oldBeaconType = PREFIX_FOR_BLE_BEACONS + subOld
            val newBeaconType = PREFIX_FOR_BLE_BEACONS + subNew

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


            // --------------------------------------------------
            if (DEBUG_MODE) {
                Log.d(TAG, "OLD BEACON: $oldBeaconType")
                Log.d(TAG, "NEW BEACON: $newBeaconType")
                for (data in beaconValues) { Log.d(TAG, "BEACON VALUES: $data") }
                Log.d(TAG, "BEACON ENCRYPTED: $deviceBeaconIsEncrypted")
            }
            // --------------------------------------------------

        } catch (ex: Exception) {
            ex.stackTrace
        }
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