package mx.softel.marketsafe.activities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.icu.util.TimeZone
import android.location.Location
import android.location.LocationManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.scanning_mask.*
import mx.softel.bleservicelib.BleService
import mx.softel.bleservicelib.enums.ConnState
import mx.softel.bleservicelib.enums.DisconnectionReason
import mx.softel.cirwirelesslib.constants.*
import mx.softel.cirwirelesslib.enums.ReceivedCmd
import mx.softel.cirwirelesslib.enums.StateMachine
import mx.softel.cirwirelesslib.extensions.hexStringToByteArray
import mx.softel.cirwirelesslib.extensions.toCharString
import mx.softel.cirwirelesslib.extensions.toHex
import mx.softel.cirwirelesslib.utils.BleCirWireless
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.cirwirelesslib.utils.CommandUtils
import mx.softel.marketsafe.R
import mx.softel.marketsafe.RepositoryModel
import mx.softel.marketsafe.UserPermissions
import mx.softel.marketsafe.dialog_module.GenericDialogButtons
import mx.softel.marketsafe.dialog_module.dialog_interfaces.DialogInteractor
import mx.softel.marketsafe.dialog_module.dialog_models.BaseDialogModel
import mx.softel.marketsafe.dialogs.*
import mx.softel.marketsafe.extensions.toast
import mx.softel.marketsafe.fragments.*
import mx.softel.marketsafe.interfaces.FragmentNavigation
import mx.softel.marketsafe.utils.Utils
import mx.softel.marketsafe.web_services_module.ui_login.log_in_dialog.DialogButtonsModel
import mx.softel.marketsafe.web_services_module.web_service.ApiClient
import mx.softel.marketsafe.web_services_module.web_service.LinkPostResponse
import mx.softel.scanblelib.extensions.toHexValue
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.time.ZoneId
import java.util.*

private const val TAG = "WiFiConfigurationActivi"

