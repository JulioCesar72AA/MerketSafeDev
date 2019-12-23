package mx.softel.cirwireless

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.ble.BleManager

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var bleDevices = ArrayList<BleDevice>()

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Log.d(TAG, "onCreate")
        setOnClick()
    }



    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.fab -> {
                scanDevices()
                for(dev in bleDevices) {
                    Log.d(TAG, "Almacenado en MainActivity: ${dev.bleDevice}")
                }
            }
        }
    }

    private fun setOnClick() {
        fab.setOnClickListener(this)
    }



    /************************************************************************************************/
    /**     AUXILIARES                                                                              */
    /************************************************************************************************/
    private fun scanDevices() {
        Log.d(TAG, "scanDevices")
        val bleManager = BleManager(this)
        bleManager.scanBleDevices {
            Log.d(TAG, "scanBleDevices")
            bleDevices = it
            for (dev in it) {
                Log.d(TAG, "Dispositivo: ${dev.bleDevice}")
                // Log.d(TAG, "Beacon: ${dev.beaconDevice}")
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
