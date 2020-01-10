package mx.softel.cirwireless.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.RootActivity
import mx.softel.cirwireless.dialogs.PasswordDialog
import mx.softel.cirwireless.extensions.toast

class AccessPointsFragment: Fragment(), AdapterView.OnItemClickListener {

    private lateinit var root           : RootActivity
    private lateinit var wifiManager    : WifiManager

    private lateinit var wifiResults    : MutableList<ScanResult>
    private          var apList         = ArrayList<String>()

    private lateinit var arrayAdapter   : ArrayAdapter<String>

    private lateinit var lvAccessPoints : ListView

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root        = (activity!! as RootActivity)
        wifiManager = root.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true

        //root.startBleService()
        Log.d(TAG, "Dispositivo a conectar: ${root.bleDevice.address}")
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_access_points, container, false)

        view.apply {
            lvAccessPoints = findViewById(R.id.lvAccessPoints)
        }

        arrayAdapter = ArrayAdapter(root, android.R.layout.simple_list_item_1, apList)
        lvAccessPoints.apply {
            adapter             = arrayAdapter
            onItemClickListener = this@AccessPointsFragment
        }
        scanWifi()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy -> stopBleService")
        root.stopBleService()
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
        }

    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
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