package mx.softel.marketsafe.activities

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_tab_main.*
import mx.softel.bleservicelib.BleService
import mx.softel.bleservicelib.enums.ConnState
import mx.softel.bleservicelib.enums.DisconnectionReason
import mx.softel.cirwirelesslib.constants.*
import mx.softel.cirwirelesslib.enums.ReceivedCmd
import mx.softel.cirwirelesslib.enums.StateMachine
import mx.softel.cirwirelesslib.extensions.hexStringToByteArray
import mx.softel.cirwirelesslib.extensions.toCharString
import mx.softel.cirwirelesslib.utils.BleCirWireless
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.cirwirelesslib.utils.CirWirelessParser
import mx.softel.marketsafe.R
import mx.softel.marketsafe.UserPermissions
import mx.softel.marketsafe.databinding.ActivityTabMainBinding
import mx.softel.marketsafe.extensions.toast
import mx.softel.marketsafe.fragments.HelpMainFragment
import mx.softel.marketsafe.fragments.HomeMainFragment
import mx.softel.marketsafe.fragments.SettingsMainFragment
import mx.softel.marketsafe.utils.Utils


class TabMainActivity : AppCompatActivity(), BleService.OnBleConnection {
    // SERVICE CONNECTIONS / FLAGS
    internal var service            : BleService?    = null
    internal var cirService         : BleCirWireless = BleCirWireless()
    private  var isServiceConnected : Boolean        = false
    internal var isConnected        : Boolean        = false


    // BLUETOOTH DEVICE
    internal lateinit var bleDevice     : BluetoothDevice
    internal lateinit var bleMac        : String
    internal lateinit var ssidSelected  : String
    internal lateinit var passwordTyped : String
    internal lateinit var bleMacBytes   : ByteArray
    private lateinit var token          : String
    internal var userPermissions        : UserPermissions? = null
    internal var isTransmiting          : Boolean = false
    internal lateinit var serialNumber  : String
    internal lateinit var assetType     : String
    internal lateinit var assetMode     : String
    internal var beaconBytes            : ByteArray? = null

    private lateinit var bottomNavigationView: BottomNavigationView

    private val homeFragment = HomeMainFragment.getInstance()
    private val helpFragment = HelpMainFragment.getInstance()
    private val settingsFragment = SettingsMainFragment.getInstance()

    private lateinit var binding: ActivityTabMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getAndSetIntentData()
        setTabListeners()
        loadFragment()
    }

    override fun onStart() {
        super.onStart()
        doBindService()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        doUnbindService()
    }

    private fun setTabListeners () {
        binding.bnvMenu.setOnNavigationItemSelectedListener(object : AdapterView.OnItemSelectedListener,
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val tabIconColor: Int = ContextCompat.getColor(applicationContext, R.color.azul40)

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.settings -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.flContainer, settingsFragment).commit()
                        return true
                    }
                    R.id.home -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.flContainer, homeFragment).commit()
                        return true
                    }
                    R.id.help -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.flContainer, helpFragment).commit()
                        return true
                    }
                }

                return false
            }
        })
    }

    private fun loadFragment () {
        binding.bnvMenu.selectedItemId = R.id.home
        supportFragmentManager.beginTransaction().replace(R.id.flContainer, homeFragment).commit()

    }

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
        ssidSelected    = data.getString(SSID)!!
        passwordTyped   = data.getString(SSID_PASSCODE)!!
        userPermissions = data.getSerializable(USER_PERMISSIONS) as UserPermissions
        beaconBytes = data[EXTRA_BEACON_BYTES] as ByteArray
        // val beaconId    = "0x${byteArrayOf(beaconBytes[5], beaconBytes[6]).toHexValue().toUpperCase(Locale.ROOT)}"
    }


    internal fun goToTestActivity () {
        val intent = Intent(this, RootActivity::class.java)
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

    /************************************************************************************************/
    /**     BLE SERVICE CONNECTION                                                                  */
    /************************************************************************************************/
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, xservice: IBinder?) {
            // Obtenemos la instancia del servicio una vez conectado
            service = (xservice as (BleService.LocalBinder)).getService()
            service!!.apply {
                registerActivity(this@TabMainActivity)
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

    private fun connectedDevice() {
        runOnUiThread {
            toast(getString(R.string.device_connected))
            homeFragment.goodConnection()
        }
        service!!.discoverDeviceServices()
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


    /************************************************************************************************/
    /**     BLE SERVICE                                                                             */
    /************************************************************************************************/
    override fun characteristicChanged(characteristic: BluetoothGattCharacteristic) {
        Log.d(TAG, "characteristicChanged: ${characteristic.value.toCharString()}")
    }

    override fun characteristicRead(characteristic: BluetoothGattCharacteristic) {
        // Log.e(TAG, "characteristicRead: ${characteristic.value.toHex()}")

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

    override fun connectionStatus(status: DisconnectionReason, newState: ConnState) {
        // Log.e(TAG, "connectionStatus: $newState")

        // newState solo puede ser CONNECTED o DISCONNECTED
        when (newState) {
            ConnState.CONNECTED -> {
                isConnected = true
                connectedDevice()
            }
            else                -> {
                Log.e(TAG, "DISCONNECTED")
                isConnected = false
                homeFragment.badConnection()
            }
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

    internal fun sendReloadCommand () {
        if (userPermissions!!.isCommander || userPermissions!!.isAdmin) {
            if (cirService.getQuickCommandsCharacteristic() == null)
                sendReloadCommand()
            else {
                apply {
                    CirCommands.reloadFridge(service!!, cirService.getQuickCommandsCharacteristic()!!, bleMacBytes)
                    cirService.setCurrentState(StateMachine.RELOADING_FRIDGE)
                }
            }
        } else
            Utils.showToastShort(this, getString(R.string.not_enough_permissions))
    }

    internal fun sendCloseLockCommand () {
        if (userPermissions!!.isCommander || userPermissions!!.isAdmin) {
            if (cirService.getQuickCommandsCharacteristic() == null)
                sendCloseLockCommand()
            else {
                toast(getString(R.string.locking))
                apply {
                    CirCommands.closeLock(service!!, cirService.getQuickCommandsCharacteristic()!!, bleMacBytes)
                    cirService.setCurrentState(StateMachine.CLOSING_LOCK)
                }
            }
        } else
            Utils.showToastShort(this, getString(R.string.not_enough_permissions))
    }

    internal fun sendOpenLockCommand () {
        if (userPermissions!!.isCommander || userPermissions!!.isAdmin) {
            if (cirService.getQuickCommandsCharacteristic() == null)
                sendOpenLockCommand()
            else {
                toast(getString(R.string.unlocking))
                apply {
                    CirCommands.openLock(service!!, cirService.getQuickCommandsCharacteristic()!!, bleMacBytes)
                    cirService.setCurrentState(StateMachine.OPENNING_LOCK)
                }
            }
        } else
            Utils.showToastShort(this, getString(R.string.not_enough_permissions))
    }


    /************************************************************************************************/
    /**     BLE SERVICE EXTENSIONS                                                                  */
    /************************************************************************************************/
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

    private fun readLockResponse () {
        // Log.e(TAG, "readLockResponse: ")
        service!!.bleGatt!!.readCharacteristic(cirService.getQuickCommandsCharacteristic())
    }


    private fun readReloadResponse () {
        service!!.bleGatt!!.readCharacteristic(cirService.getQuickCommandsCharacteristic())
    }


    private fun readUpdatedDate () {
        service!!.bleGatt!!.readCharacteristic(cirService.getQuickCommandsCharacteristic())
    }

    companion object {
        private const val TAG = "TabMainActivity"
    }
}