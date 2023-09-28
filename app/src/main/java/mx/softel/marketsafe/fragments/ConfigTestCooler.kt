package mx.softel.marketsafe.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import mx.softel.marketsafe.R
import mx.softel.marketsafe.activities.RootActivity
import mx.softel.marketsafe.interfaces.FragmentUiUpdate

private const val TAG = "ConfigTestCooler"

class ConfigTestCooler : Fragment(), RootActivity.RootBleEvents, FragmentUiUpdate {

    private lateinit var root           : RootActivity

    private lateinit var tvSsid                : TextView
    private lateinit var tvSignalValue         : TextView
    private lateinit var tvAccessPointValue    : TextView
    private lateinit var tvIPAddressValue      : TextView
    private lateinit var lavLoaderTest         : LottieAnimationView
    private lateinit var lavLoaderTest1        : LottieAnimationView
    private lateinit var ivGreenCheck1         : ImageView
    private lateinit var ivGreenCheck2         : ImageView
    private lateinit var tvCurrentStatus       : TextView
    private lateinit var tvCoolerModelLong     : TextView
    private lateinit var tvCoolerSerial        : TextView
    private lateinit var tvCoolerBrand         : TextView
    private lateinit var tvCoolerModelShort    : TextView
    internal lateinit var btnNext              : Button
    internal lateinit var buttonUpdateUrl      : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = ( activity!! as RootActivity )

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_config_test_cooler, container, false)
        loadViews(view)
        initViews()
        return view
    }

    override fun onResume() {
        super.onResume()
        //root.initWiFiConfig()
        root.initGetFwModule()
        root.setStandardUI()
        startAnimRouter()
        setCurrenStatus(getString(R.string.init_config))
    }

    private fun loadViews (view: View) {
        btnNext             = view.findViewById<Button>(R.id.btnConfigTextNext)
        tvCoolerModelLong   = view.findViewById(R.id.tvCoolerModelLong)
        tvCoolerSerial      = view.findViewById(R.id.tvCoolerSerial)
        tvCoolerBrand       = view.findViewById(R.id.tvCoolerBrand)
        tvCoolerModelShort  = view.findViewById(R.id.tvCoolerModelShort)
        tvSsid              = view.findViewById(R.id.tvSsidValue)
        tvSignalValue       = view.findViewById(R.id.tvSignalValue)
        tvAccessPointValue  = view.findViewById(R.id.tvAccessPointValue)
        tvIPAddressValue    = view.findViewById(R.id.tvIPAddressValue)
        lavLoaderTest       = view.findViewById(R.id.lavLoaderTest)
        lavLoaderTest1      = view.findViewById(R.id.lavLoaderTest1)
        ivGreenCheck1       = view.findViewById(R.id.ivGreenCheck1)
        ivGreenCheck2       = view.findViewById(R.id.ivGreenCheck2)
        tvCurrentStatus     = view.findViewById(R.id.tvCurrentStatus)
        buttonUpdateUrl     = view.findViewById<Button>( R.id.button_update_url )

        buttonUpdateUrl.visibility = View.INVISIBLE

        buttonUpdateUrl.setOnClickListener {

            //Log.e(TAG, "Boton presionado ir a ->StateMachine.GET_CLIENT<- ")
            root.initUpdateUrl()
        }

        btnNext.setOnClickListener {
            if (root.configAndTesting) {
                root.showCancelProcessDialog()

            } else {

                root.goToTabMainActivity()
                // cancel process and go to Main view
            }
        }
    }

    private fun setCurrenStatus (status: String) {
        tvCurrentStatus.text = status
    }

    private fun setCurrentStatusColor (idColor: Int) {
        tvCurrentStatus.setTextColor(ContextCompat.getColor(root.applicationContext, idColor))
    }

    internal fun startAnimRouter () {

        lavLoaderTest.playAnimation()
    }

    internal fun stopAnimRouter () {
        lavLoaderTest.pauseAnimation()
    }

    internal fun startAnimCloud () {
        lavLoaderTest1.playAnimation()
    }

    internal fun stopAnimCloud () {
        root.runOnUiThread {
            lavLoaderTest1.pauseAnimation()
        }
    }

    internal fun appearGreenCheckRouter () {
        ivGreenCheck1.visibility = View.VISIBLE
    }

    internal fun appearGreenCheckCloud () {
        root.runOnUiThread {
            ivGreenCheck2.visibility = View.VISIBLE

            val fwModuleAux: String = root.getFwModule()

            Log.e(TAG, "fw aux espxx: $fwModuleAux")

            if( validateFw(fwModuleAux) ) {
                Log.e(TAG, "fw aux espxx valido: ${validateFw(fwModuleAux)}")
                buttonUpdateUrl.visibility = View.VISIBLE
            }
        }
    }

    private fun validateFw (versionFw: String): Boolean = (versionFw == "0.2.0-1")

    @SuppressLint("SetTextI18n")
    private fun initViews () {
        tvCoolerModelLong.text  = "${root.assetType} ${root.assetMode}"
        tvCoolerSerial.text     = "Serial: ${root.serialNumber}"
        tvCoolerBrand.text      = "${getString(R.string.brand)} Imbera"
        tvCoolerModelShort.text = "${getString(R.string.model_short)} ${root.assetMode}"

    }

    private fun parseRssi (rssi: String) : String {
        val rssiInt = Integer.parseInt(rssi)

        if (rssiInt >= -50) return getString(R.string.excellent)

        if (rssiInt < -50 && rssiInt > -60) return getString(R.string.good)

        if (rssiInt <= -60 && rssiInt > -70) return getString(R.string.regular)

        if (rssiInt <= -70) return getString(R.string.bad)

        return rssi
    }

    companion object {
        @JvmStatic fun getInstance() = ConfigTestCooler()
    }

    override fun deviceConnected () {
        TODO("Not yet implemented")
    }

    override fun updateHotspot () {
        TODO("Not yet implemented")
    }

    override fun testConnection () {
        TODO("Not yet implemented")
    }

    override fun testFinished() {
        setCurrentStatusColor(R.color.green20)
        stopAnimCloud()
        appearGreenCheckCloud()

        Log.e(TAG, "Termina la configuracion entre el rautes e internet")
        tvCurrentStatus.text = getString(R.string.ok_wifi)
    }

    override fun successfullyConfigured () {
        setCurrentStatusColor(R.color.green20)
        stopAnimRouter()
        appearGreenCheckRouter()
        Handler(Looper.getMainLooper()).postDelayed({
            root.initWiFiTest()
            startAnimCloud()
            setCurrentStatusColor(R.color.azul40)
        }, 1_000)
    }

    override fun currentState(state: String) {
        setCurrenStatus(state)
    }

    override fun fragmentUiUpdate(state: Int) {
        when (state) {
            0 -> {
                tvCurrentStatus.text = getString(R.string.getting_ssid)
            }
            1 -> {
                tvSsid.text             = root.ssidAssigned
                tvCurrentStatus.text    = getString(R.string.getting_signal_strength)
            }
            2 -> {
                tvSignalValue.text      = parseRssi(root.rssiAssigned)
                tvCurrentStatus.text    = getString(R.string.getting_ip_address)
            }
            3 -> {
                tvIPAddressValue.text   = root.ipAssigned
                tvAccessPointValue.text = root.ipRouterAssigned
                tvCurrentStatus.text    = getString(R.string.pinging)
            }
            4 -> {
                tvCurrentStatus.text = getString(R.string.pinging)
            }
            5 -> { }
        }
    }

}