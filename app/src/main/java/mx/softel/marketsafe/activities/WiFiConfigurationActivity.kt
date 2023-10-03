package mx.softel.marketsafe.activities

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.scanning_mask.*
import mx.softel.bleservicelib.BleService
import mx.softel.bleservicelib.enums.DisconnectionReason
import mx.softel.cirwirelesslib.constants.*
import mx.softel.cirwirelesslib.enums.StateMachine
import mx.softel.cirwirelesslib.extensions.hexStringToByteArray
import mx.softel.cirwirelesslib.utils.BleCirWireless
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.marketsafe.R
import mx.softel.marketsafe.RepositoryModel
import mx.softel.marketsafe.UserPermissions
import mx.softel.marketsafe.dialogs.*
import mx.softel.marketsafe.extensions.toast
import mx.softel.marketsafe.fragments.*
import mx.softel.marketsafe.interfaces.FragmentNavigation
import mx.softel.scanblelib.extensions.toHexValue
import java.util.*

private const val TAG = "WiFiConfigurationActivi"

class WiFiConfigurationActivity :   AppCompatActivity(),
                                    FragmentNavigation,
                                    PasswordDialog.OnDialogClickListener,
                                    WifiOkDialog.OnWifiDialogListener,
                                    BleService.OnBleConnection,
                                    ConfigSelectorDialog.OnDialogClickListener {
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
        RootActivity.enableWifiConfig = isACirWifiBeacon(beaconId)
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
}