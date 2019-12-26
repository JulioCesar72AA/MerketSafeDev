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

class RootActivity : AppCompatActivity(), FragmentNavigation {

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setSupportActionBar(rootToolbar)
        getAndSetIntentData()
    }

    private fun getAndSetIntentData() {
        Log.d(TAG, "getIntent")

        // Obtenemos la información del intent
        val data = intent.extras!!
        val device  = data[MainActivity.EXTRA_DEVICE] as BluetoothDevice
        val name            = data.getString(MainActivity.EXTRA_NAME)!!
        val mac             = data.getString(MainActivity.EXTRA_MAC)!!
        val beacon          = data.getString(MainActivity.EXTRA_BEACON)!!
        val type            = data.getString(MainActivity.EXTRA_BEACON_TYPE)!!
        val beacEncrypted   = data.getString(MainActivity.EXTRA_BEACON_ENCRYPTED)!!
        val isEncrypted   = data.getBoolean(MainActivity.EXTRA_IS_ENCRYPTED)

        // Mandamos la información necesaria al RootFragment
        val args = Bundle()
        args.apply {
            putString(MainActivity.EXTRA_DEVICE, name)
            putString(MainActivity.EXTRA_MAC, mac)
            putString(MainActivity.EXTRA_BEACON, beacon)
            putString(MainActivity.EXTRA_BEACON_TYPE, type)
            putString(MainActivity.EXTRA_BEACON_ENCRYPTED, beacEncrypted)
            putBoolean(MainActivity.EXTRA_IS_ENCRYPTED, isEncrypted)
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
