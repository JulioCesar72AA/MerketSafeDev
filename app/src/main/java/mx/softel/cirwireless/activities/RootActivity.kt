package mx.softel.cirwireless.activities

import android.bluetooth.BluetoothDevice
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
import mx.softel.cirwirelesslib.enums.ActualState
import mx.softel.cirwirelesslib.enums.DisconnectionReason
import mx.softel.cirwirelesslib.services.BleService


class RootActivity : AppCompatActivity(),
    FragmentNavigation,
    PasswordDialog.OnDialogClickListener,
    BleService.OnBleConnection {

    // BLUETOOTH DEVICE
    internal lateinit var bleDevice : BluetoothDevice
    internal lateinit var bleMac    : String

    // SERVICE CONNECTIONS / FLAGS
    internal var service             : BleService?      = null
    private  var isServiceConnected                     = false

    private val handler = Handler()

    private var disconnectionReason = DisconnectionReason.UNKNOWN
    private val runnable = Runnable {
        finishActivity(disconnectionReason)
    }

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

        // Asociamos el servicio de BLE
        doBindService()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Desasociamos el servicio de BLE
        doUnbindService()
    }

    /**
     * ## getAndSetIntentData
     * Obtiene los elementos recibidos en el intent, para posteriormente
     * mandarlo al fragment que muestre los datos, así como el objeto
     * del dispositivo con el cual se va a interactuar
     */
    private fun getAndSetIntentData() {
        Log.d(TAG, "getIntent")

        // Obtenemos la información del intent
        val data = intent.extras!!
        bleDevice        = data[EXTRA_DEVICE] as BluetoothDevice
        bleMac           = data.getString(EXTRA_MAC)!!
    }

    /**
     * ## initFragment
     * Inicializa el fragmento [MainFragment] para el manejo de
     * la instancia principal de conexión
     */
    private fun initFragment() {
        Log.d(TAG, "initFragment")
        // Iniciamos el fragmento deseado
        val fragment = MainFragment.getInstance()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * ## finishActivity
     * Corrige un Bug de back button en fragmento
     */
    internal fun finishActivity(disconnectionReason: DisconnectionReason) {
        service!!.disconnectBleDevice(disconnectionReason)
        handler.removeCallbacks(runnable)
        finish()
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/

    /**
     * ## dialogAccept
     * Se ejecuta al hacer click en "Aceptar" del diálogo de Password
     *
     * @param password Texto de password ingresado por el usuario
     */
    override fun dialogAccept(password: String) {
        toast("Configurando el dispositivo")
        val accessPointFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainer)
                as AccessPointsFragment
        accessPointFragment.setScanningUI()
    }

    /**
     * ## dialogCancel
     * Se ejecuta al hacer click en "Cancelar" del diálogo de Password
     */
    override fun dialogCancel() {
        toast("Cancelado")
    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/

    /**
     * ## navigateTo
     * Interface que ayuda con la navegación entre fragmentos contenidos en
     * esta actividad
     *
     * @param fragment Fragmento a ser iniciado
     * @param addToBackStack Bandera para añadirlo al BackStack
     * @param args Argumentos (si es requerido) para iniciar el fragmento
     * @param animIn Animación de entrada (fadeIn por default)
     * @param animOut Animación de salida (fadeOut por default)
     */
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
    /**
     * ## connectionStatus
     * Se ejecuta cada que el servicio BLE cambia de estatus de conexión
     *
     * @param status Estatus de conexión
     */
    override fun connectionStatus(status: ActualState,
                                  newState: ActualState,
                                  disconnectionReason: DisconnectionReason?) {
        Log.e(TAG, "connectionStatus -> $status : $newState")

        // newState solo puede ser CONNECTED o DISCONNECTED
        when (newState) {
            ActualState.CONNECTED       -> connectedDevice()
            else                        -> errorConnection(disconnectionReason!!)
        }
    }

    /**
     * ## errorConnection
     * Muestra un mensaje con una descripción genérica de conexión cuando
     * no se puede conectar al dispositivo.
     * Ejecuta desconexión y regresa a la pantalla de selección [MainActivity]
     *
     * @param reason
     */
    private fun errorConnection(reason: DisconnectionReason) {
        Log.d(TAG, "errorConnection")
        disconnectionReason = reason
        when (reason) {
            DisconnectionReason.ERROR_133, DisconnectionReason.ERROR_257 -> {
                runOnUiThread { toast("Ocurrió un error") }
                handler.apply {
                    postDelayed(runnable, 1000)
                }
            }
            DisconnectionReason.DISCONNECTION_OCURRED, DisconnectionReason.CONNECTION_FAILED -> {
                runOnUiThread { toast("No se puede conectar con el dispositivo") }
                handler.apply {
                    postDelayed(runnable, 1000)
                }
            }
            else -> toast("Desconectando el dispositivo")
        }
    }

    private fun connectedDevice() {
        Log.d(TAG, "connectedDevice")
        runOnUiThread { toast("Dispositivo conectado") }
    }


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
