package mx.softel.cirwireless.activities

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.scanning_mask.*
import mx.softel.cirwireless.R
import mx.softel.cirwireless.dialogs.PasswordDialog
import mx.softel.cirwireless.dialogs.WifiNokDialog
import mx.softel.cirwireless.dialogs.WifiOkDialog
import mx.softel.cirwireless.extensions.toast
import mx.softel.cirwireless.fragments.AccessPointsFragment
import mx.softel.cirwireless.fragments.MainFragment
import mx.softel.cirwireless.fragments.TesterFragment
import mx.softel.cirwireless.interfaces.FragmentNavigation
import mx.softel.cirwirelesslib.constants.*
import mx.softel.cirwirelesslib.enums.*
import mx.softel.cirwirelesslib.extensions.toCharString
import mx.softel.cirwirelesslib.extensions.toHex
import mx.softel.cirwirelesslib.services.BleService


class RootActivity : AppCompatActivity(),
    FragmentNavigation,
    PasswordDialog.OnDialogClickListener,
    WifiOkDialog.OnWifiDialogListener,
    BleService.OnBleConnection {

    // BLUETOOTH DEVICE
    internal lateinit var bleDevice         : BluetoothDevice
    internal lateinit var bleMac            : String
    internal lateinit var ssidSelected      : String
    private  lateinit var passwordTyped     : String

    // VALORES PARA FRAGMENT DE TESTER
    internal var ipAssigned         : String            = ""
    internal var apAssigned         : Boolean           = false
    internal var ssidAssigned       : String            = ""
    internal var rssiAssigned       : String            = ""
    internal var pingAssigned       : Boolean           = false
    internal var dataAssigned       : Boolean           = false
    internal var statusAssigned     : Boolean           = false

    // SERVICE CONNECTIONS / FLAGS
    internal var service                    : BleService?           = null
    private  var isServiceConnected         : Boolean               = false
    private  var isRefreshed                : Boolean               = false

    // FLAGS / EXTRA VARIABLES
    private  var disconnectionReason        : DisconnectionReason   = DisconnectionReason.UNKNOWN
    internal var deviceMacList              : ArrayList<String>?    = null
    internal var actualFragment             : Fragment?             = null
    internal val mainFragment               : MainFragment          = MainFragment.getInstance()
    internal val testerFragment             : TesterFragment        = TesterFragment.getInstance()
    internal val wifiFragment               : AccessPointsFragment  = AccessPointsFragment.getInstance()

    // HANDLERS / RUNNABLES
    private val handler                 = Handler()
    private val disconnectionRunnable   = Runnable { finishActivity(disconnectionReason) }


    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setScanningUI()
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

    override fun onBackPressed() {
        backFragment()
        //super.onBackPressed()
    }

    internal fun setScanningUI() {
        scanMask.apply {
            visibility = View.VISIBLE
            background = getDrawable(R.color.hardMask)
            setOnClickListener { toast("Espere un momento") }
        }
        pbScanning.visibility = View.VISIBLE
    }

    internal fun setStandardUI() {
        scanMask.visibility = View.GONE
        pbScanning.visibility = View.GONE
        serviceStep = 0
    }

    /**
     * ## getAndSetIntentData
     * Obtiene los elementos recibidos en el intent, para posteriormente
     * mandarlo al fragment que muestre los datos, así como el objeto
     * del dispositivo con el cual se va a interactuar
     */
    private fun getAndSetIntentData() {
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
        // Iniciamos el fragmento deseado
        val fragment = mainFragment
        actualFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .commit()
    }

    internal fun backFragment() {
        if (actualFragment == mainFragment) {
            finishActivity(DisconnectionReason.NORMAL_DISCONNECTION)
            return
        }
        if (actualFragment == testerFragment) actualFragment = mainFragment
        if (actualFragment == wifiFragment) actualFragment = mainFragment
        supportFragmentManager.popBackStackImmediate()
    }

    /**
     * ## finishActivity
     * Desconecta los dispositivos asociados, elimina los callbacks
     * y termina la actividad, para volver a la lista de dispositivos escaneados
     */
    internal fun finishActivity(disconnectionReason: DisconnectionReason) {
        service!!.disconnectBleDevice(disconnectionReason)
        handler.removeCallbacks(disconnectionRunnable)
        actualFragment = null
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

        toast("Configurando el WIFI del dispositivo", Toast.LENGTH_LONG)
        passwordTyped = password
        service!!.apply {
            sendConfigureWifiCmd(ssidSelected, passwordTyped)
            currentState = StateMachine.WIFI_CONFIG
        }
        setScanningUI()
    }

    /**
     * ## dialogCancel
     * Se ejecuta al hacer click en "Cancelar" del diálogo de Password
     */
    override fun dialogCancel() {
        toast("Cancelado")
    }

    override fun dialogOk() {
        backFragment()
    }





    /************************************************************************************************/
    /**     CONTROL RESULTS                                                                         */
    /************************************************************************************************/
    private fun wifiConfigProcess(response: ByteArray, command: ReceivedCmd) {
        Log.d(TAG, "wifiConfigProcess -> $command, RESPONSE -> ${response.toCharString()}, RESPONSE2 -> ${response.toHex()}")

        when (command) {
            ReceivedCmd.AT_OK -> {
                Log.d(TAG, "AT Correctamente leído, esperando respuesta")
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.POLEO -> {
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.AT_READY -> {
                if (response.toCharString().contains("WIFI GOT IP")) {
                    service!!.currentState = StateMachine.POLING
                    Log.e(TAG, "Configuración correcta")
                    runOnUiThread { setStandardUI() }
                    val dialog = WifiOkDialog.getInstance()
                    dialog.show(supportFragmentManager, null)
                    return
                }
                if (response.toCharString().contains(AT_CMD_ERROR)) {
                    service!!.currentState = StateMachine.POLING
                    Log.e(TAG, "Ocurrió un error con el Wi-Fi")
                    runOnUiThread { setStandardUI() }
                    val dialog = WifiNokDialog.getInstance()
                    dialog.apply {
                        show(supportFragmentManager, null)
                    }
                    return
                }
                service!!.readAtResponseCmd()
            }
            else -> {  }
        }
    }


    private fun getIpFromAt(response: ByteArray, command: ReceivedCmd) {
        Log.d(TAG, "geIpFromAt -> $command, RESPONSE -> ${response.toCharString()}, RESPONSE2 -> ${response.toHex()}")

        when (command) {
            ReceivedCmd.AT_OK -> {
                Log.d(TAG, "AT Correctamente leído, esperando respuesta")
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.WAIT_AP -> {
                service!!.sendIpAtCmd()
            }
            ReceivedCmd.POLEO -> {
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.AT_READY -> {
                parseIpResponse(response.toCharString())
                service!!.currentState = StateMachine.GET_STATUS_AP
            }
            else -> {  }
        }
    }

    private fun getApStatusFromAT(response: ByteArray, command: ReceivedCmd) {
        Log.d(TAG, "getApStatusFromAT -> $command, RESPONSE -> ${response.toCharString()}, RESPONSE2 -> ${response.toHex()}")

        when (command) {
            ReceivedCmd.AT_OK -> {
                Log.d(TAG, "AT Correctamente leído, esperando respuesta")
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.WAIT_AP -> {
                service!!.sendApConnectionCmd()
            }
            ReceivedCmd.POLEO -> {
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.AT_READY -> {
                Log.d(TAG, "MENSAJE RECIBIDO CORRECTAMENTE: ${response.toCharString()}")
                parseApResponse(response.toCharString())
                service!!.currentState = StateMachine.PING
            }
            else -> {  }
        }
    }

    private fun getPing(response: ByteArray, command: ReceivedCmd) {
        Log.d(TAG, "getPing -> $command, RESPONSE -> ${response.toCharString()}")

        when (command) {
            ReceivedCmd.AT_OK -> {
                Log.d(TAG, "AT Correctamente leído, esperando respuesta")
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.WAIT_AP -> {
                service!!.sendPing("www.google.com")
            }
            ReceivedCmd.POLEO -> {
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.AT_READY -> {
                Log.d(TAG, "MENSAJE RECIBIDO CORRECTAMENTE: ${response.toCharString()}")
                parsePingResponse(response.toCharString())
                service!!.currentState = StateMachine.DATA_CONNECTION
            }
            else -> {  }
        }
    }

    private fun getDataConnection(response: ByteArray, command: ReceivedCmd, step: Int) {
        Log.d(TAG, "getDataConnection -> $command, RESPONSE -> ${response.toCharString()}, STEP: $step, RESPONSE2 -> ${response.toHex()}")

        when (command) {
            ReceivedCmd.AT_OK -> {
                Log.d(TAG, "AT Correctamente leído, esperando respuesta")
                service!!.readAtResponseCmd()
            }
            ReceivedCmd.WAIT_AP -> {
                when (step) {
                    0 -> service!!.closeAtSocketCmd()
                    3 -> service!!.sendStatusWifiCmd()
                }
            }
            ReceivedCmd.POLEO -> {
                when (step) {
                    3 -> service!!.sendStatusWifiCmd()
                    else -> service!!.readAtResponseCmd()
                }
            }
            ReceivedCmd.AT_READY -> {
                Log.d(TAG, "MENSAJE RECIBIDO CORRECTAMENTE: ${response.toCharString()}")
                parseDataResponse(response, step)
            }
            else -> {  }
        }
    }


    private fun parseIpResponse(response: String) {
        if (response.contains(WIFI_NOT_IP_STRING)){
            ipAssigned = "IP No asignada"
            apAssigned = false
        } else {
            var ipRead = response.substringAfter(WIFI_SUBSTRING_IP_AFTER)
            ipRead = ipRead
                .substringBefore(WIFI_SUBSTRING_IP_BEFORE)
                .replace("\"", "")
                .replace("\n", "")
                .replace("\r", "")
            ipAssigned = ipRead
            apAssigned = true
        }
        runOnUiThread { testerFragment.fragmentUiUpdate(1) }
    }

    private fun parseApResponse(response: String) {
        if (response.contains(WIFI_SUBSTRING_AP_AFTER)) {
            ssidAssigned = response
                .substringAfter(WIFI_SUBSTRING_AP_AFTER)
                .substringBefore(",")
                .replace("\"", "")
                .replace("\n", "")
                .replace("\r", "")
            rssiAssigned = response
                .substringAfterLast(",")
                .substringBefore("OK")
                .replace("\r", "")
                .replace("\n", "")
            Log.e(TAG, "SSID: $ssidAssigned, RSSI: $rssiAssigned")
        } else {
            ssidAssigned = "No conectado"
            rssiAssigned = "No conectado"
        }
        runOnUiThread { testerFragment.fragmentUiUpdate(2) }
    }

    private fun parsePingResponse(response: String) {
        pingAssigned = (response.contains(PING_OK) && !response.contains(AT_CMD_ERROR))
        runOnUiThread { testerFragment.fragmentUiUpdate(3) }
    }

    private fun parseDataResponse(response: ByteArray, step: Int) {
        Log.d(TAG, "parseDataResponse $step")
        val restring = response.toCharString()
        when (step) {
            0 -> {
                service!!.openAtSocketCmd("foodservices.otus.com.mx", "8030")
                serviceStep = 1
            }
            1 -> {
                if (restring.contains(AT_CMD_CONNECT)) {
                    service!!.closeAtSocketCmd()
                    dataAssigned = true
                } else if (restring.contains(AT_CMD_CLOSED) || restring.contains(AT_CMD_ERROR)) {
                    Log.d(TAG, "El socket está cerrado/error")
                    dataAssigned = false
                }
                serviceStep = 2
                //service!!.currentState = StateMachine.POLING
                runOnUiThread {
                    testerFragment.fragmentUiUpdate(4)
                }
            }
            2 -> {
                if (restring.contains(AT_CMD_CLOSED) || restring.contains(AT_CMD_ERROR)) {
                    Log.d(TAG, "Cerrando el socket")
                    service!!.currentState = StateMachine.POLING
                    serviceStep = 0
                    runOnUiThread {
                        setStandardUI()
                    }
                } else {
                    service!!.closeAtSocketCmd()
                }
            }
        }
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

        when (state) {

            // STATUS DE POLEO
            StateMachine.POLING -> {
                // Actualizamos los access points recién se conecta al dispositivo
                // TODO: No es necesario actualizar al dispositivo recién se conecta
                if (!isRefreshed) service!!.sendRefreshApCmd()
                if (command == ReceivedCmd.REFRESH_AP_OK) {
                    isRefreshed = true
                    runOnUiThread { setStandardUI() }
                }
            }

            // STATUS DE MAC'S DE AP'S QUE EL DISPOSITIVO VE
            StateMachine.GET_AP -> {
                if (command == ReceivedCmd.GET_AP) {
                    // Casteamos el resultado y navegamos al fragmento de AP's
                    deviceMacList = service!!.fromResponseGetMacList(response)
                    if (actualFragment != wifiFragment) navigateTo(wifiFragment, true, null)
                    else wifiFragment.scanWifi()
                    actualFragment = wifiFragment
                }
            }

            // STATUS DE CONFIGURACIÓN WIFI
            StateMachine.WIFI_CONFIG        -> { wifiConfigProcess(response, command) }

            // TESTING CONNECTION MACHINE *************************************************************
            StateMachine.GET_IP             -> { getIpFromAt(response, command) }
            StateMachine.GET_STATUS_AP      -> { getApStatusFromAT(response, command) }
            StateMachine.PING               -> { getPing(response, command) }
            StateMachine.DATA_CONNECTION    -> { getDataConnection(response, command, serviceStep) }
            // ****************************************************************************************




            else -> Log.e(TAG, "STATE -> $state, RESPONSE -> ${response.toHex()}, COMMAND -> $command")
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
                    postDelayed(disconnectionRunnable, UI_TIMEOUT)
                }
            }
            DisconnectionReason.DISCONNECTION_OCURRED, DisconnectionReason.CONNECTION_FAILED -> {
                runOnUiThread { toast("No se puede conectar con el dispositivo") }
                handler.apply {
                    postDelayed(disconnectionRunnable, UI_TIMEOUT)
                }
            }
            DisconnectionReason.FIRMWARE_UNSOPPORTED -> {
                runOnUiThread { toast("Dispositivo no soportado") }
                handler.apply {
                    postDelayed(disconnectionRunnable, UI_TIMEOUT)
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

        private var statusCountDown     = 0
        private var waitCountDown       = 0
        private var retryConnection     = 0
        private var serviceStep         = 0

        // Timeouts de la actividad
        private const val UI_TIMEOUT        = 500L
    }
}
