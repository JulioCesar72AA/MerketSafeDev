package mx.softel.scanblelib.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.util.Log
import mx.softel.scanblelib.BLE_MANAGER_DEBUG_MODE
import mx.softel.scanblelib.extensions.isLocationPermissionGranted
import mx.softel.scanblelib.extensions.requestLocationPermission

class BleManager(var appContext: Context,
                 var scanningTime: Long) {

    val bleAdapter  : BluetoothAdapter
    val bleScanner  : BluetoothLeScanner

    // AUXILIARES
    val bleDevices = ArrayList <BleDevice>()
    val filterBeaconList = ArrayList<String>()

    /************************************************************************************************/
    /**     CONSTRUCTORES                                                                           */
    /************************************************************************************************/
    init {
        val manager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = manager.adapter
        bleScanner = bleAdapter.bluetoothLeScanner

        // TODO: Agragar BeaconsDatabase
        // TODO: Agregar AvailableBeacons
    }


    /************************************************************************************************/
    /**     METODOS                                                                                 */
    /************************************************************************************************/
    fun isImberaDevice(address: String): Boolean = (address.startsWith(PREFIX_BLE_1)
                                                 || address.startsWith(PREFIX_BLE_2)
                                                 || address.startsWith(PREFIX_WIFI))

    fun newDeviceScanned(mac: String): Boolean {
        for (device in bleDevices) {
            val scannedMac = device.bleMacAddress
            val exists = (scannedMac == mac)
            if (exists) return false    // Ya existe ese dispositivo en la lista de escaneo
        }
        return true
    }


    fun scanBleDevices(onSuccessScan: (ArrayList<BleDevice>) -> Unit) {
        bleDevices.clear()

        if (appContext.isLocationPermissionGranted()) {
            bleScanner.startScan(scanCallback)
            Handler().postDelayed({
                bleScanner.stopScan(scanCallback)
                onSuccessScan(bleDevices)
            }, scanningTime)
        } else {
            appContext.requestLocationPermission()
        }
    }




    /************************************************************************************************/
    /**     CALLBACKS                                                                               */
    /************************************************************************************************/
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val mac = result?.device!!.address

            // --------------------------------------------------
            if (BLE_MANAGER_DEBUG_MODE) {
                Log.d(TAG, "onScanResult: $mac | isImberaDevice: ${isImberaDevice(mac)}")
            }
            // --------------------------------------------------

            if (isImberaDevice(mac) && newDeviceScanned(mac)) {
                val scannedBleDevice = BleDevice(result,/* TODO: Cambiar HashMap*/ HashMap())
                if (filterBeaconList.contains(scannedBleDevice.deviceBeaconType)) {
                    bleDevices.add(scannedBleDevice)
                }
            }
        }
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = BleManager::class.java.simpleName

        // Prefijos para la validación de direcciones MAC
        private const val PREFIX_BLE_1 = "B4:A2:EB:4"
        private const val PREFIX_BLE_2 = "00:1B:C5"
        private const val PREFIX_WIFI  = "C4:4F:33"
    }

}