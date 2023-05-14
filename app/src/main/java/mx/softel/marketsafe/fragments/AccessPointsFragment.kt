package mx.softel.marketsafe.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_main.*
import mx.softel.marketsafe.R
import mx.softel.marketsafe.activities.RootActivity
import mx.softel.marketsafe.dialogs.PasswordDialog
import mx.softel.marketsafe.extensions.toast
import mx.softel.cirwirelesslib.enums.StateMachine
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.marketsafe.adapters.ScanRecyclerAdapter
import mx.softel.marketsafe.adapters.ScanWiFiAdapter

class AccessPointsFragment: Fragment(),
                            AdapterView.OnItemClickListener,
                            SwipeRefreshLayout.OnRefreshListener,
                            View.OnClickListener,
                            RootActivity.RootEvents,
                            ScanWiFiAdapter.OnScanClickListener {

    // ROOT MANAGERS
    private lateinit var root           : RootActivity
    private lateinit var wifiManager    : WifiManager

    // LIST DATA
    private lateinit var wifiResults    : MutableList<ScanResult>
    private          var apMacList      = ArrayList<String>()     // Lista validada de MACS
    private lateinit var arrayAdapter   : ArrayAdapter<String>

    // VIEWS
    private lateinit var lvAccessPoints : RecyclerView
    private lateinit var tvMacSelected  : TextView
    private lateinit var backBtn        : ImageView
    private lateinit var srlScanAp      : SwipeRefreshLayout

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root        = (activity!! as RootActivity)
        wifiManager = root.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_access_points, container, false)

        view.apply {
            lvAccessPoints  = findViewById(R.id.lvAccessPoints)
            tvMacSelected   = findViewById(R.id.tvMacSelectedAccess)
            backBtn         = findViewById(R.id.ivBackAccess)
            srlScanAp       = findViewById(R.id.srlScanAp)
        }

        tvMacSelected.text = root.bleMac
        srlScanAp.apply {
            setOnRefreshListener(this@AccessPointsFragment)
            setColorSchemeColors(resources.getColor(R.color.colorAccent, null),
                resources.getColor(R.color.colorIconBlue, null),
                resources.getColor(R.color.colorPrimary, null))
        }


        setOnClickListeners()
        root.setScanningUI()
        setWifiList()
        scanWifi()
        return view
    }

    override fun onRefresh() {
        this.scanAccessPoints()
    }

    private fun scanAccessPoints () {
        root.apply {
            setScanningUI()
            CirCommands.getMacListCmd(service!!, cirService.getCharacteristicWrite()!!)
            cirService.setCurrentState(StateMachine.GET_AP)
        }
    }

    /**
     * ## setOnClickListeners
     * Inicializa los eventos de click para los elementos en la vista
     */
    private fun setOnClickListeners() {
        backBtn.setOnClickListener(this)
    }

    /************************************************************************************************/
    /**     BROADCAST                                                                               */
    /************************************************************************************************/

    /**
     * ## wifiReceiver: [BroadcastReceiver]
     * Ejecuta la acción cuando el teléfono termina de escanear los Access Points cercanos
     */
    private val wifiReceiver = object: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            wifiResults = wifiManager.scanResults
            Log.e(TAG, "WIFI_RESULTS: $wifiResults")
            root.unregisterReceiver(this)
            // Rellena la lista para mostrar en la pantalla estándar
            for (data in wifiResults) {
                Log.e(TAG, "wifi: $data")
                Log.e(TAG, "condition: ${root.deviceMacList == null}")

                // Establecemos una nueva lista de macs validadas por el dispositivo
                if (root.deviceMacList == null) return

                for (mac in root.deviceMacList!!) {
                    if (data.BSSID == mac) {
                        if (!data.SSID.isNullOrEmpty()) apMacList.add(data.SSID)
                    }
                }
            }

            for (data in wifiResults) {
                // Completamos la lista con los dispositivos escaneados por el teléfono
                // El dispositivo solo puede conectarse a 2.4 de frecuencia, así que filtramos...
                if (data.frequency < 3000 && data.SSID.length > 2)
                    if (!apMacList.contains(data.SSID) && !data.SSID.isNullOrEmpty())
                        apMacList.add(data.SSID)
            }

            // Actualizamos la vista con los access points encontrados
            arrayAdapter.notifyDataSetChanged()
            root.setStandardUI()
            srlScanAp.isRefreshing = false
        }

    }

    /************************************************************************************************/
    /**     VISTA                                                                                   */
    /************************************************************************************************/

    /**
     * ## setWifiList
     * Inicializa la lista de AccessPoints visibles para el teléfono,
     * asocia el adaptador y los datos a la [ListView]
     */
    @SuppressLint("UseRequireInsteadOfGet")
    private fun setWifiList() {

        arrayAdapter = ArrayAdapter(root, android.R.layout.simple_list_item_1, apMacList)
        // arrayAdapter = ArrayAdapter(root, R.id.tvWiFiName, apMacList)
        lvAccessPoints.apply {
            val manager     = LinearLayoutManager(root, LinearLayoutManager.VERTICAL, false)
            layoutManager   = manager
            adapter         = ScanWiFiAdapter(this@AccessPointsFragment.context!!, apMacList, this@AccessPointsFragment)
        }
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/

    /**
     * ## onClick
     * Implementación de la interface [View.OnClickListener]
     *
     * @param v Vista asociada al evento click
     */
    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.scanMask       -> toast("Espera un momento, escaneando Access Points")
            R.id.ivBackAccess   -> root.backFragment()
        }
    }

    /**
     * ## onItemClick
     * Implementación de la interface [AdapterView.OnItemClickListener]
     *
     * @param parent Adaptador del cual proviene el evento
     * @param view Vista seleccionada dentro del adaptador
     * @param position Índice del elemento seleccionado
     * @param id Id asociado a la vista seleccionada
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        root.ssidSelected = apMacList[position]
        root.goToWiFiPasscode()
        toast(apMacList[position])
    }


    /************************************************************************************************/
    /**     WIFI                                                                                    */
    /************************************************************************************************/

    /**
     * ## scanWifi
     * Realiza el escaneo de los AccessPoints que el teléfono puede ver
     */
    @Suppress("DEPRECATION")
    internal fun scanWifi() {
        // Leemos los Access Points del celular
        apMacList.clear()
        root.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()
    }


    private fun clickTest() {
        if (root.cirService.getCharacteristicWrite() == null)
            clickTest()
        else {
            toast(getString(R.string.getting_device_data))
            root.apply{
                setScanningUI()

                // CirCommands.initCmd(service!!, cirService.getCharacteristicWrite()!!, root.bleMacBytes)

                cirService.setCurrentState(StateMachine.UNKNOWN)

                if (actualFragment != testerFragment) {
                    actualFragment = testerFragment
                }

                runOnUiThread {
                    navigateTo(testerFragment, true, null)
                    setScanningUI()
                }
            }
        }
    }

    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = AccessPointsFragment::class.java.simpleName

        /**
         * Singleton access to [AccessPointsFragment]
         */
        @JvmStatic fun getInstance() = AccessPointsFragment()
    }

    override fun deviceConnected() {
        TODO("Not yet implemented")
    }

    override fun updateHotspot() {
        TODO("Not yet implemented")
    }

    override fun testConnection() {
        clickTest()
    }

    override fun onScanClickListener(position: Int) {
        root.ssidSelected = apMacList[position]
        root.goToWiFiPasscode()
        toast(apMacList[position])
    }
}