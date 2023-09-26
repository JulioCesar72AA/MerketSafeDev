package mx.softel.marketsafe.activities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import android.icu.util.TimeZone
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_config_test_cooler.*
import kotlinx.android.synthetic.main.scanning_mask.*
import mx.softel.bleservicelib.BleService
import mx.softel.bleservicelib.enums.ConnState
import mx.softel.bleservicelib.enums.DisconnectionReason
import mx.softel.cirwirelesslib.constants.*
import mx.softel.cirwirelesslib.enums.*
import mx.softel.cirwirelesslib.extensions.hexStringToByteArray
import mx.softel.cirwirelesslib.extensions.toCharString
import mx.softel.cirwirelesslib.extensions.toHex
import mx.softel.cirwirelesslib.utils.BleCirWireless
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.cirwirelesslib.utils.CirWirelessParser
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
import mx.softel.marketsafe.web_services_module.ui_login.log_in_dialog.DialogButtonsModel
import mx.softel.marketsafe.web_services_module.web_service.ApiClient
import mx.softel.marketsafe.web_services_module.web_service.TransmitPostResponse
import mx.softel.marketsafe.utils.Utils
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

class RootActivity : AppCompatActivity(),
                     FragmentNavigation,
                     PasswordDialog.OnDialogClickListener,
                     WifiOkDialog.OnWifiDialogListener,
                     BleService.OnBleConnection,
                     ConfigSelectorDialog.OnDialogClickListener,
                     IpConfigValuesDialog.OnDialogClickListener {

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
    private  var disconnectionReason: DisconnectionReason   = DisconnectionReason.UNKNOWN
    internal var deviceMacList      : ArrayList<String>?    = null
    internal var actualFragment     : Fragment?             = null
    private  val mainFragment       : MainFragment          = MainFragment.getInstance()
    internal val testerFragment     : TesterFragment        = TesterFragment.getInstance()
    private val wifiFragment        : AccessPointsFragment  = AccessPointsFragment.getInstance()
    private val wiFiPasscodeFragment: WiFiPasscodeFragment  = WiFiPasscodeFragment.getInstance()
    private val configTestCooler    : ConfigTestCooler      = ConfigTestCooler.getInstance()


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

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setScanningUI()
        getAndSetIntentData()
        initFragment()
        checkLocation()
    }

    override fun onResume() {
        super.onResume()
        doBindService()     // Asociamos el servicio de BLE
    }

    override fun onPause() {
        super.onPause()
        finishAndDisconnectActivity(DisconnectionReason.UNKNOWN.status)
    }

    override fun onDestroy() {
        super.onDestroy()
        doUnbindService()   // Desasociamos el servicio de BLE
    }

    /**
     * ## onBackPressed
     * Bloquea el "BackButton" mientras la pantalla se encuentra en
     * estado de "escaneando"
     */
    override fun onBackPressed() {
        if (isScanning) {
            toast(getString(R.string.wait_moment))
            return
        }
        backFragment()
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
        serviceStep = 0
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
        val beaconId    = "0x${byteArrayOf(this.beaconBytes!![5], this.beaconBytes!![6]).toHexValue().toUpperCase(Locale.ROOT)}"
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
     * ## initFragment
     * Inicializa el fragmento [MainFragment] para el manejo de
     * la instancia principal de conexión
     */
    private fun initFragment() {
        // Iniciamos el fragmento deseado
        val fragment = wifiFragment // mainFragment
        actualFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * ## backFragment
     * Realiza la navegación inversa de fragments, si ya no hay fragments por
     * expulsar, desconecta el dispositivo y termina la actividad
     */
    internal fun backFragment() {
        if (actualFragment == mainFragment) {
            finishAndDisconnectActivity(DisconnectionReason.UNKNOWN.status)
            return
        }
        if (actualFragment == testerFragment) actualFragment = mainFragment
        if (actualFragment == wifiFragment) actualFragment = mainFragment
        if (actualFragment == wiFiPasscodeFragment) actualFragment = wifiFragment
        supportFragmentManager.popBackStackImmediate()
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
        finish()
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    /**
     * ## dialogConfigSelector
     * Se ejecutan de acuerdo a la IP seleccionada
     */
    override fun staticIpSelected() {
        staticIp = true
        cirService.setCurrentState(StateMachine.SHOW_PARAMETERS_STATIC_IP)
    }

    override fun dynamicIpSelected() {
        staticIp = false
        cirService.setCurrentState(StateMachine.SETTING_MODE_IP_CONFIG)
        showWaitDialog()
    }


    private fun showWaitDialog () {
        runOnUiThread {
            waitDialog.show(supportFragmentManager, TAG)
        }
    }


    private fun dismissWaitDialog () {
        if (waitDialog.isVisible) waitDialog.dismiss()
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    /**
     * ## ipConfigDialog
     * Se obtienen los valores de la IP
     */
    override fun staticIpValues (ipValues: IpConfigModel) {
        ipConfigValues = ipValues
        cirService.setCurrentState(StateMachine.SETTING_MODE_IP_CONFIG)
        showWaitDialog()
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
        actualFragment = wiFiPasscodeFragment
        setScanningUI()
    }

    internal fun goToConfigAndTest () {
        actualFragment = configTestCooler
        setScanningUI()
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

    private fun checkStatus(response: ByteArray, command: ReceivedCmd) {
        // Log.e(TAG, "cipStatusMode: $cipStatusMode")

        if (cipStatusMode == -1) {
            // Log.e(TAG, "SENDING_STATUS_COMMAND")
            if (command == ReceivedCmd.POLEO && !commandSent) {
                CirCommands.checkCipStatusCmd(service!!, cirService.getCharacteristicWrite()!!,bleMacBytes)
                cipStatusMode = -2
                commandSent = true
                timeoutResendCommand(30_000)
            }
        }

        when (command) {
            ReceivedCmd.AT_READY -> {
                commandSent = false
                parseStatus(response)
            }
            else -> {
                if (command == ReceivedCmd.POLEO) {
                    CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
                    // Log.e(TAG, "READIN_CHAR GG")
                }
            }
        }
    }

    /***********************************************************************************************
     * UPDATE FIRMWARE ROUTES
     *
     * Actualiza las rutas de los firmwares almacenados en la nube
     ***********************************************************************************************/
    private fun isRepositoryUrlUpdated (response: ByteArray, command: ReceivedCmd) {
        if (command == ReceivedCmd.POLEO && !commandSent) {

            CirCommands.setRepositoryUrl(repositoryModel.repositoryUrl(), repositoryModel.port(), service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            commandSent = true
            timeoutResendCommand(60_000)

        } else if (command == ReceivedCmd.POLEO || command == ReceivedCmd.AT_OK) {

            CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)

        } else if (command == ReceivedCmd.AT_READY) {
            val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
            val response    = decResponse.toCharString()

            Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

            if (response.contains(AT_CMD_OK)) {
                commandSent = false
                cirService.setCurrentState(StateMachine.SETTING_FIRMWARE_PATH)

            } else if (response.contains(AT_CMD_ERROR)) {
                dismissWaitDialog()
                cirService.setCurrentState(StateMachine.POLING)
                runOnUiThread { toast(getString(R.string.error_ocurred)) }
            }
        }
    }

    private fun getFirmwareModule(response: ByteArray, command: ReceivedCmd) {
        Log.e(TAG, "getFirmwareModule")
        if (command == ReceivedCmd.POLEO && !commandSent) {
            CirCommands.getFirmwareWiFiModule(
                service!!,
                cirService.getCharacteristicWrite()!!,
                bleMacBytes
            )
            commandSent = true

        } else if (command == ReceivedCmd.AT_OK || command == ReceivedCmd.POLEO) {

            CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)

        } else if (command == ReceivedCmd.AT_READY) {
            val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
            val response = decResponse.toCharString()

            Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

            if (response.contains(AT_CMD_OK)) {
                commandSent = false

                val respondModule: String = decResponse.toCharString()

                // Recupera la version del modulo ESPxx
                fwModule = respondModule.substringAfter("Ver: \"")
                fwModule = fwModule.substringBefore("Compile Date:")
                    .replace("\"", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .replace(" ", "")

                Log.e("FW MODULE", fwModule)
                setFwModule(fwModule)

                // Se sigue a configurar el moduo wifi
                initWiFiConfig()

            } else if (response.contains(AT_CMD_ERROR)) {

                runOnUiThread { toast(getString(R.string.error_ocurred)) }
            }
        }
    }


    private fun isFirmwarePathUpdated (response: ByteArray, command: ReceivedCmd) {
        if (command == ReceivedCmd.POLEO && !commandSent) {
            CirCommands.setFirmwarePath(repositoryModel.path(), repositoryModel.imagePrefix(), service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            commandSent = true
            timeoutResendCommand(30_000)

        } else if (command == ReceivedCmd.AT_OK || command == ReceivedCmd.POLEO) {

            CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)

        } else if (command == ReceivedCmd.AT_READY) {
            val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
            val response    = decResponse.toCharString()

            Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

            if (response.contains(AT_CMD_OK)) {
                commandSent = false
                cirService.setCurrentState(StateMachine.SETTING_FIRMWARE_VERSION)

            } else if (response.contains(AT_CMD_ERROR)) {
                dismissWaitDialog()
                cirService.setCurrentState(StateMachine.POLING)
                runOnUiThread { toast(getString(R.string.error_ocurred)) }
            }
        }
    }

    private fun isFirmwareVersionUpdated (response: ByteArray, command: ReceivedCmd) {
        if (command == ReceivedCmd.POLEO && !commandSent) {
            CirCommands.setFirmwareVersion(repositoryModel.imageVersion(), service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            commandSent = true
            timeoutResendCommand(30_000)

        } else if (command == ReceivedCmd.AT_OK || command == ReceivedCmd.POLEO) {

            CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)

        } else if (command == ReceivedCmd.AT_READY) {
            val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
            val response    = decResponse.toCharString()

            Log.e(TAG, "isFirmwareVersionUpdated: ${decResponse.toHex()} -> ${decResponse.toCharString()}")

            if (response.contains(AT_CMD_OK)) {
                commandSent = false
                dismissWaitDialog()
                cirService.setCurrentState(StateMachine.POLING)
                runOnUiThread { toast(getString(R.string.device_successfully_configured)) }

            } else if (response.contains(AT_CMD_ERROR)) {

                if (response.contains("is equal or older than")) {
                    runOnUiThread { toast(getString(R.string.device_successfully_configured)) }
                } else {
                    runOnUiThread { toast(getString(R.string.card_updated_or_error)) }
                }

                dismissWaitDialog()
                cirService.setCurrentState(StateMachine.POLING)

            }
        }
    }

    private fun isIpModeConfigOk (response: ByteArray, command: ReceivedCmd) {
        if (command == ReceivedCmd.POLEO && !commandSent) {
            if (staticIp)
                CirCommands.setStaticIp(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)

            else
                CirCommands.setDyanmicIp(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)

            commandSent = true
            timeoutResendCommand(30_000)

        } else if (command == ReceivedCmd.AT_OK || command == ReceivedCmd.POLEO) {

            CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)

        } else if (command == ReceivedCmd.AT_READY) {
            val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
            val response = decResponse.toCharString()
            // Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

            if (response.contains(AT_CMD_OK)) {
                commandSent = false

                if (staticIp)
                    cirService.setCurrentState(StateMachine.SETTING_STATIC_IP_VALUES)

                else
                    cirService.setCurrentState(StateMachine.GET_AP)

                dismissWaitDialog()

            } else if (response.contains(AT_CMD_ERROR)) {
                dismissWaitDialog()
                cirService.setCurrentState(StateMachine.POLING)
                runOnUiThread { toast(getString(R.string.error_ocurred)) }
            }
        }
    }

    private fun areIpConfigValuesOk (response: ByteArray, command: ReceivedCmd) {
        if (command == ReceivedCmd.POLEO && !commandSent) {

            CirCommands.setStaticIpValues(
                ipConfigValues.ipAddress(),
                ipConfigValues.maskAddress(),
                ipConfigValues.gateway(),
                service!!,
                cirService.getCharacteristicWrite()!!,
                bleMacBytes)

            commandSent = true
            timeoutResendCommand(30_000)

        } else if (command == ReceivedCmd.AT_OK || command == ReceivedCmd.POLEO) {

            CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)

        } else if (command == ReceivedCmd.AT_READY) {

            val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
            val response = decResponse.toCharString()
            // Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

            if (response.contains(AT_CMD_OK)) {
                commandSent = false
                cirService.setCurrentState(StateMachine.GET_AP)
                runOnUiThread { toast(getString(R.string.success)) }

            } else if (response.contains(AT_CMD_ERROR)) {
                dismissWaitDialog()
                cirService.setCurrentState(StateMachine.POLING)
                runOnUiThread { toast(getString(R.string.error_ocurred)) }
            }
        }
    }

    /**
     * ## checkModeSetted
     * Espera confirmación del dispositivo recién configurado
     *
     * @param command Tipo de respuesta recibida
     */
    private fun checkModeSetted(command: ReceivedCmd) {
        when (command) {
            ReceivedCmd.WAIT_AP     ->
                CirCommands.setDeviceModeCmd(service!!,
                                             cirService.getCharacteristicWrite()!!,
                                             AT_MODE_MASTER_SLAVE,
                                             bleMacBytes)
            ReceivedCmd.AT_READY    -> {
                cirService.setCurrentState(StateMachine.GET_CONFIG_AP)
                CirCommands.getInternalWifiCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            }
            else -> CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
        }
    }

    private fun getSsidFromResponse(response: ByteArray, command: ReceivedCmd) {
        when (command) {
            ReceivedCmd.WAIT_AP     -> {
                if (!commandSent) {
                    CirCommands.getInternalWifiCmd(service!!,cirService.getCharacteristicWrite()!!, bleMacBytes)
                    commandSent     = true
                    timeoutResendCommand(10_000)
                }
            }
            ReceivedCmd.AT_READY    -> parseSsidResponse(response)
            else                    -> CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
        }
    }

    /**
     * ## getIpFromAt
     * Obtiene la IP asignada al dispositivo en el Access Point
     *
     * @param response Respuesta en Bytes
     * @param command Tipo de respuesta recibida
     */
    private fun getIpFromAt(response: ByteArray, command: ReceivedCmd) {
        when (command) {
            ReceivedCmd.WAIT_AP     -> {
                if (!commandSent) {
                    CirCommands.sendIpAtCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                    commandSent     = true
                    timeoutResendCommand(10_000)
                }
            }
            ReceivedCmd.AT_READY    -> {
                commandSent     = false
                parseIpResponse(response)
            }
            else                    -> CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
        }
    }

    /**
     * ## getApStatusFromAT
     * Obtiene el Access Point (SSID) al que está actualmente conectado,
     * así como el RSSI que registra del Access Point
     *
     * @param response Respuesta en Bytes
     * @param command Tipo de respuesta recibida
     */
    private fun getApStatusFromAT(response: ByteArray, command: ReceivedCmd) {
        when (command) {
            ReceivedCmd.WAIT_AP     -> {
                if (!commandSent) {
                    CirCommands.sendApConnectionCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                    commandSent     = true
                    timeoutResendCommand(10_000)
                }
            }
            ReceivedCmd.AT_READY    -> {
                commandSent = false
                parseApResponse(response)
            }
            else                    -> CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
        }
    }

    /**
     * ## getPing
     * Ejecuta y recibe del comando AT un ping correcto,
     * incorrecto o con error
     *
     * @param response Respuesta en Bytes
     * @param command Tipo de respuesta recibida
     */
    private fun getPing(response: ByteArray, command: ReceivedCmd) {
        when (command) {
            ReceivedCmd.WAIT_AP     -> {
                if (!commandSent) {
                    CirCommands.sendPing(service!!, cirService.getCharacteristicWrite()!!, "www.google.com", bleMacBytes)
                    commandSent     = true
                    timeoutResendCommand(10_000)
                }
            }
            ReceivedCmd.AT_READY    -> {
                commandSent = false
                parsePingResponse(response)
            }
            else                    -> CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
        }
    }

    /**
     * ## getDataConnection
     * Ejecuta y gestiona la máquina de estados para obtener el estatus
     * de la conexión de datos con el servidor configurado
     *
     * @param response Respuesta en Bytes
     * @param command Tipo de respuesta recibida
     * @param step Paso de la máquina de estados, según avanza en comandos
     */
    private fun getDataConnection(response: ByteArray, command: ReceivedCmd, step: Int) {
        when (command) {
            ReceivedCmd.AT_OK       -> {
                if (!commandSent) {
                    CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
                    commandSent     = true
                    timeoutResendCommand(10_000)
                }

            }
            ReceivedCmd.AT_READY    -> {
                commandSent = false
                parseDataResponse(response, step)
            }
            ReceivedCmd.WAIT_AP     -> {
                when (step) {
                    0 -> {
                        CirCommands.closeAtSocketCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                    }
                    3 -> {
                        CirCommands.sendStatusWifiCmd(service!!, cirService.getCharacteristicWrite()!!)
                    }
                }
            }
            else -> {
                when (step) {
                    3       -> {
                        CirCommands.sendStatusWifiCmd(service!!, cirService.getCharacteristicWrite()!!)
                    }
                    else    -> {
                        CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
                    }
                }
            }
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

    private fun parseStatus(dataResponse: ByteArray) {

        val decResponse = CommandUtils.decryptResponse(dataResponse, bleMacBytes)
        val response = decResponse.toCharString()
        // Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

        if (response.contains(AT_CMD_STATUS)) {

            if (response.contains("TCP")) {
                CirCommands.closeAtSocketCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                return
            }

            // Log.e("parseStatus", "$response")
            cipStatusMode = response.substringAfter("$AT_CMD_STATUS:")
                .substringBefore(AT_CMD_OK)
                .replace("\r", "")
                .replace("\n", "").toInt()
            cirService.setCurrentState(StateMachine.SET_MODE)
        }

        if (response.contains(AT_CMD_CLOSED)) {
            cirService.setCurrentState(StateMachine.SET_MODE)
        }
    }

    /**
     * ## parseSsidResponse
     * Obtiene de la respuesta el access point que vive internamente en
     * el dispositivo y lo actualiza en la pantalla
     *
     * @param dataResponse Cadena de respuesta del comando AT
     */
    private fun parseSsidResponse(dataResponse: ByteArray) {

        val decResponse = CommandUtils.decryptResponse(dataResponse, bleMacBytes)
        val response = decResponse.toCharString()
        // Log.e(TAG, "SSID: ${decResponse.toHex()} -> ${decResponse.toCharString()}")

        if (response.contains(AT_CMD_OK)) {
            ssidAssigned = response
                .substringAfter(SSID_SUBSTRING_AFTER)
                .substringBefore("\",")
            runOnUiThread { configTestCooler.fragmentUiUpdate(1) }
            cirService.setCurrentState(StateMachine.GET_STATUS_AP)
            CirCommands.sendApConnectionCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)

        } else if (response.contains(AT_CMD_ERROR)) {
            val dialog = ConfigInfoDialog(0)
            dialog.show(supportFragmentManager, null)
            runOnUiThread { setStandardUI() }
            cirService.setCurrentState(StateMachine.POLING)
        }
    }

    /**
     * ## parseApResponse
     * Extrae de la respuesta AT el SSID y el RSSI del Access
     * Point actualmente conectado. Realiza 3 reintentos en caso
     * de que no entregue los datos, al tercer intento fallido,
     * se considera fallido
     *
     * @param dataResponse Cadena de respuesta del comando AT
     */
    private fun parseApResponse(dataResponse: ByteArray) {

        val decResponse = CommandUtils.decryptResponse(dataResponse, bleMacBytes)
        val response = decResponse.toCharString()
        // Log.e(TAG, "RSSI: ${decResponse.toHex()} -> ${decResponse.toCharString()}")

        if (response.contains(WIFI_SUBSTRING_AP_AFTER)) {
            rssiAssigned = response
                .substringAfterLast(",-")
                .substringBefore("OK")
                .substringBefore(",0")
                .replace("\r", "")
                .replace("\n", "")
            rssiAssigned = "-$rssiAssigned"
            runOnUiThread { configTestCooler.fragmentUiUpdate(2) }
            cirService.setCurrentState(StateMachine.GET_IP)
        } else {
            if (retryAtResponse >= 2) {
                retryAtResponse = 0
                rssiAssigned = "No conectado"
                runOnUiThread {
                    configTestCooler.fragmentUiUpdate(2)
                    setStandardUI()
                }
                val dialog = ConfigInfoDialog(1)
                dialog.show(supportFragmentManager, null)
                cirService.setCurrentState(StateMachine.POLING)
            } else {
                retryAtResponse++
                CirCommands.sendApConnectionCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            }
        }
    }

    /**
     * ## parseIpResponse
     * Extrae de la respuesta en formato Char la dirección IP que el
     * Access Point asignó al dispositivo. Ejecuta 3 reintentos en caso
     * de que no entregue IP, al tercer intento fallido, se considera fallido
     *
     * @param dataResponse Cadena de respuesta del comando AT
     */
    private fun parseIpResponse(dataResponse: ByteArray) {

        val decResponse = CommandUtils.decryptResponse(dataResponse, bleMacBytes)
        val response = decResponse.toCharString()
        // Log.e(TAG, "ip: ${decResponse.toHex()} -> ${decResponse.toCharString()}")

        if (response.contains(WIFI_NOT_IP_STRING)){
            if (retryAtResponse >= MAX_AT_RETRY) {
                retryAtResponse = 0
                ipAssigned = "IP No asignada"
                apAssigned = false
                runOnUiThread {
                    configTestCooler.fragmentUiUpdate(3)
                    setStandardUI()
                }
                val dialog = ConfigInfoDialog(2)
                dialog.show(supportFragmentManager, null)
                cirService.setCurrentState(StateMachine.POLING)
            } else {
                retryAtResponse++
                CirCommands.sendIpAtCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            }
            return
        } else if ( response.contains( WIFI_SUBSTRING_ROUTER_IP_AFTER )) {
            retryAtResponse = 0
            Log.e(TAG, "response IP: ${response}")

            var ipRouter = response.substringAfter(WIFI_SUBSTRING_ROUTER_IP_AFTER)
            Log.e(TAG, "response ipRouter: ${ipRouter}")

            var ipRead = response.substringAfter(WIFI_SUBSTRING_IP_AFTER)
            Log.e(TAG, "response ipRead: ${ipRead}")

            ipRouter = ipRouter
                .substringBefore(WIFI_SUBSTRING_ROUTER_IP_BEFORE)
                .replace("\"", "")
                .replace("\n", "")
                .replace("\r", "")

            ipRead = ipRead
                .substringBefore(WIFI_SUBSTRING_IP_BEFORE)
                .replace("\"", "")
                .replace("\n", "")
                .replace("\r", "")

            Log.e(TAG, "response ipRouter next: ${ipRouter}")

            Log.e(TAG, "response ipRead next: ${ipRead}")

            ipAssigned          = ipRead
            ipRouterAssigned    = ipRouter
            apAssigned          = true
            cirService.setCurrentState(StateMachine.PING)
            runOnUiThread { configTestCooler.fragmentUiUpdate(3) }
        } else {
            cirService.setCurrentState(StateMachine.GET_IP)
        }
    }

    /**
     * ## parsePingResponse
     * Analiza la respuesta obtenida por el comando AT, y determina
     * si el PING se completó de manera exitosa o no
     *
     * @param response Cadena de respuesta del comando AT
     */
    private fun parsePingResponse(response: ByteArray) {

        val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
        val stringResponse = decResponse.toCharString()
        // Log.e(TAG, "${decResponse.toHex()} -> ${decResponse.toCharString()}")

        pingAssigned = (stringResponse.contains(PING_OK) && !stringResponse.contains(AT_CMD_ERROR))
        runOnUiThread { configTestCooler.fragmentUiUpdate(4) }
        if (pingAssigned)
            cirService.setCurrentState(StateMachine.DATA_CONNECTION)
        else {
            runOnUiThread {
                setStandardUI()
            }
            val dialog = ConfigInfoDialog(3)
            dialog.show(supportFragmentManager, null)
            cirService.setCurrentState(StateMachine.POLING)
        }
    }

    /**
     * ## parseDataResponse
     * Controla el flujo para validar que el servidor de datos
     * y el dispositivo estén correctamente comunicados, basado
     * en máquina de estados
     *
     * @param response Cadena de respuesta del comando AT
     * @param step Estado actual de la máquina de estados
     */
    private fun parseDataResponse(response: ByteArray, step: Int) {

        val decResponse = CommandUtils.decryptResponse(response, bleMacBytes)
        val restring = decResponse.toCharString()
        Log.e(TAG, "DataResponse: ${decResponse.toHex()} -> ${decResponse.toCharString()}")

        when (step) {
            0 -> {
                CirCommands.openAtSocketCmd(service!!, cirService.getCharacteristicWrite()!!, "foodservices.otus.com.mx", "8030", bleMacBytes)
                serviceStep = 1
            }
            1 -> {
                if (restring.contains(AT_CMD_CONNECT)) {
                    CirCommands.closeAtSocketCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                    dataAssigned = true
                } else if (restring.contains(AT_CMD_CLOSED)
                    || restring.contains(AT_CMD_ERROR)) {
                    dataAssigned = false
                    cirService.setCurrentState(StateMachine.POLING)
                    CirCommands.setDeviceModeCmd(service!!, cirService.getCharacteristicWrite()!!, AT_MODE_SLAVE, bleMacBytes)
                    runOnUiThread {
                        configTestCooler.fragmentUiUpdate(5)
                        setStandardUI()
                    }
                    val dialog = ConfigInfoDialog(4)
                    dialog.show(supportFragmentManager, null)
                    return
                }
                serviceStep = 2
                runOnUiThread { configTestCooler.fragmentUiUpdate(5) }
            }
            2 -> {
                if (restring.contains(AT_CMD_CLOSED) || restring.contains(AT_CMD_ERROR)) {
                    cirService.setCurrentState(StateMachine.POLING)
                    CirCommands.setDeviceModeCmd(service!!, cirService.getCharacteristicWrite()!!, AT_MODE_SLAVE, bleMacBytes)
                    serviceStep = 0

                    runOnUiThread {
                        setStandardUI()
                        Utils.showToastLong(this, getString(R.string.wait_45_secs))
                    }

                    Handler(mainLooper).postDelayed({
                        Utils.showToastLong(this, getString(R.string.wait_20_secs))
                    }, 20_000)

                    Handler(mainLooper).postDelayed({
                        val dialog = ConfigInfoDialog(100)
                        dialog.show(supportFragmentManager, null)
                        // Se ejecuta en otro hilo
                        runOnUiThread {(actualFragment as ConfigTestCooler).testFinished()}
                        configAndTesting = false
                    }, 45_000)


                } else {
                    CirCommands.closeAtSocketCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                }
            }
        }
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



    /************************************************************************************************/
    /**     BLE SERVICE                                                                             */
    /************************************************************************************************/
    override fun characteristicChanged(characteristic: BluetoothGattCharacteristic) {
        Log.d(TAG, "characteristicChanged: ${characteristic.value.toCharString()}")

        commandState(
            cirService.getCurrentState(),
            characteristic.value,
            CirWirelessParser.receivedCommand(characteristic.value)
        )
    }

    override fun characteristicRead(characteristic: BluetoothGattCharacteristic) {
         Log.e(TAG, "characteristicRead: ${characteristic.value.toHex()}")

        val uuidCharacteristic : String = characteristic.uuid.toString()
        val value : ByteArray = characteristic.value

        if (uuidCharacteristic == BleConstants.QUICK_COMMANDS_CHARACTERISTIC) {
            when {
                cirService.getCurrentState() == StateMachine.RELOADING_FRIDGE -> {
                    wasReloadSuccess(
                        cirService.getCurrentState(),
                        value
                    )
                }
                cirService.getCurrentState() == StateMachine.UPDATING_DATE -> {
                    val updated = wasUpdatedSuccess(
                        cirService.getCurrentState(),
                        value
                    )

                    when {
                        updated -> {
                            runOnUiThread{ toast(getString(R.string.updated_date)) }
                            cirService.setCurrentState(StateMachine.POLING)
                            maxTimes = 0
                            fetchLinkPost()
                        }

                        maxTimes < 5 -> {
                            (actualFragment as MainFragment).updateCirDate()
                        }

                        else -> {
                            runOnUiThread{ toast(getString(R.string.error_date)) }
                            cirService.setCurrentState(StateMachine.POLING)
                        }
                    }

                }
                else -> {
                    wasLockSuccess(
                        cirService.getCurrentState(),
                        value
                    )

                }
            }
        } else {
            cirService.extractFirmwareData(
                service!!,
                cirService.getCharacteristicDeviceInfo()!!,
                cirService.getNotificationDescriptor()!!
            )
        }

    }

    override fun characteristicWrite(characteristic: BluetoothGattCharacteristic) {
       // Log.e(TAG, "characteristicWrite: ${characteristic.value.toCharString()}")

        val uuidCharacteristic : String = characteristic.uuid.toString()
        val value : ByteArray = characteristic.value

        if (uuidCharacteristic == BleConstants.QUICK_COMMANDS_CHARACTERISTIC) {
            quickCommandState(
                cirService.getCurrentState(),
                value,
                CirWirelessParser.receivedCommand(value)
            )

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun connectionStatus(status: DisconnectionReason, newState: ConnState) {
        // Log.e(TAG, "connectionStatus: $newState")

        // newState solo puede ser CONNECTED o DISCONNECTED
        when (newState) {
            ConnState.CONNECTED -> {
                connectedDevice()
                triesConnect = 0
            }
            else                -> errorConnection(status) /*{
                Log.e(TAG, "triesConnect: $triesConnect")
                if (triesConnect > 15) {
                    triesConnect = 0
                    errorConnection(status)
                } else {
                    Log.e(TAG, "Trying to connect")
                    triesConnect++
                    Handler(mainLooper).postDelayed({
                        service!!.connectBleDevice(bleDevice)
                    }, 1000)

                }
            }*/
        }
    }

    override fun descriptorWrite() {
        cirService.descriptorFlag(service!!, cirService.getNotificationDescriptor()!!)
    }

    override fun mtuChanged() {
        cirService.getFirmwareData(service!!)
        CirCommands.updateDate(service!!, cirService.getQuickCommandsCharacteristic()!!, bleMacBytes)
        CirCommands.readDate(service!!, cirService.getQuickCommandsCharacteristic()!!, bleMacBytes)
        cirService.setCurrentState(StateMachine.UPDATING_DATE)
    }

    override fun servicesDiscovered(gatt: BluetoothGatt) {
        val services = gatt.services
        for (serv in services.iterator()) {
            cirService.apply {
                getCommCharacteristics(serv)
                getInfoCharacteristics(serv)
            }
        }

        service!!.setMtu(300)
    }


    /************************************************************************************************/
    /**     BLE SERVICE EXTENSIONS                                                                  */
    /************************************************************************************************/
    /**
     * ## commandState
     * Recibe la respuesta del dispositivo y su estado actual en la
     * máquina de estados, para poder dar un flujo adecuado
     *
     * @param state Estado actual (Máquina de estados)
     * @param response Respuesta en bytes del dispositivo
     * @param command Tipo de respuesta recibida
     */
    private fun commandState(state: StateMachine,
                             response: ByteArray,
                             command: ReceivedCmd) {
//        Log.e(TAG, "macBytes: ${bleMacBytes.toHex()}")
        Log.e(TAG, "commandState: $state -> $command -> ${response.toHex()} -> ${response.toString()}")

        when (state) {

            // STATUS DE POLEO
            StateMachine.POLING -> {
                // Habilitamos la pantalla cuando se inicia el poleo
                // Quiere decir que el dispositivo está listo para recibir comandos
                /*
                This was causing that buffer in Cir Wireless get full
                if (!isRefreshed)
                    CirCommands.initCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                */

                if (command == ReceivedCmd.POLEO) {
                    isRefreshed = true
                    runOnUiThread { setStandardUI() }
                    if (firstTime && actualFragment == wifiFragment) {
                        firstTime = false
                    }

                    if (actualFragment == mainFragment) {
                        (actualFragment as MainFragment).deviceConnected()
                    }
                }
                cipStatusMode = -1
            }

            StateMachine.GET_CLIENT -> {
                showWaitDialog()

                Log.e(TAG, "commandState: GET_CLIENT")

                cirService.setCurrentState(StateMachine.SETTING_REPOSITORY_URL)
                repositoryModel = RepositoryModel(1) // Esto se debe de cambiar por el cliente que nos de la tarjeta
                // TODO("IMPLEMENTAR OBTENER CLIENTE MEDIANTE EL PROTOCOLO DE POLEO")
            }

            StateMachine.GET_FIRMWARE_WIFI_MODULE -> {
                getFirmwareModule(response, command)
            }

            StateMachine.SETTING_REPOSITORY_URL -> {
                Log.e(TAG, "commandState: SETTING_REPOSITORY_URL")
                isRepositoryUrlUpdated(response, command)
            }

            StateMachine.SETTING_FIRMWARE_PATH -> {
                isFirmwarePathUpdated(response, command)
            }

            StateMachine.SETTING_FIRMWARE_VERSION -> {
                isFirmwareVersionUpdated(response, command)
            }

            // SE MUESTRA QUE TIPO DE CONFIGURACION SE DESEA REALIZAR
            StateMachine.SHOW_CONFIG_MODES -> {
                val dialog = ConfigSelectorDialog()
                dialog.show(supportFragmentManager, TAG)
                cirService.setCurrentState(StateMachine.POLING)

            }

            StateMachine.SHOW_PARAMETERS_STATIC_IP -> {
                val dialog = IpConfigValuesDialog()
                dialog.show(supportFragmentManager, TAG)
                cirService.setCurrentState(StateMachine.POLING)
            }

            StateMachine.SETTING_MODE_IP_CONFIG -> {
                isIpModeConfigOk(response, command)
            }


            StateMachine.SETTING_STATIC_IP_VALUES -> {
                areIpConfigValuesOk(response, command)
            }


            // STATUS DE MAC'S DE AP'S QUE EL DISPOSITIVO VE
            StateMachine.GET_AP -> {
//                Log.e(TAG, "getAp: $getApLaunched")

                if (command == ReceivedCmd.GET_AP) {
                    getApLaunched = true

                    // Casteamos el resultado y navegamos al fragmento de AP's
                    deviceMacList = CirCommands.fromResponseGetMacList(response)

                    // Log.e(TAG, "DEVICE_MAC_LIST: $deviceMacList")
                    if (actualFragment != wifiFragment) navigateTo(wifiFragment, true, null)

                    else wifiFragment.scanWifi()

                    actualFragment = wifiFragment

                    dismissWaitDialog()
                }

                if (command == ReceivedCmd.POLEO && !getApLaunched) {
//                    Log.e(TAG, "GETTING_AP: COMMAND: ${CommandUtils.getAccessPointsCmd().toHex()}")
                    CirCommands.getMacListCmd(service!!, cirService.getCharacteristicWrite()!!)
                }

                if (actualFragment == wiFiPasscodeFragment) {

                    navigateTo(wiFiPasscodeFragment, true, null)

                } else if (actualFragment == configTestCooler) {

                    navigateTo(configTestCooler, true, null)
                }
            }

            // STATUS DE CONFIGURACIÓN WIFI
            StateMachine.WIFI_CONFIG        -> { wifiConfigProcess(response, command, wifiStep) }

            // TESTING CONNECTION MACHINE *************************************************************
            StateMachine.UNKNOWN            -> { checkStatus(response, command) }
            StateMachine.SET_MODE           -> { checkModeSetted(command) }
            StateMachine.GET_CONFIG_AP      -> { getSsidFromResponse(response, command) }
            StateMachine.GET_STATUS_AP      -> { getApStatusFromAT(response, command) }
            StateMachine.GET_IP             -> { getIpFromAt(response, command) }
            StateMachine.PING               -> { getPing(response, command) }
            StateMachine.DATA_CONNECTION    -> { getDataConnection(response, command, serviceStep) }
            // ****************************************************************************************

            else -> { /* Ignoramos la respuesta */ }
        }
    }

    private fun wasReloadSuccess (state: StateMachine, value: ByteArray) {
        // Log.e(TAG, "Value RELOAD: ${value.toHex()}")

        when (CirWirelessParser.reloadResponse(value)) {
            ReceivedCmd.RELOAD_OK -> {
                runOnUiThread{ toast(getString(R.string.reload_enabled)) }
            }

            ReceivedCmd.RELOAD_NOT_ENABLED -> {
                runOnUiThread{ toast(getString(R.string.reload_not_available)) }

            }
            else -> { runOnUiThread{ toast(getString(R.string.error_occurred)) } }
        }

        cirService.setCurrentState(StateMachine.POLING)
    }

    private fun wasUpdatedSuccess (state: StateMachine, value: ByteArray) : Boolean {
        // Log.e(TAG, "Value DATE: ${value.toHex()}")
        var updated = false
        when (CirWirelessParser.dateUpdateResponse(value)) {
            ReceivedCmd.DATE_UPDATED -> {
                updated = true
            }

            else -> {
                updated = false
            }
        }

        return updated
    }


    private fun fetchLinkPost () {
        val apiClient = ApiClient()
        // Pass the token as parameter
        apiClient.getApiService(this).fetchLinkPost(token = "Bearer $token", bleMac)
            .enqueue(object : retrofit2.Callback <TransmitPostResponse> {
                override fun onFailure(call: Call<TransmitPostResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure")
                }

                override fun onResponse(call: Call<TransmitPostResponse>, response: Response<TransmitPostResponse>) {
                    Log.e(TAG, "onResponse: ${response.body()}")
                    val body = response.body()
                    if (body != null) {
//                        Log.e(TAG, "onResponse: ${response.body()!!.status}")
                        val status = body.status
                        if (status == "OK") {
                            showTransmitionOkDialog()
                        } else {
                            showTransmitionNotOkDialog()
                        }
                    }

                }
            })
    }


    private fun showTransmitionOkDialog () {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.login_error_dialog, -1,
            getString(R.string.right_config),
            getString(R.string.well_configured_message),
            getString(R.string.accept),
            getString(R.string.accept),
            View.GONE,
            View.VISIBLE
        )


        val dialog = GenericDialogButtons(
            this@RootActivity,
            baseDialogModel,
            object : DialogInteractor {
                override fun positiveClick(dialog: GenericDialogButtons) {}
                override fun negativeClick(dialog: GenericDialogButtons) {
                    dialog.dismiss()
                }
            })

        dialog.show()
    }

    internal fun showCancelProcessDialog () {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.generic_dialog_two_btns, R.drawable.router,
            getString(R.string.cancel_process),
            getString(R.string.cancel_process_ask),
            getString(R.string.yes),
            getString(R.string.no),
            View.VISIBLE,
            View.VISIBLE
        )

        val dialog = GenericDialogButtons(
            this@RootActivity,
            baseDialogModel,
            object : DialogInteractor {
                override fun positiveClick(dialog: GenericDialogButtons) {
                    cirService.setCurrentState(StateMachine.POLING)
                    configAndTesting = false
                    configTestCooler.btnNext.text = getString(R.string.next)
                    configTestCooler.stopAnimRouter()
                    configTestCooler.stopAnimCloud()
                    configTestCooler.currentState(getString(R.string.process_stopped))
                    dialog.dismiss()
                }

                override fun negativeClick(dialog: GenericDialogButtons) {
                    dialog.dismiss()
                }
            })

        dialog.show()
    }


    private fun showWifiProbeConfig () {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.generic_dialog_two_btns, R.drawable.ic_chip,
            getString(R.string.test_connection),
            getString(R.string.click_to_probe),
            getString(R.string.probe),
            getString(R.string.cancel),
            View.VISIBLE,
            View.VISIBLE
        )


        val dialog = GenericDialogButtons(
            this@RootActivity,
            baseDialogModel,
            object : DialogInteractor {
                override fun positiveClick(dialog: GenericDialogButtons) {
                    (actualFragment as AccessPointsFragment).testConnection()
                    dialog.dismiss() }
                override fun negativeClick(dialog: GenericDialogButtons) {
                    dialog.dismiss()
                }
            })

        dialog.show()
    }


    private fun showWifiOkDialog () {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.generic_dialog_two_btns, R.drawable.ic_img_lock,
            getString(R.string.ok_wifi),
            getString(R.string.tv_wifi_ok),
            getString(R.string.accept),
            getString(R.string.accept),
            View.GONE,
            View.VISIBLE
        )


        val dialog = GenericDialogButtons(
            this@RootActivity,
            baseDialogModel,
            object : DialogInteractor {
                override fun positiveClick(dialog: GenericDialogButtons) {}
                override fun negativeClick(dialog: GenericDialogButtons) {
                    showWifiProbeConfig()
                    dialog.dismiss()
                }
            })

        dialog.show()
    }


    private fun showWifiBadDialog () {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.generic_dialog_two_btns,
            R.drawable.ic_img_lock_red,
            getString(R.string.nok_wifi),
            getString(R.string.tv_wifi_nok),
            getString(R.string.accept),
            getString(R.string.accept),
            View.GONE,
            View.VISIBLE
        )


        val dialog = GenericDialogButtons(
            this@RootActivity,
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
            this@RootActivity,
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


    private fun wasLockSuccess (state: StateMachine, value: ByteArray) {
        // Log.e(TAG, "Date Response: ${value.toHex()}")
        when (CirWirelessParser.lockResponse(value)) {
            ReceivedCmd.LOCK_OK -> {
                when (state) {
                    StateMachine.OPENNING_LOCK -> {
                        runOnUiThread{ toast(getString(R.string.lock_open)) }
                    }

                    StateMachine.CLOSING_LOCK -> {
                        runOnUiThread { toast(getString(R.string.lock_close)) }
                    }

                    else -> { runOnUiThread{ toast(getString(R.string.error_occurred)) } }
                }
            }

            ReceivedCmd.LOCK_NOT_ENABLED -> {
                runOnUiThread { toast(getString(R.string.lock_disabled)) }
            }
            else -> { runOnUiThread{ toast(getString(R.string.error_occurred)) } }
        }

        cirService.setCurrentState(StateMachine.POLING)
    }

    private fun quickCommandState (state: StateMachine,
                                   response: ByteArray,
                                   command: ReceivedCmd) {
        // Log.e(TAG, "quickCommandState: $state")
        when (state) {
            StateMachine.CLOSING_LOCK,
            StateMachine.OPENNING_LOCK -> {
                readLockResponse()
            }

            StateMachine.RELOADING_FRIDGE -> {
                readReloadResponse()
            }

            StateMachine.UPDATING_DATE -> {
                readUpdatedDate()
            }
            else -> { /* nothing here */}
        }
    }


    @SuppressLint("MissingPermission")
    private fun readLockResponse () {
        // Log.e(TAG, "readLockResponse: ")
        service!!.bleGatt!!.readCharacteristic(cirService.getQuickCommandsCharacteristic())
    }


    @SuppressLint("MissingPermission")
    private fun readReloadResponse () {
        service!!.bleGatt!!.readCharacteristic(cirService.getQuickCommandsCharacteristic())
    }


    @SuppressLint("MissingPermission")
    private fun readUpdatedDate () {
        service!!.bleGatt!!.readCharacteristic(cirService.getQuickCommandsCharacteristic())
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
                override fun onFailure(call: Call <LinkPostResponse>, t: Throwable) {
                    // Error fetching posts
                    Log.e(TAG, "onFailure: ${t.message}")
                    runOnUiThread { Utils.showToastShort(applicationContext, getString(R.string.link_device_error)) }
                }

                override fun onResponse(call: Call<LinkPostResponse>, response: Response <LinkPostResponse>) {
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

    private fun checkLocation(){
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) showAlertLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun showAlertLocation() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Your location settings is set to Off, Please enable location to use this application")
        dialog.setPositiveButton("Settings") { _, _ ->
            val myIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            finish()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    /************************************************************************************************/
    /**     BLE SERVICE CONNECTION                                                                  */
    /************************************************************************************************/
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, xservice: IBinder?) {
            // Obtenemos la instancia del servicio una vez conectado
            service = (xservice as (BleService.LocalBinder)).getService()
            service!!.apply {
                registerActivity(this@RootActivity)
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
        private val TAG = RootActivity::class.java.simpleName
        var enableWifiConfig        = true

        private var retryAtResponse         = 0
        private var serviceStep             = 0
        private var wifiStep                = 0
        private var cipStatusMode           = -1

        // Timeouts de la actividad
        private const val UI_TIMEOUT        = 500L
        private const val MAX_AT_RETRY      = 2
    }
}
