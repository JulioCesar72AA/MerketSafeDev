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
    }

    /**
     * ## setOnClickListeners
     * Inicializa los eventos de click para los elementos en la vista
     */
    private fun setOnClickListeners() {
        scanMask.setOnClickListener(this)
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
            root.unregisterReceiver(this)

            // Rellena la lista para mostrar en la pantalla estándar
            for (data in wifiResults) {
                Log.d(TAG, "${data.SSID} - ${data.frequency} - ${data.centerFreq0} - ${data.centerFreq1}")

                // El dispositivo solo puede conectarse a 2.4 de frecuencia, así que filtramos...
                if (data.frequency < 3000 && data.SSID.length > 2)
                    if (!apList.contains(data.SSID)) apList.add(data.SSID)
                arrayAdapter.notifyDataSetChanged()

                // TODO: Añadir los "destacados" de la lista de macs del dispositivo
            }

            // Inicializa la vista estándar
            setStandardUI()
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
    private fun setWifiList() {
        arrayAdapter = ArrayAdapter(root, android.R.layout.simple_list_item_1, apList)
        lvAccessPoints.apply {
            adapter             = arrayAdapter
            onItemClickListener = this@AccessPointsFragment
        }
    }

    /**
     * ## setScanningUI
     * Muestra la vista boqueada de escaneo de datos
     */
    internal fun setScanningUI() {
        progressBar.visibility = View.VISIBLE
        scanMask   .apply {
            visibility = View.VISIBLE
            background = root.getDrawable(R.color.hardMask)
        }
    }

    /**
     * ## setStandardUI
     * Muestra una pantalla natural, sin bloqueos de escaneo
     */
    internal fun setStandardUI() {
        progressBar.visibility = View.GONE
        scanMask   .visibility = View.GONE
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
            R.id.ivBackAccess   -> root.supportFragmentManager.popBackStackImmediate()
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
        val dialog = PasswordDialog.getInstance()
        dialog.apply {
            apSelected = apList[position]
            show(root.supportFragmentManager, null)
        }
        toast(apList[position])
    }


    /************************************************************************************************/
    /**     WIFI                                                                                    */
    /************************************************************************************************/

    /**
     * ## scanWifi
     * Realiza el escaneo de los AccessPoints que el teléfono puede ver
     */
    private fun scanWifi() {
        // Leemos los Access Points del celular
        apList.clear()
        root.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        wifiManager.startScan()

        // Leemos los Access Points del dispositivo
        root.service!!.getMacListCmd()
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

}