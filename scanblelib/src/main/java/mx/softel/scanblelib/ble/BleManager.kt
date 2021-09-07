package mx.softel.scanblelib.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.util.Log
import mx.softel.scanblelib.extensions.isLocationPermissionGranted
import mx.softel.scanblelib.extensions.requestLocationPermission
import mx.softel.scanblelib.extensions.toHexValue
import mx.softel.scanblelib.sqlite.BeaconsDatabaseHelper
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BleManager(private var appContext: Context,
                 private var scanningTime: Long = 10_000L,
                 private var filterBeaconList: ArrayList<String> = arrayListOf() ) {

    private val bleAdapter          : BluetoothAdapter
    private val bleScanner          : BluetoothLeScanner
    private val availableBeacons    : HashMap<String, String>

    // AUXILIARES
    val bleDevices                  = ArrayList <BleDevice>()
    private val beaconsDB           = BeaconsDatabaseHelper(appContext, null)


    /************************************************************************************************/
    /**     CONSTRUCTORES                                                                           */
    /************************************************************************************************/
    init {
        val manager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter          = manager.adapter
        bleScanner          = bleAdapter.bluetoothLeScanner
        availableBeacons    = beaconsDB.getBeaconTypes()
    }


    /************************************************************************************************/
    /**     METODOS                                                                                 */
    /************************************************************************************************/
    fun isImberaDevice(address: String): Boolean = (address.startsWith(PREFIX_BLE_1)
                                                 || address.startsWith(PREFIX_BLE_2)
                                                 || address.startsWith(PREFIX_WIFI))

    fun isAValidBeacon (beaconId: String) : Boolean = when (beaconId) {
        "0x000B" -> true
        "0x000C" -> true
        "0x000D" -> true
        "0x000E" -> true
        "0x0013" -> true
        "0x0014" -> true
        else -> false
    }



    fun newDeviceScanned(mac: String): Boolean {
        for (device in bleDevices) {
            val scannedMac = device.getMac()
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
            val mac         = result?.device!!.address
            val beacon      = result?.scanRecord.bytes
            val beaconId    = "0x${byteArrayOf(beacon[5], beacon[6]).toHexValue().toUpperCase(Locale.ROOT)}"
            val check       = isImberaDevice(mac) && newDeviceScanned(mac) && isAValidBeacon(beaconId)

            if (check) {
                val scannedBleDevice = BleDevice(result, availableBeacons)
                if (filterBeaconList.isEmpty()) { // NO BEACON FILTER
                    bleDevices.add(scannedBleDevice)
                } else {
                    val type = filterBeaconList.contains(scannedBleDevice.deviceBeaconType)
                    if (type) {
                        bleDevices.add(scannedBleDevice)
                    }
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