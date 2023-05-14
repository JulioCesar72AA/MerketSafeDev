package mx.softel.marketsafe.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import mx.softel.marketsafe.R
import mx.softel.marketsafe.activities.RootActivity

private const val TAG = "ConfigTestCooler"

class ConfigTestCooler : Fragment() {

    internal var configAndTesting = false

    private lateinit var root           : RootActivity

    internal lateinit var tvSsid                : TextView
    internal lateinit var tvSignalValue         : TextView
    internal lateinit var tvAccessPointValue    : TextView
    internal lateinit var tvIPAddressValue      : TextView
    internal lateinit var lavLoaderTest         : LottieAnimationView
    internal lateinit var lavLoaderTest1        : LottieAnimationView
    internal lateinit var ivGreenCheck1         : ImageView
    internal lateinit var ivGreenCheck2         : ImageView
    internal lateinit var tvCurrentStatus       : TextView
    internal lateinit var tvCoolerModelLong     : TextView
    internal lateinit var tvCoolerSerial        : TextView
    internal lateinit var tvCoolerBrand         : TextView
    internal lateinit var tvCoolerModelShort    : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = (activity!! as RootActivity)
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
        root.setStandardUI()
    }

    private fun loadViews (view: View) {
        view.findViewById<Button>(R.id.btnConfigTextNext).setOnClickListener {
            if (configAndTesting) {

            }
        }

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
    }


    @SuppressLint("SetTextI18n")
    private fun initViews () {
        tvCoolerModelLong.text = "${root.assetType} ${root.assetMode}"
        tvCoolerSerial.text = "Serial: ${root.serialNumber}"
        tvCoolerBrand.text = "${getString(R.string.brand)} Imbera"
        tvCoolerModelShort.text = "${getString(R.string.model_short)} ${root.assetMode}"

    }

    companion object {
        @JvmStatic fun getInstance() = ConfigTestCooler()
    }
}