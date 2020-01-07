package mx.softel.cirwireless.activities

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_root.*
import mx.softel.cirwireless.R
import mx.softel.cirwireless.fragments.MainFragment
import mx.softel.cirwireless.interfaces.FragmentNavigation
import mx.softel.cirwirelesslib.constants.Constants
import mx.softel.cirwirelesslib.services.BleService

class RootActivity : AppCompatActivity(), FragmentNavigation {

    internal lateinit var bleDevice : BluetoothDevice
    internal lateinit var bleMac    : String

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        getAndSetIntentData()
        initFragment()
    }

    /**
     * Interface que ejecuta la actividad cuando el usuario hace
     * click en el ícono de "volver" <- del [getSupportActionBar]
     */
    override fun onSupportNavigateUp(): Boolean {
        // Quitamos el fragmento que esté en la parte superior del backstack
        val pop = supportFragmentManager.popBackStackImmediate()

        // Si no hay nada que hacer POP, entonces termina la actividad
        if (!pop) finish()
        return true
    }


    private fun getAndSetIntentData() {
        Log.d(TAG, "getIntent")

        // Obtenemos la información del intent
        val data = intent.extras!!
        bleDevice        = data[Constants.EXTRA_DEVICE] as BluetoothDevice
        bleMac           = data.getString(Constants.EXTRA_MAC)!!
        /*val name           = data.getString(Constants.EXTRA_NAME)!!
        val beacon         = data.getString(Constants.EXTRA_BEACON)!!
        val type           = data.getString(Constants.EXTRA_BEACON_TYPE)!!
        val beacEncrypted  = data.getString(Constants.EXTRA_BEACON_ENCRYPTED)!!
        val isEncrypted    = data.getBoolean(Constants.EXTRA_IS_ENCRYPTED)*/
    }


    private fun initFragment() {
        Log.d(TAG, "initFragment")
        // Iniciamos el fragmento deseado
        val fragment = MainFragment.getInstance()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .commit()
    }



    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    override fun navigateTo(fragment: Fragment,
                            addToBackStack: Boolean,
                            args: Bundle?,
                            animIn: Int,
                            animOut: Int) {
        Log.d(TAG, "navigateTo")

        if (args != null) {
            fragment.arguments = args
        }

        val transaction = supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(animIn, animOut)
            .replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) transaction.addToBackStack(null)

        transaction.commit()
    }

    /************************************************************************************************/
    /**     SERVICES                                                                                */
    /************************************************************************************************/
    internal fun startBleService() {

        val intent = Intent(this, BleService::class.java)
        intent.apply {
            putExtra(Constants.EXTRA_DEVICE, bleDevice)
        }
        startService(intent)
    }

    internal fun stopBleService()
            = stopService(Intent(this, BleService::class.java))


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = RootActivity::class.java.simpleName
    }
}
