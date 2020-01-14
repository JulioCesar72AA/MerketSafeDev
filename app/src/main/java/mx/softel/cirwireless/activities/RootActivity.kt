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
import mx.softel.cirwirelesslib.enums.ReceivedCmd
import mx.softel.cirwirelesslib.enums.StateMachine
import mx.softel.cirwirelesslib.extensions.toHex
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

    // HANDLERS / RUNNABLES
    private val handler = Handler()
    private val runnable = Runnable {
        finishActivity(disconnectionReason)
    }

    // FLAGS / EXTRA VARIABLES
    private var disconnectionReason                         = DisconnectionReason.UNKNOWN
    internal var deviceMacList      : ArrayList<String>?    = null


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
     * Desconecta los dispositivos asociados, elimina los callbacks
     * y termina la actividad, para volver a la lista de dispositivos escaneados
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

    // BLE SERVICE INTERFACES **********************************************************************
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
     * ## commandState
     * Recibe la respuesta del dispositivo y su estado actual en la
     * máquina de estados, para poder dar un flujo adecuado
     *
     * @param state Estado actual (Máquina de estados)
     * @param response Respuesta en bytes del dispositivo
     * @param command Tipo de respuesta recibida
     */
    override fun commandState(state: StateMachine, response: ByteArray, command: ReceivedCmd) {

        Log.e(TAG, "STATE -> $state, RESPONSE -> ${response.toHex()}, COMMAND -> $command")
        when (state) {
            StateMachine.REFRESH_AP -> {
                // Si se recibe el comando REFRESH_AP_OK
                if (command == ReceivedCmd.REFRESH_AP_OK) {
                    // Iniciamos el fragmento de AccessPointsFragment
                    val fragment = AccessPointsFragment.getInstance()
                    navigateTo(fragment, true, null)
                }
                // Si no es REFRESH_AP_OK, simplemente ignoramos el resultado hasta cambiar de estado
            }
            StateMachine.GET_AP -> {
                if (command == ReceivedCmd.GET_AP) {
                    // Casteamos el resultado en una lista de Strings
                    deviceMacList = service!!.fromResponseGetMacList(response)
                }
            }
            else -> Log.e(TAG, "STATE -> $state, RESPONSE -> ${response.toHex()}, COMMAND -> $command")
        }
    }
    // *********************************************************************************************


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
                    postDelayed(runnable, UI_TIMEOUT)
                }
            }
            DisconnectionReason.DISCONNECTION_OCURRED, DisconnectionReason.CONNECTION_FAILED -> {
                runOnUiThread { toast("No se puede conectar con el dispositivo") }
                handler.apply {
                    postDelayed(runnable, UI_TIMEOUT)
                }
            }
            DisconnectionReason.FIRMWARE_UNSOPPORTED -> {
                runOnUiThread { toast("Dispositivo no soportado") }
                handler.apply {
                    postDelayed(runnable, UI_TIMEOUT)
                }
            }
            else -> toast("Desconectando el dispositivo")
        }
    }

    /**
     * ## connectedDevice
     * Notifica en pantalla que el dispositivo fue correctamente conectado.
     * Manda a escanear los servicios que el dispositivo contiene, e inicializa
     * algunas características necesarias para la comunicación
     */
    private fun connectedDevice() {
        Log.d(TAG, "connectedDevice")
        runOnUiThread { toast("Dispositivo conectado") }
        service!!.discoverDeviceServices()
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

        // Timeouts de la actividad
        private const val UI_TIMEOUT = 500L
    }
}
