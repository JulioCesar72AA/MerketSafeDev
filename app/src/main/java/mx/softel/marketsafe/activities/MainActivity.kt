package mx.softel.marketsafe.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.util.TimeZone
import android.location.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.scanning_mask.*
import mx.softel.cirwirelesslib.constants.*
import mx.softel.marketsafe.CirDevice
import mx.softel.marketsafe.R
import mx.softel.marketsafe.UserPermissions
import mx.softel.marketsafe.adapters.ScanRecyclerAdapter
import mx.softel.marketsafe.dialog_module.GenericDialogButtons
import mx.softel.marketsafe.dialog_module.dialog_interfaces.DialogInteractor
import mx.softel.marketsafe.dialog_module.dialog_models.BaseDialogModel
import mx.softel.marketsafe.extensions.toast
import mx.softel.marketsafe.utils.Utils
import mx.softel.marketsafe.web_services_module.ui_login.log_in_dialog.DialogButtonsModel
import mx.softel.marketsafe.web_services_module.web_service.ApiClient
import mx.softel.marketsafe.web_services_module.web_service.LinkPostResponse
import mx.softel.marketsafe.web_services_module.web_service.ScanPostResponse
import mx.softel.marketsafe.wifi_db.WifiDatabase
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.ble.BleManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.time.ZoneId
import java.util.*


