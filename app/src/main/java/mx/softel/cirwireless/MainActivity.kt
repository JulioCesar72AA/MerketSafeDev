package mx.softel.cirwireless

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import mx.softel.cirwirelesslib.BleUtils

class MainActivity : AppCompatActivity() {

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }



    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }


}
