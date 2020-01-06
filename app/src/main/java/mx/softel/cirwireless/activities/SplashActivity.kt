package mx.softel.cirwireless.activities

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import mx.softel.cirwireless.R

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
        Log.d(TAG, "onResume")
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
        Log.d(TAG, "enableBle")

        if (adapter.isEnabled) {
            Log.i(TAG, "El bluetooth ya está encendido")
        } else {
            Log.i(TAG, "Encendiendo el bluetooth")
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
