package mx.softel.cirwireless.activities

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_root.*
import mx.softel.cirwireless.R
import mx.softel.cirwireless.fragments.MainFragment
import mx.softel.cirwireless.interfaces.FragmentNavigation
import mx.softel.cirwirelesslib.constants.Constants

class RootActivity : AppCompatActivity(), FragmentNavigation {

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setSupportActionBar(rootToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        getAndSetIntentData()
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
        val bleDevice                = data[Constants.EXTRA_DEVICE] as BluetoothDevice
        val name           = data.getString(Constants.EXTRA_NAME)!!
        val mac            = data.getString(Constants.EXTRA_MAC)!!
        val beacon         = data.getString(Constants.EXTRA_BEACON)!!
        val type           = data.getString(Constants.EXTRA_BEACON_TYPE)!!
        val beacEncrypted  = data.getString(Constants.EXTRA_BEACON_ENCRYPTED)!!
        val isEncrypted  = data.getBoolean(Constants.EXTRA_IS_ENCRYPTED)

        // Mandamos la información necesaria al RootFragment
        val args = Bundle()
        args.apply {
            putString(Constants.EXTRA_DEVICE, name)
            putString(Constants.EXTRA_MAC, mac)
            putString(Constants.EXTRA_BEACON, beacon)
            putString(Constants.EXTRA_BEACON_TYPE, type)
            putString(Constants.EXTRA_BEACON_ENCRYPTED, beacEncrypted)
            putBoolean(Constants.EXTRA_IS_ENCRYPTED, isEncrypted)
            putParcelable(Constants.EXTRA_DEVICE, bleDevice)
        }

        // Lo añadimos al fragmento e iniciamos la vista
        val fragment = MainFragment.getInstance()
        fragment.arguments = args
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
                            animIn: Int,
                            animOut: Int) {
        Log.d(TAG, "navigateTo")

        val transaction = supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(animIn, animOut)
            .replace(R.id.fragmentContainer, fragment)

        if (addToBackStack) transaction.addToBackStack(null)

        transaction.commit()
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = RootActivity::class.java.simpleName
    }
}
