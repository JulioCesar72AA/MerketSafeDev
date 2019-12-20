package mx.softel.scanblelib

import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log

class BleDevice(var ctx: Context,
                var scanResult: ScanResult,
                var availableBeacons: HashMap<String, String>) {

    // FORMATO DEL BEACON
    private val BLE_DEVICE_NOT_ENCRYPTED    = 0     // El dispositivo contiene información sin encriptar
    private val BLE_DEVICE_ENCRYPTED        = 1     // El dispositivo contiene información encriptada

    // BEACON
    private val BEGIN_OFFSET_FOR_OLD_BEACON = 14
    private val END_OFFSET_FOR_OLD_BEACON   = 18
    private val BEGIN_OFFSET_FOR_NEW_BEACON = 10
    private val END_OFFSET_FOR_NEW_BEACON   = 14
    private val PREFIXFOR_BLE_BEACONS       = "0x"
    private val BEACON_IS_ENCRYPTED         = "0x8005"
    private val BEACON_IS_NOT_ENCRYPTED     = "5"

    // FLAGS
    private var isEncrypted                 = 0




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
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }




    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = BleDevice::class.java.simpleName
    }

}