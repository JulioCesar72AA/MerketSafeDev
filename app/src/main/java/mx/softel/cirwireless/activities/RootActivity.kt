package mx.softel.cirwireless.activities

import android.bluetooth.BluetoothDevice
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.dialogs.PasswordDialog
import mx.softel.cirwireless.extensions.toast
import mx.softel.cirwireless.fragments.AccessPointsFragment
import mx.softel.cirwireless.fragments.MainFragment
import mx.softel.cirwireless.interfaces.FragmentNavigation
import mx.softel.cirwirelesslib.constants.*
import mx.softel.cirwirelesslib.services.BleService


class RootActivity : AppCompatActivity(),
    FragmentNavigation,
    PasswordDialog.OnDialogClickListener,
    BleService.OnBleConnection {

    internal lateinit var bleDevice : BluetoothDevice
    internal lateinit var bleMac    : String

    // SERVICE CONNECTIONS / FLAGS
    internal var service             : BleService?       = null
    private var isServiceConnected                      = false

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        getAndSetIntentData()
        initFragment()
    }

    override fun onResume() {
        super.onResume()

        // Iniciamos el Broadcast Receiver
        doBindService()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Quitamos el Broadcast Receiver
        doUnbindService()
    }

    private fun getAndSetIntentData() {
        Log.d(TAG, "getIntent")

        // Obtenemos la información del intent
        val data = intent.extras!!
        bleDevice        = data[EXTRA_DEVICE] as BluetoothDevice
        bleMac           = data.getString(EXTRA_MAC)!!
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
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun dialogAccept(password: String) {
        toast("Configurando el dispositivo")
        val accessPointFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer)
                as AccessPointsFragment
        accessPointFragment.setScanningUI()
        //startBleService()                   // Iniciamos el servicio de Bluetooth
    }

    override fun dialogCancel() {
        toast("Cancelado")
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

    // BLE SERVICE INTERFACES
    override fun connectionStatus(status: Int) {
        Log.e(TAG, "connectionStatus -> $status")
    }

   /* override fun sendCommand(data: ByteArray) {

    }

    override fun onBleResponse(data: ByteArray) {

    }*/


    /************************************************************************************************/
    /**     BLE SERVICE CONNECTION                                                                  */
    /************************************************************************************************/
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, xservice: IBinder?) {
            // Obtenemos la instancia del servicio una vez conectado
            Log.d(TAG, "onServiceConnected")
            service = (xservice as (BleService.LocalBinder)).getService()
            service!!.apply {
                registerActivity(this@RootActivity)
                connectBleDevice(bleDevice)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
            service!!.stopBleService()
        }
    }

    private fun doBindService() {
        Log.d(TAG, "doBindService")
        // Conecta la aplicación con el servicio
        bindService(Intent(this, BleService::class.java),
            connection, Context.BIND_AUTO_CREATE)
        isServiceConnected = true
    }

    private fun doUnbindService() {
        Log.d(TAG, "doUnbindService")
        if (isServiceConnected) {
            // Termina la conexión existente con el servicio
            service!!.stopBleService()
            unbindService(connection)
            isServiceConnected = false
        }
    }



    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = RootActivity::class.java.simpleName
    }
}