class WiFiConfigurationActivity :   AppCompatActivity(),
                                    FragmentNavigation,
                                    PasswordDialog.OnDialogClickListener,
                                    WifiOkDialog.OnWifiDialogListener,
                                    BleService.OnBleConnection {
    private var maxTimes        = 0
    private var triesConnect    = 0

    private var firstTime                       = true
    internal var goingToTabActivity             = false
    private var commandSent                     = false
    internal var configAndTesting               = false

    private lateinit var token              : String
    internal var userPermissions            : UserPermissions? = null

    private var fwModuleEsp                 : String = ""
    private var fwModule                    : String = ""


    // BLUETOOTH DEVICE
    internal lateinit var bleDevice     : BluetoothDevice
    internal lateinit var bleMac        : String

    // COMMANDS DATA
    internal lateinit var ssidSelected  : String
    internal lateinit var passwordTyped : String
    internal lateinit var bleMacBytes   : ByteArray

    internal var isTransmiting          : Boolean = false
    internal lateinit var serialNumber  : String
    internal lateinit var assetType     : String
    internal lateinit var assetMode     : String



    // VALORES PARA FRAGMENT DE TESTER
    internal var ipAssigned         : String        = ""
    internal var ipRouterAssigned   : String        = ""
    internal var apAssigned         : Boolean       = false
    internal var ssidAssigned       : String        = ""
    internal var rssiAssigned       : String        = ""
    internal var pingAssigned       : Boolean       = false
    internal var dataAssigned       : Boolean       = false
    private  var staticIp           : Boolean       = false

    // SERVICE CONNECTIONS / FLAGS
    internal var service            : BleService?    = null
    internal var cirService         : BleCirWireless = BleCirWireless()
    private  var isServiceConnected : Boolean        = false
    private  var isRefreshed        : Boolean        = false
    private  var isScanning         : Boolean        = false
    private  var isWifiConnected    : Boolean        = false
    private  var getApLaunched      : Boolean        = false
    private  var beaconBytes        : ByteArray?     = null

    // VARIABLES DE FLUJO
    private  var disconnectionReason: DisconnectionReason = DisconnectionReason.UNKNOWN
    internal var deviceMacList      : ArrayList<String>?    = null
    internal var actualFragment     : Fragment?             = null
    private  val mainFragment       : MainFragment = MainFragment.getInstance()
    internal val testerFragment     : TesterFragment = TesterFragment.getInstance()
    private val wifiFragment        : AccessPointsFragment = AccessPointsFragment.getInstance()
    private val wiFiPasscodeFragment: WiFiPasscodeFragment = WiFiPasscodeFragment.getInstance()
    private val configTestCooler    : ConfigTestCooler = ConfigTestCooler.getInstance()


    // HANDLERS / RUNNABLES
    private val handler               = Handler(Looper.getMainLooper())
    private val disconnectionRunnable = Runnable { finishAndDisconnectActivity(disconnectionReason.status) }

    // DIALOGOS
    private var waitDialog = WaitDialog()
    private lateinit var ipConfigValuesDialog   : IpConfigValuesDialog

    // IP STATIC
    private lateinit var ipConfigValues : IpConfigModel

    // REPOSITORY URL
    private lateinit var repositoryModel: RepositoryModel


    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wi_fi_configuration)
        setScanningUI()
        getAndSetIntentData()
        initFragment()
        checkLocation()
    }


    /**
     * ## setScanningUI
     * Muestra en la actividad la máscara de escaneo
     */
    internal fun setScanningUI() {
        scanMask.apply {
            visibility = View.VISIBLE
            background = getDrawable(R.color.gray70)
            setOnClickListener { toast(getString(R.string.wait_moment)) }
        }
        pbScanning.visibility = View.INVISIBLE
        lavLoaderPositive.visibility = View.VISIBLE
        isScanning = true
    }

    /**
     * ## setStandardUI
     * Elimina de la vista la máscara de bloqueo de escaneo
     */
    internal fun setStandardUI() {
        scanMask.visibility = View.GONE
        pbScanning.visibility = View.GONE
        lavLoaderPositive.visibility = View.GONE
        isScanning = false
    }


    /**
     * ## getAndSetIntentData
     * Obtiene los elementos recibidos en el intent, para posteriormente
     * mandarlo al fragment que muestre los datos, así como el objeto
     * del dispositivo con el cual se va a interactuar
     */
    private fun getAndSetIntentData() {
        // Obtenemos la información del intent
        val data        = intent.extras!!
        bleDevice       = data[EXTRA_DEVICE] as BluetoothDevice
        bleMac          = data.getString(EXTRA_MAC)!!
        bleMacBytes     = bleMac.hexStringToByteArray()
        isTransmiting   = data.getBoolean(TRANSMITION)
        serialNumber    = data.getString(SERIAL_NUMBER)!!
        assetType       = data.getString(ASSET_TYPE)!!
        assetMode       = data.getString(ASSET_MODEL)!!
        token           = data.getString(TOKEN)!!
        userPermissions = data.getSerializable(USER_PERMISSIONS) as UserPermissions
        // Log.e(TAG, "TOKEN: ${token}")

        beaconBytes = data[EXTRA_BEACON_BYTES] as ByteArray
        val beaconId    = "0x${byteArrayOf(this.beaconBytes!![5], this.beaconBytes!![6]).toHexValue().toUpperCase(
            Locale.ROOT)}"
        enableWifiConfig = isACirWifiBeacon(beaconId)
    }

    private fun isACirWifiBeacon (beaconId: String) : Boolean = when (beaconId) {
        "0x000B" -> true
        "0x000C" -> true
        "0x000D" -> false
        "0x000E" -> false
        else -> false
    }


    /**
     * ## finishAndDisconnectActivity
     * Desconecta los dispositivos asociados, elimina los callbacks
     * y termina la actividad, para volver a la lista de dispositivos escaneados
     */
    internal fun finishAndDisconnectActivity(disconnectionReason: Int) {
        service!!.disconnectBleDevice(disconnectionReason)
        handler.removeCallbacks(disconnectionRunnable)
        actualFragment = null
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    /**
     * ## backFragment
     * Realiza la navegación inversa de fragments, si ya no hay fragments por
     * expulsar, desconecta el dispositivo y termina la actividad
     */
    internal fun backFragment() {
        Log.e(TAG, "backFragment: ")

        when (actualFragment) {
            wifiFragment -> {
                finishAndDisconnectActivity(DisconnectionReason.UNKNOWN.status)
                return
            }

            wiFiPasscodeFragment -> { actualFragment  = wifiFragment }

            configTestCooler -> {
                if (!configAndTesting) { actualFragment = wiFiPasscodeFragment }

                else {
                    toast(getString(R.string.wifi_configuration_running))
                    return
                }
            }
        }


        removeCurrentFragment()
        navigateTo(actualFragment!!, true, null)
    }

    internal fun removeCurrentFragment () {
        val oldFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        if (oldFragment != null) { supportFragmentManager.beginTransaction().remove(oldFragment).commit() }
    }


    /************************************************************************************************/
    /**     NAVIGATION                                                                              */
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
     * ## initFragment
     * Inicializa el fragmento [MainFragment] para el manejo de
     * la instancia principal de conexión
     */
    @SuppressLint("CommitTransaction")
    private fun initFragment() {
        // Iniciamos el fragmento deseado
        val fragment    = wifiFragment // mainFragment
        actualFragment  = fragment

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .commit()
    }


    private fun checkLocation(){
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) showAlertLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    private fun showAlertLocation() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(getString(R.string.no_location_services))
        dialog.setPositiveButton(getString(R.string.settings)) { _, _ ->
            val myIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }
        dialog.setNegativeButton(getString(R.string.tv_cancelar)) { _, _ ->
            finish()
        }
        dialog.setCancelable(false)
        dialog.show()
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

        toast(getString(R.string.configuring_wifi_device), Toast.LENGTH_LONG)
        passwordTyped = password
        CirCommands.setDeviceModeCmd(service!!,
            cirService.getCharacteristicWrite()!!,
            AT_MODE_MASTER_SLAVE,
            bleMacBytes)
        cirService.setCurrentState(StateMachine.WIFI_CONFIG)
        setScanningUI()
    }

    /**
     * ## dialogCancel
     * Se ejecuta al hacer click en "Cancelar" del diálogo de Password
     */
    override fun dialogCancel() {
        toast(getString(R.string.cancelled))
    }

    /**
     * ## dialogOk
     * Se ejecuta al hacer click en "Aceptar" en el diálogo de
     * "Wifi correctamente configurado".
     * Termina el fragmento de Access Points y retorna al punto inicial
     */
    override fun dialogOk() {
        backFragment()
    }


    internal fun goToWiFiPasscode () {
        setScanningUI()
        removeCurrentFragment()
        actualFragment = wiFiPasscodeFragment
        navigateTo(actualFragment!!, false, null)
    }

    internal fun goToTabMainActivity () {
        goingToTabActivity = true

        val intent = Intent(this, TabMainActivity::class.java)
        intent.apply {
            putExtra(EXTRA_DEVICE,              bleDevice)
            putExtra(EXTRA_MAC,                 bleMac)
            putExtra(EXTRA_BEACON_BYTES,        bleMacBytes)
            putExtra(SSID, ssidSelected)
            putExtra(SSID_PASSCODE, passwordTyped)
            putExtra(EXTRA_BEACON_BYTES, beaconBytes)

            // Solkos' flags
            putExtra(TOKEN, token)
            putExtra(USER_PERMISSIONS, userPermissions)
            putExtra(TRANSMITION, isTransmiting)
            putExtra(SERIAL_NUMBER, serialNumber)
            putExtra(ASSET_TYPE, assetType)
            putExtra(ASSET_MODEL, assetMode)
            startActivity(this)
        }
        finish()
    }


    internal fun initWiFiConfig () {
        configAndTesting = true
        CirCommands.setDeviceModeCmd(service!!,
            cirService.getCharacteristicWrite()!!,
            AT_MODE_MASTER_SLAVE,
            bleMacBytes)

        cirService.setCurrentState(StateMachine.WIFI_CONFIG)
    }

    internal fun initWiFiTest () {
        cirService.setCurrentState(StateMachine.UNKNOWN)
    }

    internal fun initGetFwModule () {
        cirService.setCurrentState(StateMachine.GET_FIRMWARE_WIFI_MODULE)
    }

    internal fun setFwModule (saveFwModule: String) {
        fwModuleEsp = saveFwModule
    }

    internal fun getFwModule (): String {
        return fwModuleEsp
    }

    internal fun initUpdateUrl () {
        cirService.setCurrentState(StateMachine.GET_CLIENT)
    }


    /************************************************************************************************/
    /**     CONTROL TESTER STATE MACHINE                                                            */
    /************************************************************************************************/
    /**
     * ## wifiConfigProcess
     * Máquina de estados mientras se espera la respuesta del comando AT
     * de configuración WIFI. Está en espera de respuesta, buscando en ella
     * WIFI GOT IP. Ejecuta el diálogo pertinente según el estado de la respuesta
     *
     * @param response Respuesta en Bytes
     * @param command Tipo de respuesta recibida
     */
    private fun wifiConfigProcess(response: ByteArray, command: ReceivedCmd, step: Int) {
        // Log.e("WifiConfigProcess", "step: $step response: ${response.toHex()}")
        when (command) {
            ReceivedCmd.AT_READY -> {
                when (step) {
                    0   -> parseOkWifiConfigured(response, 100)
                    100 -> parseOkWifiConfigured(response, 1)
                    1   -> parseOkWifiConfigured(response, 101)
                    101 -> parseOkWifiConfigured(response, 2)
                    2   -> parseWifiConfigured  (response)
                    3   -> parseOkWifiConfigured(response, 0)
                }
            }
            else -> {
                if (command == ReceivedCmd.POLEO) {
                    // Log.e("Reading AT", "Writing command AT")
                    CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
                }
            }
        }
    }

    /**
     * ## parseOkWifiConfigured
     * Establece la siguiente acción partiendo de la máquina de estados de
     * conexión Wifi, validando a cada paso que la respuesta sea un OK
     *
     * @param dataResponse Cadena de respuesta del comando AT
     * @param nextStep Estado siguiente de la máquina de estados
     */
    private fun parseOkWifiConfigured(dataResponse: ByteArray, nextStep: Int) {

        val decResponse = CommandUtils.decryptResponse(dataResponse, bleMacBytes)
        val response = decResponse.toCharString()
        Log.e(TAG, "parseOkWifiConfigured: ${decResponse.toHex()} -> ${decResponse.toCharString()}")

        if (response.contains(AT_CMD_OK)) {
            when (nextStep) {
                0 -> {
                    cirService.setCurrentState(StateMachine.POLING)

                    runOnUiThread {
                        setStandardUI()

                        if (isWifiConnected) {
                            // WiFi exitosamente configurado
                            // showWifiOkDialog()
                            Log.e(TAG, "se termina la configuracion entre el enfriador y el rauter")
                            (actualFragment as ConfigTestCooler).successfullyConfigured()
                            (actualFragment as ConfigTestCooler).currentState(getString(R.string.ok_wifi))
                        }
                        else {
                            showWifiBadDialog()
                            (actualFragment as ConfigTestCooler).stopAnimRouter()
                            (actualFragment as ConfigTestCooler).stopAnimCloud()
                        }
                    }

                }

                100 -> {
                    // Se ejecutan en otro hilo
                    runOnUiThread {(actualFragment as ConfigTestCooler).currentState(getString(R.string.reset_wifi))}
                    CirCommands.resetWifiCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                }

                1   -> {
                    // Se ejecutan en otro hilo
                    runOnUiThread {(actualFragment as ConfigTestCooler).currentState(getString(R.string.internal_config))}
                    CirCommands.setInternalWifiCmd(service!!, cirService.getCharacteristicWrite()!!, ssidSelected, passwordTyped, AT_NO_SEND_SSID, bleMacBytes)
                }

                101 -> {
                    // Se ejecutan en otro hilo
                    runOnUiThread {(actualFragment as ConfigTestCooler).currentState(getString(R.string.autconnect))}
                    CirCommands.setAutoConnCmd(service!!, cirService.getCharacteristicWrite()!!, 1, bleMacBytes)
                }

                2   -> {
                    // Se ejecutan en otro hilo
                    runOnUiThread {(actualFragment as ConfigTestCooler).currentState(getString(R.string.autconnect))}
                    CirCommands.sendConfigureWifiCmd(service!!, cirService.getCharacteristicWrite()!!, ssidSelected, passwordTyped, bleMacBytes)
                }
            }

            wifiStep = nextStep

        } else {
            wifiStep = 0
            parseOkWifiConfigured(dataResponse, wifiStep)
        }
    }


    /************************************************************************************************/
    /**     PARSER                                                                                  */
    /************************************************************************************************/
    /**
     * ## parseWifiConfigured
     * Extrae el estatus de conexión obtenido por el dispositivo
     *
     * @param dataResponse Cadena de respuesta del comando AT
     */
    private fun parseWifiConfigured(dataResponse: ByteArray) {

        val decResponse = CommandUtils.decryptResponse(dataResponse, bleMacBytes)
        val response = decResponse.toCharString()
        // Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

        if (response.contains("WIFI GOT IP")) {
            wifiStep = 3
            isWifiConnected = true
            CirCommands.setDeviceModeCmd(service!!, cirService.getCharacteristicWrite()!!, AT_MODE_SLAVE, bleMacBytes)
        } else if (response.contains(AT_CMD_ERROR)) {
            wifiStep = 3
            isWifiConnected = false
            CirCommands.setDeviceModeCmd(service!!, cirService.getCharacteristicWrite()!!, AT_MODE_SLAVE, bleMacBytes)
        }
        CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
    }


    private fun showWifiBadDialog () {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.generic_dialog_two_btns,
            R.drawable.router,
            getString(R.string.nok_wifi),
            getString(R.string.tv_wifi_nok),
            getString(R.string.accept),
            getString(R.string.accept),
            View.GONE,
            View.VISIBLE
        )


        val dialog = GenericDialogButtons(
            this@WiFiConfigurationActivity,
            baseDialogModel,
            object : DialogInteractor {
                override fun positiveClick(dialog: GenericDialogButtons) {}
                override fun negativeClick(dialog: GenericDialogButtons) {
                    dialog.dismiss()
                }
            })

        dialog.show()
    }


    private fun showTransmitionNotOkDialog () {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.login_error_dialog, -1,
            getString(R.string.bad_config),
            getString(R.string.config_needed),
            getString(R.string.config),
            getString(R.string.config),
            View.GONE,
            View.VISIBLE
        )


        val dialog = GenericDialogButtons(
            this@WiFiConfigurationActivity,
            baseDialogModel,
            object : DialogInteractor {

                override fun positiveClick(dialog: GenericDialogButtons) { }

                override fun negativeClick(dialog: GenericDialogButtons) {
                    (actualFragment as MainFragment).updateHotspot()
                    dialog.dismiss()
                }
            })

        dialog.show()
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
        disconnectionReason = reason

        Log.e(TAG, "errorConnection: $reason")

        when (reason) {
            DisconnectionReason.NOT_AVAILABLE, DisconnectionReason.FAILURE -> {
                runOnUiThread { toast(getString(R.string.error_occurred)) }
                handler.apply { postDelayed(disconnectionRunnable, UI_TIMEOUT) }
            }
            DisconnectionReason.CONNECTION_FAILED -> {
                runOnUiThread { toast(getString(R.string.timeout_expired)) }
                handler.apply { postDelayed(disconnectionRunnable, UI_TIMEOUT) }
            }
            DisconnectionReason.FIRMWARE_UNSUPPORTED -> {
                runOnUiThread { toast(getString(R.string.unsupported_device)) }
                handler.apply { postDelayed(disconnectionRunnable, UI_TIMEOUT) }
            }
            else -> {
                runOnUiThread { if (!goingToTabActivity) { toast(getString(R.string.device_disconnecting)) } }
                handler.apply {
                    postDelayed(disconnectionRunnable, UI_TIMEOUT)
                }
            }
        }
    }

    private fun timeoutResendCommand (timeInMillis: Long) {
        Handler(mainLooper).postDelayed({
            commandSent = false
        }, timeInMillis)

    }

    /**
     * ## connectedDevice
     * Notifica en pantalla que el dispositivo fue correctamente conectado.
     * Manda a escanear los servicios que el dispositivo contiene, e inicializa
     * algunas características necesarias para la comunicación
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun connectedDevice() {
        runOnUiThread { toast(getString(R.string.device_connected)) }
        service!!.discoverDeviceServices()
        sendLocation()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission", "NewApi")
    private fun sendLocation () {
        val tz: TimeZone = TimeZone.getDefault()
        System.out.println(
            "TimeZone   " + tz.getDisplayName(false, TimeZone.SHORT)
                .toString() + " Timezone id :: " + tz.getID()
        )
        val zone: ZoneId = ZoneId.systemDefault()

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                val latitude =  location?.latitude
                val longitude = location?.longitude
                val body = JSONObject()

                body.put("mac", bleDevice.address)
                body.put("latitude", latitude)
                body.put("longitude", longitude)
                body.put("timezone_id", zone.toString())

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = body.toString().toRequestBody(mediaType)
                Log.e(TAG, body.toString())
                fetchLink(token, requestBody)
            }


    }


    private fun fetchLink (token: String, infoMac: RequestBody) {
        val apiClient = ApiClient()
        // Log.e(TAG, "token ${token}")
        apiClient.getApiService(this).fetchLinkPost(token = "Bearer $token", infoMac)
            .enqueue(object : retrofit2.Callback<LinkPostResponse> {
                override fun onFailure(call: Call<LinkPostResponse>, t: Throwable) {
                    // Error fetching posts
                    Log.e(TAG, "onFailure: ${t.message}")
                    runOnUiThread { Utils.showToastShort(applicationContext, getString(R.string.link_device_error)) }
                }

                override fun onResponse(call: Call<LinkPostResponse>, response: Response<LinkPostResponse>) {
                    // Handle function to display posts
                    Log.e(TAG, "onResponse: ${response.body()}")
                    val body = response.body()

                    if (body != null) {
//                        runOnUiThread { Utils.showToastShort(applicationContext, getString(R.string.link_device_ok)) }
                        Log.e(TAG, "Device linked and location updated")
                    } else {
                        Log.e(TAG, "Error ocurred: Location was not sent correctly")
//                        runOnUiThread { Utils.showToastShort(applicationContext, getString(R.string.link_device_error)) }
                    }
                }
            })
    }


    /************************************************************************************************/
    /**     BLE SERVICE CONNECTION                                                                  */
    /************************************************************************************************/
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, xservice: IBinder?) {
            // Obtenemos la instancia del servicio una vez conectado
            service = (xservice as (BleService.LocalBinder)).getService()
            service!!.apply {
                registerActivity(this@WiFiConfigurationActivity)
                connectBleDevice(bleDevice)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service!!.stopService()
        }
    }

    private fun doBindService() {
        // Conecta la aplicación con el servicio
        bindService(Intent(this, BleService::class.java),
            connection, Context.BIND_AUTO_CREATE)
        isServiceConnected = true
    }

    private fun doUnbindService() {
        if (isServiceConnected) {
            // Termina la conexión existente con el servicio
            service!!.stopService()
            unbindService(connection)
            isServiceConnected = false
        }
    }


    /************************************************************************************************/
    /**     ACTIVITY INTERFACES                                                                     */
    /************************************************************************************************/
    interface RootEvents {
        fun deviceConnected ()

        fun updateHotspot ()

        fun testConnection ()
    }


    interface RootBleEvents {
        fun deviceConnected ()

        fun updateHotspot ()

        fun testConnection ()

        fun successfullyConfigured ()

        fun currentState (state: String)

        fun testFinished ()
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = WiFiConfigurationActivity::class.java.simpleName
        var enableWifiConfig        = true

        private var retryAtResponse         = 0
        private var serviceStep             = 0
        private var wifiStep                = 0
        private var cipStatusMode           = -1

        // Timeouts de la actividad
        private const val UI_TIMEOUT        = 500L
        private const val MAX_AT_RETRY      = 2
    }

    override fun characteristicChanged(characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    override fun characteristicRead(characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    override fun characteristicWrite(characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    override fun connectionStatus(status: DisconnectionReason, newState: ConnState) {
        TODO("Not yet implemented")
    }

    override fun descriptorWrite() {
        TODO("Not yet implemented")
    }

    override fun mtuChanged() {
        TODO("Not yet implemented")
    }

    override fun servicesDiscovered(gatt: BluetoothGatt) {
        TODO("Not yet implemented")
    }
}