package mx.softel.cirwireless

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import mx.softel.scanblelib.ble.BleManager

class MainActivity : AppCompatActivity() {

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // SCAN
        val bleManager = BleManager(this, 10_000L)
        bleManager.scanBleDevices {
            for (dev in it) {
                Log.d(TAG, "Tengo la lista de dispositivos: $it")
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