class MainActivity: AppCompatActivity(),
                    SwipeRefreshLayout.OnRefreshListener,
                    View.OnClickListener,
                    ScanRecyclerAdapter.OnScanClickListener,
                    PopupMenu.OnMenuItemClickListener {

    private lateinit var token          : String
    private lateinit var userPermissions: UserPermissions

    private var bleDevices                  = ArrayList<BleDevice>()
    private var cirDevice                   = ArrayList <CirDevice> ()
    private var isScanning                  = false
    private var checkingCloudPermissions    = false


    private var db: WifiDatabase? = null


    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = WifiDatabase(this)
        bleDevices.clear()
        cirDevice.clear()

        checkLocation()
        setScanningUI()
        setOnClick()
//
//        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            showAlertLocation()
//        }
//        else
//        {
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//            setScanningUI()
//            clearRecyclerView()
//            scanDevices()
////            letsScan()
//            setOnClick()
//        }
    }

    override fun onResume() {
        super.onResume()

        /**
         * Se añade el escaneo en el [onResume] porque solicita los permisos
         * de ubicación al usuario, si no los ha proporcionado hace el flujo
         * [onResume] -> [onPause] -> [onResume], es en el segundo instante
         * donde se ejecuta el escaneo al solicitar los permisos necesarios
         */
        clearRecyclerView()
        scanDevices()
    }


    private fun checkLocation(){
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) showAlertLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    private fun showAlertLocation() {
        val dialog = AlertDialog.Builder(this)
        val adMessage = getString(R.string.no_location_services)
        dialog.setMessage(adMessage)
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


    override fun onRefresh() {
        Log.e(TAG, "onRefresh: ")
        scanMask.visibility             = View.VISIBLE
        lavLoaderPositive.visibility    = View.VISIBLE
        scanningMask.visibility         = View.VISIBLE

        clearRecyclerView()
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
//            R.id.ivMenu -> createMenu()
        }
    }

    private fun setOnClick() {
        srlScan.setOnRefreshListener(this)
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


    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationRequest: LocationRequest
//    private lateinit var locationCallback: LocationCallback


    private fun showLinkDeviceDialog (mac: String) {
        val baseDialogModel: BaseDialogModel = DialogButtonsModel(
            R.layout.generic_dialog_two_btns, -1,
            getString(R.string.link_device_title),
            getString(R.string.link_device),
            getString(R.string.link),
            getString(R.string.cancel),
            View.VISIBLE,
            View.VISIBLE
        )

        val dialog = GenericDialogButtons(
            this,
            baseDialogModel,
            object : DialogInteractor {
                @SuppressLint("MissingPermission", "NewApi")
                override fun positiveClick(dialog: GenericDialogButtons) {
                    setPermissionScanUI()

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

                            body.put("mac", mac)
                            body.put("latitude", latitude)
                            body.put("longitude", longitude)
                            body.put("timezone_id", zone.toString())

                            val mediaType = "application/json; charset=utf-8".toMediaType()
                            val requestBody = body.toString().toRequestBody(mediaType)
                            Log.e(TAG, body.toString())
                            fetchLink(token, requestBody)
                            dialog.dismiss()
                        }
                }
                override fun negativeClick(dialog: GenericDialogButtons) {
                    dialog.dismiss()
                }
            }
        )

        dialog.show()
    }


    fun clearRecyclerView() {
        val cirDeviceSize = cirDevice.size
        cirDevice.clear()
        ScanRecyclerAdapter(applicationContext, cirDevice, this@MainActivity)
    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    override fun onCirSelected(cirSelected: CirDevice?, allowedDevice: Boolean) {
        try {

            if (!allowedDevice) {
                if (userPermissions.isLinker || userPermissions.isAdmin) {
                    showLinkDeviceDialog(cirSelected!!.bleDevice.getMac())

                } else
                    Utils.showToastLong(this, getString(R.string.connection_not_allowed))

            } else {
                val intent = Intent(this, RootActivity::class.java)
                intent.apply {
                    putExtra(EXTRA_DEVICE, cirSelected!!.bleDevice.getBleDevice())
                    putExtra(EXTRA_NAME, cirSelected.bleDevice.getName())
                    putExtra(EXTRA_MAC, cirSelected.bleDevice.getMac())
                    putExtra(EXTRA_BEACON, cirSelected.bleDevice.getBeaconDeviceString())
                    putExtra(EXTRA_BEACON_BYTES, cirSelected.bleDevice.getScanRecord()?.bytes)
                    putExtra(EXTRA_BEACON_ENCRYPTED, cirSelected.bleDevice.getDeviceBeaconIsEncrypted())
                    putExtra(EXTRA_BEACON_TYPE, cirSelected.bleDevice.getBeaconType())
                    putExtra(EXTRA_IS_ENCRYPTED, cirSelected.bleDevice.isEncrypted())

                    // Solkos' flags
                    putExtra(TOKEN, token)
                    putExtra(USER_PERMISSIONS, userPermissions)
                    putExtra(TRANSMITION, cirSelected.getScanPostResponse()!!.isTransmitting)
                    putExtra(SERIAL_NUMBER, cirSelected.getScanPostResponse()!!.serialNumber)
                    putExtra(ASSET_TYPE, cirSelected.getScanPostResponse()!!.assetType)
                    putExtra(ASSET_MODEL, cirSelected.getScanPostResponse()!!.assetModel)

                    startActivity(this)
                    finish()
                }
            }
        } catch (ex: Exception){
            Log.e(TAG, "ex.message: ${ex.message}")
            Utils.showToastShort(this, "ex.message: ${ex.message}" )
        }
    }


    /************************************************************************************************/
    /**     VISTA                                                                                   */
    /************************************************************************************************/
    private fun initUI() {
        pbScanning.visibility   = View.GONE
        scanMask.visibility     = View.GONE
        tvNoDevices.visibility  = View.GONE
        lavLoaderPositive.visibility = View.GONE
        scanningMask.visibility      = View.GONE
    }


    private fun setPermissionScanUI () {
        pbScanning.visibility   = View.INVISIBLE
        scanMask.visibility     = View.INVISIBLE
        tvNoDevices.visibility  = View.GONE
        lavLoaderPositive.visibility = View.VISIBLE
    }


    private fun setScanningUI() {
        pbScanning.visibility           = View.INVISIBLE
        lavLoaderPositive.visibility    = View.VISIBLE
        scanMask.visibility             = View.VISIBLE
        tvNoDevices.visibility          = View.GONE
        scanningMask.visibility         = View.VISIBLE
    }

    private fun setRecyclerUI() {
        rvBleList.apply {
            val manager     = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            layoutManager   = manager
            adapter         = ScanRecyclerAdapter(applicationContext, cirDevice, this@MainActivity)
        }

        initUI()
    }

    private fun setNoDataUI() {
        pbScanning.visibility           = View.GONE
        lavLoaderPositive.visibility    = View.GONE
        scanMask.visibility             = View.GONE
        tvNoDevices.visibility          = View.VISIBLE
        scanningMask.visibility         = View.GONE
    }


    /************************************************************************************************/
    /**     AUXILIARES                                                                              */
    /************************************************************************************************/
    private fun scanDevices() {
        if (!isScanning) {
            isScanning = true
            bleDevices.clear()
            cirDevice.clear()

            var bleManager = BleManager(this, TIMEOUT)

            bleManager.scanBleDevices { devices ->
                bleDevices = devices
                isScanning = false
                Log.e(TAG, "BLE Devices: ${bleDevices}")
                if (bleDevices.isEmpty()) {

                    setNoDataUI()

                } else {

                    checkingCloudPermissions    = true
                    setPermissionScanUI()
                    db!!.getUserPermissionsAndToken {
                        val tokenAndPermission = it as Array <String>

                        token           = tokenAndPermission[0]
                        userPermissions = UserPermissions(tokenAndPermission[1])
                        val macsArray   = JSONArray()

                        for (device in devices) {
                            macsArray.put(device.getMac())
                        }

                        val body = JSONObject()
                        body.put("macs", macsArray)
                        val mediaType = "application/json; charset=utf-8".toMediaType()
                        val requestBody = body.toString().toRequestBody(mediaType)
                        Log.e(TAG, "BODY TO REQUEST: ${body.toString()}")
                        fetchMacs(token, requestBody)
                    }
                }
            }
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
                    initUI()

                }

                override fun onResponse(call: Call<LinkPostResponse>, response: Response <LinkPostResponse>) {
                    // Handle function to display posts
                     Log.e(TAG, "onResponse: ${response.body()}")
                    val body = response.body()

                    if (body != null) {
                        runOnUiThread { Utils.showToastShort(applicationContext, getString(R.string.link_device_ok)) }

                    } else {

                        runOnUiThread { Utils.showToastShort(applicationContext, getString(R.string.link_device_error)) }
                    }
                    initUI()
                }
            })
    }


    /**
     * Function to fetch posts
     */
    private fun fetchMacs(token : String, macs : RequestBody) {
        val apiClient = ApiClient()

        // Pass the token as parameter
        apiClient.getApiService(this).fetchScanPost(token = "Bearer $token", macs)
            .enqueue(object : retrofit2.Callback< List <ScanPostResponse> > {
                override fun onFailure(call: Call<List <ScanPostResponse>>, t: Throwable) {
                    // Error fetching posts
                    Log.e(TAG, "onFailure: ${t.message}")

                }

                override fun onResponse(call: Call<List <ScanPostResponse>>, response: Response <List <ScanPostResponse>>) {
                    // Handle function to display posts
                     Log.e(TAG, "onResponseMacs: ${response.body()}")
                    val respList    = response.body()
                    val testArray   = arrayOf("")
                    if (respList != null && respList.isNotEmpty()) {
                        for (device in bleDevices) {
                            val cir = CirDevice( device)
                            for (scanPostRes in respList) {
                                if (scanPostRes.mac == device.getMac()) {
                                    cir.setScanPostResponse(scanPostRes)
                                    cirDevice.add(cir)
                                    break
                                }
                            }
                        }

                        Log.e(TAG, "CIR device: ${cirDevice}")
                        Log.e(TAG, "CIR device size: ${cirDevice.size}")

                        for (dev in cirDevice) { setRecyclerUI() }

                    } else { setNoDataUI() }
                }
            })
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private const val TIMEOUT           = 5_000L
    }


}
