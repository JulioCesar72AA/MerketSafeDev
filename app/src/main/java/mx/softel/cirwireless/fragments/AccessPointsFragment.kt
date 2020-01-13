package mx.softel.cirwireless.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.RootActivity
import mx.softel.cirwireless.dialogs.PasswordDialog
import mx.softel.cirwireless.extensions.toast

class AccessPointsFragment: Fragment(),
    AdapterView.OnItemClickListener,
    View.OnClickListener {

    // ROOT MANAGERS
    private lateinit var root           : RootActivity
    private lateinit var wifiManager    : WifiManager

    // LIST DATA
    private lateinit var wifiResults    : MutableList<ScanResult>
    private          var apList         = ArrayList<String>()
    private lateinit var arrayAdapter   : ArrayAdapter<String>

    // VIEWS
    private lateinit var lvAccessPoints : ListView
    private lateinit var backBtn        : ImageView
    private lateinit var scanMask       : View
    private lateinit var progressBar    : ProgressBar

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root        = (activity!! as RootActivity)
        wifiManager = root.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true

        Log.d(TAG, "Dispositivo a conectar: ${root.bleDevice.address}")
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_access_points, container, false)

        view.apply {
            lvAccessPoints  = findViewById(R.id.lvAccessPoints)
            backBtn         = findViewById(R.id.ivBackAccess)
            scanMask        = findViewById(R.id.scanMask)
            progressBar     = findViewById(R.id.pbScanning)
        }

        setOnClickListeners()
        setScanningUI()
        setWifiList()
        scanWifi()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy -> stopBleService")
        //root.stopBleService()
    }

    private fun setOnClickListeners() {
        scanMask.setOnClickListener(this)
        backBtn.setOnClickListener(this)
    }

    /************************************************************************************************/
    /**     BROADCAST                                                                               */
    /************************************************************************************************/
    private val wifiReceiver = object: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            wifiResults = wifiManager.scanResults
            root.unregisterReceiver(this)

            for (data in wifiResults) {
                Log.d(TAG, "${data.SSID} - ${data.frequency} - ${data.centerFreq0} - ${data.centerFreq1}")
                if (data.frequency < 3000 && data.SSID.length > 2)
                    if (!apList.contains(data.SSID)) apList.add(data.SSID)
                arrayAdapter.notifyDataSetChanged()
            }
            setStandardUI()
        }

    }

    /************************************************************************************************/
    /**     VISTA                                                                                   */
    /************************************************************************************************/
    private fun setWifiList() {
        arrayAdapter = ArrayAdapter(root, android.R.layout.simple_list_item_1, apList)
        lvAccessPoints.apply {
            adapter             = arrayAdapter
            onItemClickListener = this@AccessPointsFragment
        }
    }

    internal fun setScanningUI() {
        progressBar.visibility = View.VISIBLE
        scanMask   .apply {
            visibility = View.VISIBLE
            background = root.getDrawable(R.color.hardMask)
        }
    }

    internal fun setStandardUI() {
        progressBar.visibility = View.GONE
        scanMask   .visibility = View.GONE
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.scanMask       -> toast("Espera un momento, escaneando Access Points")
            R.id.ivBackAccess   -> root.supportFragmentManager.popBackStackImmediate()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val dialog = PasswordDialog()
        dialog.apply {
            show(root.supportFragmentManager, null)
        }
        toast(apList[position])
    }


    /************************************************************************************************/
    /**     WIFI                                                                                    */
    /************************************************************************************************/
    private fun scanWifi() {
        apList.clear()
        root.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = AccessPointsFragment::class.java.simpleName

        /**
         * Singleton access to [AccessPointsFragment]
         */
        fun getInstance() = AccessPointsFragment()
    }

}