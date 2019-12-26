package mx.softel.cirwireless

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import mx.softel.scanblelib.adapters.BleDeviceRecyclerAdapter
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.ble.BleManager

class MainActivity : AppCompatActivity(),
    View.OnClickListener,
    BleDeviceRecyclerAdapter.OnScanClickListener {

    private var bleDevices = ArrayList<BleDevice>()
    private var isScanning = false

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Log.d(TAG, "onCreate")
        initUI()
        setOnClick()
    }



    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab -> {
                if (!isScanning) scanDevices()
                else {
                    Toast.makeText(this, "Espera un momento... Escaneando", Toast.LENGTH_SHORT).apply {
                        show()
                    }
                }
            }
        }
    }

    private fun setOnClick() {
        fab.setOnClickListener(this)
    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    override fun onScanClickListener(position: Int) {
        Log.d(TAG, "onScanClickListener")
        Log.d(TAG, "NAME: ${bleDevices[position].name}")
        Log.d(TAG, "BLE DEVICE: ${bleDevices[position].bleMacAddress}")
        Log.d(TAG, "RSSI: ${bleDevices[position].rssi}")
    }


    /************************************************************************************************/
    /**     VISTA                                                                                   */
    /************************************************************************************************/
    private fun initUI() {
        Log.d(TAG, "initUI")

        pbScanning.visibility = View.GONE
        scanMask.visibility = View.GONE
    }

    private fun setScanningUI() {
        Log.d(TAG, "setScanningUI")

        pbScanning.visibility = View.VISIBLE
        scanMask.visibility = View.VISIBLE
    }

    private fun setRecyclerUI() {
        Log.d(TAG, "setRecyclerUI")

        rvBleList.apply {
            val manager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            layoutManager = manager
            adapter = BleDeviceRecyclerAdapter(bleDevices, this@MainActivity)
        }

        initUI()
    }


    /************************************************************************************************/
    /**     AUXILIARES                                                                              */
    /************************************************************************************************/
    private fun scanDevices() {
        Log.d(TAG, "scanDevices")

        isScanning = true
        setScanningUI()
        val bleManager = BleManager(this)
        bleManager.scanBleDevices {
            Log.d(TAG, "scanBleDevices")
            bleDevices = it
            for (dev in it) {
                Log.d(TAG, "Dispositivo: ${dev.bleDevice}")
                // Log.d(TAG, "Beacon: ${dev.beaconDevice}")
                setRecyclerUI()
                isScanning = false
            }
        }
    }



    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }


}
