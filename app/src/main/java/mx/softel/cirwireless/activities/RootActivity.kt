package mx.softel.cirwireless.activities

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.scanning_mask.*
import mx.softel.bleservicelib.BleService
import mx.softel.bleservicelib.enums.ConnState
import mx.softel.bleservicelib.enums.DisconnectionReason
import mx.softel.cirwireless.R
import mx.softel.cirwireless.dialogs.ConfigInfoDialog
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
import mx.softel.cirwirelesslib.extensions.hexStringToByteArray
import mx.softel.cirwirelesslib.extensions.toCharString
import mx.softel.cirwirelesslib.extensions.toHex
import mx.softel.cirwirelesslib.utils.BleCirWireless
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.cirwirelesslib.utils.CirWirelessParser

class RootActivity : AppCompatActivity(),
                     FragmentNavigation,
                     PasswordDialog.OnDialogClickListener,
                     WifiOkDialog.OnWifiDialogListener,
                     BleService.OnBleConnection {

    // BLUETOOTH DEVICE
    internal lateinit var bleDevice     : BluetoothDevice
    internal lateinit var bleMac        : String

    // COMMANDS DATA
    internal lateinit var ssidSelected  : String
    private  lateinit var passwordTyped : String
    internal lateinit var bleMacBytes   : ByteArray

    // VALORES PARA FRAGMENT DE TESTER
    internal var ipAssigned         : String        = ""
    internal var apAssigned         : Boolean       = false
    internal var ssidAssigned       : String        = ""
    internal var rssiAssigned       : String        = ""
    internal var pingAssigned       : Boolean       = false
    internal var dataAssigned       : Boolean       = false

    // SERVICE CONNECTIONS / FLAGS
    internal var service            : BleService?    = null
    internal var cirService         : BleCirWireless = BleCirWireless()
    private  var isServiceConnected : Boolean        = false
    private  var isRefreshed        : Boolean        = false
    private  var isScanning         : Boolean        = false
    private  var isWifiConnected    : Boolean        = false
    private  var getApLaunched      : Boolean        = false

    // VARIABLES DE FLUJO
    private  var disconnectionReason: DisconnectionReason   = DisconnectionReason.UNKNOWN
    internal var deviceMacList      : ArrayList<String>?    = null
    internal var actualFragment     : Fragment?             = null
    private  val mainFragment       : MainFragment          = MainFragment.getInstance()
    internal val testerFragment     : TesterFragment        = TesterFragment.getInstance()
    private  val wifiFragment       : AccessPointsFragment  = AccessPointsFragment.getInstance()

    // HANDLERS / RUNNABLES
    private val handler               = Handler()
    private val disconnectionRunnable = Runnable { finishAndDisconnectActivity(disconnectionReason.status) }


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
        doBindService()     // Asociamos el servicio de BLE
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
            toast("Espere un momento")
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
            background = getDrawable(R.color.hardMask)
            setOnClickListener { toast("Espere un momento") }
        }
        pbScanning.visibility = View.VISIBLE
        isScanning = true
    }

    /**
     * ## setStandardUI
     * Elimina de la vista la máscara de bloqueo de escaneo
     */
    internal fun setStandardUI() {
        scanMask.visibility = View.GONE
        pbScanning.visibility = View.GONE
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
        val data = intent.extras!!
        bleDevice       = data[EXTRA_DEVICE] as BluetoothDevice
        bleMac          = data.getString(EXTRA_MAC)!!
        bleMacBytes     = bleMac.hexStringToByteArray()
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
     * ## dialogAccept
     * Se ejecuta al hacer click en "Aceptar" del diálogo de Password
     *
     * @param password Texto de password ingresado por el usuario
     */
    override fun dialogAccept(password: String) {

        toast("Configurando el WIFI del dispositivo", Toast.LENGTH_LONG)
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
        toast("Cancelado")
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
        when (command) {
            ReceivedCmd.AT_READY -> {
                when (step) {
                    0   -> parseOkWifiConfigured(response.toCharString(), 100)
                    100 -> parseOkWifiConfigured(response.toCharString(), 1)
                    1   -> parseOkWifiConfigured(response.toCharString(), 101)
                    101 -> parseOkWifiConfigured(response.toCharString(), 2)
                    2   -> parseWifiConfigured  (response.toCharString())
                    3   -> parseOkWifiConfigured(response.toCharString(), 0)
                }
            }
            else -> {
                CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
            }
        }
    }

    private fun checkStatus(response: ByteArray, command: ReceivedCmd) {
        Log.d(TAG, "CHECK STATUS")
        if (cipStatusMode == -1) {
            CirCommands.checkCipStatusCmd(service!!, cirService.getCharacteristicWrite()!!,bleMacBytes)
            cipStatusMode = -2
        }
        when (command) {
            ReceivedCmd.AT_READY -> parseStatus(response.toCharString())
            else -> {
                CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
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
            ReceivedCmd.WAIT_AP     -> CirCommands.getInternalWifiCmd(service!!,cirService.getCharacteristicWrite()!!, bleMacBytes)
            ReceivedCmd.AT_READY    -> parseSsidResponse(response.toCharString())
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
            ReceivedCmd.WAIT_AP     -> CirCommands.sendIpAtCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            ReceivedCmd.AT_READY    -> parseIpResponse(response.toCharString())
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
            ReceivedCmd.WAIT_AP     -> CirCommands.sendApConnectionCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
            ReceivedCmd.AT_READY    -> parseApResponse(response.toCharString())
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
            ReceivedCmd.WAIT_AP     -> CirCommands.sendPing(service!!, cirService.getCharacteristicWrite()!!, "www.google.com", bleMacBytes)
            ReceivedCmd.AT_READY    -> parsePingResponse(response.toCharString())
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
            ReceivedCmd.AT_OK       -> CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
            ReceivedCmd.AT_READY    -> parseDataResponse(response, step)
            ReceivedCmd.WAIT_AP     -> {
                when (step) {
                    0 -> CirCommands.closeAtSocketCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                    3 -> CirCommands.sendStatusWifiCmd(service!!, cirService.getCharacteristicWrite()!!)
                }
            }
            else -> {
                when (step) {
                    3       -> CirCommands.sendStatusWifiCmd(service!!, cirService.getCharacteristicWrite()!!)
                    else    -> CirCommands.readAtResponseCmd(service!!, cirService.getCharacteristicWrite()!!)
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
     * @param response Cadena de respuesta del comando AT
     */
    private fun parseWifiConfigured(response: String) {
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
     * @param response Cadena de respuesta del comando AT
     * @param nextStep Estado siguiente de la máquina de estados
     */
    private fun parseOkWifiConfigured(response: String, nextStep: Int) {
        if (response.contains(AT_CMD_OK)) {
            when (nextStep) {
                0 -> {
                    cirService.setCurrentState(StateMachine.POLING)
                    runOnUiThread { setStandardUI() }
                    val dialog: DialogFragment
                            = if (isWifiConnected) WifiOkDialog.getInstance()
                              else WifiNokDialog.getInstance()
                    dialog.show(supportFragmentManager, null)
                }
                100 -> CirCommands.resetWifiCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                1   -> CirCommands.setInternalWifiCmd(service!!, cirService.getCharacteristicWrite()!!, ssidSelected, passwordTyped, AT_NO_SEND_SSID, bleMacBytes)
                101 -> CirCommands.setAutoConnCmd(service!!, cirService.getCharacteristicWrite()!!, 1, bleMacBytes)
                2   -> CirCommands.sendConfigureWifiCmd(service!!, cirService.getCharacteristicWrite()!!, ssidSelected, passwordTyped, bleMacBytes)
            }
            wifiStep = nextStep
        } else {
            wifiStep = 0
            parseOkWifiConfigured(response, wifiStep)
        }
    }

    private fun parseStatus(response: String) {
        if (response.contains(AT_CMD_STATUS)) {

            if (response.contains("TCP")) {
                CirCommands.closeAtSocketCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                return
            }
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
     * @param response Cadena de respuesta del comando AT
     */
    private fun parseSsidResponse(response: String) {
        if (response.contains(AT_CMD_OK)) {
            ssidAssigned = response
                .substringAfter(SSID_SUBSTRING_AFTER)
                .substringBefore("\",")
            runOnUiThread { testerFragment.fragmentUiUpdate(1) }
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
     * @param response Cadena de respuesta del comando AT
     */
    private fun parseApResponse(response: String) {
        if (response.contains(WIFI_SUBSTRING_AP_AFTER)) {
            rssiAssigned = response
                .substringAfterLast(",-")
                .substringBefore("OK")
                .substringBefore(",0")
                .replace("\r", "")
                .replace("\n", "")
            rssiAssigned = "-$rssiAssigned"
            runOnUiThread { testerFragment.fragmentUiUpdate(2) }
            cirService.setCurrentState(StateMachine.GET_IP)
        } else {
            if (retryAtResponse >= 2) {
                retryAtResponse = 0
                rssiAssigned = "No conectado"
                runOnUiThread {
                    testerFragment.fragmentUiUpdate(2)
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
     * @param response Cadena de respuesta del comando AT
     */
    private fun parseIpResponse(response: String) {
        if (response.contains(WIFI_NOT_IP_STRING)){
            if (retryAtResponse >= MAX_AT_RETRY) {
                retryAtResponse = 0
                ipAssigned = "IP No asignada"
                apAssigned = false
                runOnUiThread {
                    testerFragment.fragmentUiUpdate(3)
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
        } else {
            retryAtResponse = 0
            var ipRead = response.substringAfter(WIFI_SUBSTRING_IP_AFTER)
            ipRead = ipRead
                .substringBefore(WIFI_SUBSTRING_IP_BEFORE)
                .replace("\"", "")
                .replace("\n", "")
                .replace("\r", "")
            ipAssigned = ipRead
            apAssigned = true
            cirService.setCurrentState(StateMachine.PING)
            runOnUiThread { testerFragment.fragmentUiUpdate(3) }
        }
    }

    /**
     * ## parsePingResponse
     * Analiza la respuesta obtenida por el comando AT, y determina
     * si el PING se completó de manera exitosa o no
     *
     * @param response Cadena de respuesta del comando AT
     */
    private fun parsePingResponse(response: String) {
        pingAssigned = (response.contains(PING_OK) && !response.contains(AT_CMD_ERROR))
        runOnUiThread { testerFragment.fragmentUiUpdate(4) }
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
        val restring = response.toCharString()
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
                        testerFragment.fragmentUiUpdate(5)
                        setStandardUI()
                    }
                    val dialog = ConfigInfoDialog(4)
                    dialog.show(supportFragmentManager, null)
                    return
                }
                serviceStep = 2
                runOnUiThread { testerFragment.fragmentUiUpdate(5) }
            }
            2 -> {
                if (restring.contains(AT_CMD_CLOSED) || restring.contains(AT_CMD_ERROR)) {
                    cirService.setCurrentState(StateMachine.POLING)
                    CirCommands.setDeviceModeCmd(service!!, cirService.getCharacteristicWrite()!!, AT_MODE_SLAVE, bleMacBytes)
                    serviceStep = 0
                    runOnUiThread {
                        setStandardUI()
                    }
                    val dialog = ConfigInfoDialog(100)
                    dialog.show(supportFragmentManager, null)
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
        //Log.d(TAG, "characteristicChanged: ${characteristic.value.toCharString()}")

        commandState(
            cirService.getCurrentState(),
            characteristic.value,
            CirWirelessParser.receivedCommand(characteristic.value)
        )
    }

    override fun characteristicRead(characteristic: BluetoothGattCharacteristic) {
        Log.d(TAG, "characteristicRead: ${characteristic.value.toHex()}")

        cirService.extractFirmwareData(
            service!!,
            cirService.getCharacteristicDeviceInfo()!!,
            cirService.getNotificationDescriptor()!!
        )
    }

    override fun characteristicWrite(characteristic: BluetoothGattCharacteristic) {
        /* Nada que hacer por aquí*/
        Log.i(TAG, "characteristicWrite: ${characteristic.value.toCharString()}")
    }

    override fun connectionStatus(status: DisconnectionReason, newState: ConnState) {
        Log.d(TAG, "connectionStatus: $newState")

        // newState solo puede ser CONNECTED o DISCONNECTED
        when (newState) {
            ConnState.CONNECTED -> connectedDevice()
            else                -> errorConnection(status)
        }
    }

    override fun descriptorWrite() {
        cirService.descriptorFlag(service!!, cirService.getNotificationDescriptor()!!)
    }

    override fun mtuChanged() {
        cirService.getFirmwareData(service!!)
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
        Log.e(TAG, "commandState: $state -> $command -> ${response.toHex()} -> ${response.toCharString()}")
        when (state) {

            // STATUS DE POLEO
            StateMachine.POLING -> {
                // Habilitamos la pantalla cuando se inicia el poleo
                // Quiere decir que el dispositivo está listo para recibir comandos
                if (!isRefreshed) CirCommands.initCmd(service!!, cirService.getCharacteristicWrite()!!, bleMacBytes)
                if (command == ReceivedCmd.POLEO) {
                    isRefreshed = true
                    runOnUiThread { setStandardUI() }
                }
                cipStatusMode = -1
            }

            // STATUS DE MAC'S DE AP'S QUE EL DISPOSITIVO VE
            StateMachine.GET_AP -> {
                if (!getApLaunched) {
                    CirCommands.getMacListCmd(service!!, cirService.getCharacteristicWrite()!!)
                    getApLaunched = true
                }
                if (command == ReceivedCmd.GET_AP) {
                    // Casteamos el resultado y navegamos al fragmento de AP's
                    deviceMacList = CirCommands.fromResponseGetMacList(response)
                    if (actualFragment != wifiFragment) navigateTo(wifiFragment, true, null)
                    else wifiFragment.scanWifi()
                    actualFragment = wifiFragment
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
        when (reason) {
            DisconnectionReason.NOT_AVAILABLE, DisconnectionReason.FAILURE -> {
                runOnUiThread { toast("Ocurrió un error") }
                handler.apply { postDelayed(disconnectionRunnable, UI_TIMEOUT) }
            }
            DisconnectionReason.CONNECTION_FAILED -> {
                runOnUiThread { toast("Tiempo de espera agotado, desconectando") }
                handler.apply { postDelayed(disconnectionRunnable, UI_TIMEOUT) }
            }
            DisconnectionReason.FIRMWARE_UNSUPPORTED -> {
                runOnUiThread { toast("Dispositivo no soportado") }
                handler.apply { postDelayed(disconnectionRunnable, UI_TIMEOUT) }
            }
            else -> {
                runOnUiThread { toast("Desconectando el dispositivo") }
                handler.apply {
                    postDelayed(disconnectionRunnable, UI_TIMEOUT)
                }
            }
        }
    }

    /**
     * ## connectedDevice
     * Notifica en pantalla que el dispositivo fue correctamente conectado.
     * Manda a escanear los servicios que el dispositivo contiene, e inicializa
     * algunas características necesarias para la comunicación
     */
    private fun connectedDevice() {
        runOnUiThread { toast("Dispositivo conectado") }
        service!!.discoverDeviceServices()
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
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = RootActivity::class.java.simpleName

        private var retryAtResponse         = 0
        private var serviceStep             = 0
        private var wifiStep                = 0
        private var cipStatusMode           = -1

        // Timeouts de la actividad
        private const val UI_TIMEOUT        = 500L
        private const val MAX_AT_RETRY      = 2
    }
}
