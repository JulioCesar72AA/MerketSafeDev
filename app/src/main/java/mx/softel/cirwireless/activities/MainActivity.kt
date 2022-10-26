package mx.softel.cirwireless.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.scanning_mask.*
import mx.softel.cirwireless.R
import mx.softel.cirwireless.adapters.ScanRecyclerAdapter
import mx.softel.cirwireless.extensions.toast
import mx.softel.cirwireless.log_in_module.web_service.ScanPermission
import mx.softel.cirwireless.wifi_db.WifiDatabase
import mx.softel.cirwirelesslib.constants.*
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.ble.BleManager
import org.json.JSONArray
import org.json.JSONObject


class MainActivity: AppCompatActivity(),
                    SwipeRefreshLayout.OnRefreshListener,
                    View.OnClickListener,
                    ScanRecyclerAdapter.OnScanClickListener,
                    PopupMenu.OnMenuItemClickListener {

    private var bleDevices = ArrayList<BleDevice>()
    private var isScanning = false
    private var checkingCloudPermissions = false

    private var db: WifiDatabase? = null


    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        srlScan.apply {
            setOnRefreshListener(this@MainActivity)
            setColorSchemeColors(getColor(R.color.colorAccent),
                                 getColor(R.color.colorIconBlue),
                                 getColor(R.color.colorPrimary))
        }

        db = WifiDatabase(this)

        bleDevices.clear()
        setScanningUI()
        setOnClick()
    }

    override fun onResume() {
        super.onResume()

        /**
         * Se añade el escaneo en el [onResume] porque solicita los permisos
         * de ubicación al usuario, si no los ha proporcionado hace el flujo
         * [onResume] -> [onPause] -> [onResume], es en el segundo instante
         * donde se ejecuta el escaneo al solicitar los permisos necesarios
         */
        scanDevices()
    }

    override fun onRefresh() {
        scanMask.visibility = View.VISIBLE
        scanDevices()

        // Detenemos el escaneo en pantalla
        Handler(mainLooper).postDelayed({
            srlScan.isRefreshing = false
        }, TIMEOUT)
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.scanMask -> {
                // Ignoramos el click para bloquear los demás elementos
                if (checkingCloudPermissions)
                    toast(R.string.scan_permissions_cloud)

                else
                    toast(R.string.tst_scanning)

            }
            R.id.ivMenu -> createMenu()
        }
    }

    private fun setOnClick() {
        scanMask.setOnClickListener(this)
        ivMenu  .setOnClickListener(this)
    }




    /************************************************************************************************/
    /**     MENU                                                                                    */
    /************************************************************************************************/
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_info -> startActivity(Intent(this, InfoActivity::class.java))
        }
        return true
    }


    private fun createMenu() {
        val popup = PopupMenu(this, ivMenu)
        popup.apply {
            menuInflater.inflate(R.menu.menu_version, popup.menu)
            setOnMenuItemClickListener(this@MainActivity)
            show()
        }
    }



    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    override fun onScanClickListener(position: Int) {


        val intent = Intent(this, RootActivity::class.java)
        intent.apply {
            val dev = bleDevices[position]
            putExtra(EXTRA_DEVICE,              dev.getBleDevice())
            putExtra(EXTRA_NAME,                dev.getName())
            putExtra(EXTRA_MAC,                 dev.getMac())
            putExtra(EXTRA_BEACON,              dev.getBeaconDeviceString())
            putExtra(EXTRA_BEACON_BYTES,        dev.getScanRecord()?.bytes)
            putExtra(EXTRA_BEACON_ENCRYPTED,    dev.getDeviceBeaconIsEncrypted())
            putExtra(EXTRA_BEACON_TYPE,         dev.getBeaconType())
            putExtra(EXTRA_IS_ENCRYPTED,        dev.isEncrypted())
            startActivity(this)
        }
    }


    /************************************************************************************************/
    /**     VISTA                                                                                   */
    /************************************************************************************************/
    private fun initUI() {
        pbScanning.visibility   = View.GONE
        scanMask.visibility     = View.GONE
        tvNoDevices.visibility  = View.GONE
    }


    private fun setPermissionScanUI () {
        pbScanning.visibility   = View.VISIBLE
        scanMask.visibility     = View.VISIBLE
        tvNoDevices.visibility  = View.GONE
    }


    private fun setScanningUI() {
        pbScanning.visibility   = View.VISIBLE
        scanMask.visibility     = View.VISIBLE
        tvNoDevices.visibility  = View.GONE
    }

    private fun setRecyclerUI() {
        rvBleList.apply {
            val manager     = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            layoutManager   = manager
            adapter         = ScanRecyclerAdapter(bleDevices, this@MainActivity)
        }

        initUI()
    }

    private fun setNoDataUI() {
        pbScanning.visibility   = View.GONE
        scanMask.visibility     = View.GONE
        tvNoDevices.visibility  = View.VISIBLE
    }


    /************************************************************************************************/
    /**     AUXILIARES                                                                              */
    /************************************************************************************************/
    private fun scanDevices() {
        isScanning = true
        val bleManager = BleManager(this, TIMEOUT)
        bleDevices.clear()
        bleManager.scanBleDevices { devices ->
            bleDevices = devices
            if (bleDevices.isEmpty()) {
                isScanning = false
                setNoDataUI()
            } else {

                isScanning = false
//                for (dev in devices) {
//                    setRecyclerUI()
//                }
                checkingCloudPermissions = true
                setPermissionScanUI()
                db!!.getUserToken {
                    val token = it as String
                    val scanPermission = ScanPermission(token)
                    val macsArray = JSONArray()
                    for (device in devices) {
                        Log.e(TAG, "MAC: ${device.getMac()}")
                        macsArray.put(device.getMac())
                    }
                    val body = JSONObject()
                    body.put("macs", macsArray)
                    Log.e(TAG, body.toString())
                    val response = scanPermission.checkMacsPermission(body.toString())
                    Log.e(TAG, response?.networkResponse.toString())
                    Log.e(TAG, response?.message.toString())

                }

            }
        }
    }



    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val TIMEOUT           = 5_000L
    }


}
