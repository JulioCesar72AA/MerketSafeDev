package mx.softel.cirwireless.activities

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import mx.softel.cirwireless.R
import mx.softel.cirwirelesslib.extensions.hexStringToByteArray
import mx.softel.cirwirelesslib.extensions.toHex
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.cirwirelesslib.utils.CommandUtils

class SplashActivity : AppCompatActivity() {

    private val adapter = BluetoothAdapter.getDefaultAdapter()

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        if (adapter.isEnabled) {
            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 2000)

        } else {
            Log.d(TAG, "Esperando a que el usuario encienda el Bluetooth")
            enableBle()
        }

    }


    /************************************************************************************************/
    /**     BLUETOOTH                                                                               */
    /************************************************************************************************/
    private fun enableBle() {
        if (!adapter.isEnabled) {
            val turnOn = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnOn, 0)
        }
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = SplashActivity::class.java.simpleName
    }
}
