package mx.softel.cirwireless.fragments

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.RootActivity

class TesterFragment: Fragment(), View.OnClickListener {

    // ROOT MANAGERS
    private lateinit var root           : RootActivity

    // VIEWS
    private lateinit var btnBack            : ImageView
    private lateinit var tvMacSelected      : TextView
    private lateinit var tvIpResult         : TextView
    private lateinit var tvSsidResult       : TextView
    private lateinit var tvRssiResult       : TextView
    private lateinit var apChecked          : ImageView
    private lateinit var internetChecked    : ImageView
    private lateinit var dataChecked        : ImageView
    private lateinit var statusChecked      : ImageView

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = (activity!! as RootActivity)
    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dispositivo_prueba, container, false)

        view.apply {
            btnBack         = findViewById(R.id.ivBackDispositivos)
            tvMacSelected   = findViewById(R.id.tvMacSelectedDispositivos)
            tvIpResult      = findViewById(R.id.tvIpDispositivos)
            tvSsidResult    = findViewById(R.id.tvSsidDispositivos)
            tvRssiResult    = findViewById(R.id.tvRssiDispositivos)
            apChecked       = findViewById(R.id.ivCheckAp)
            internetChecked = findViewById(R.id.ivCheckInternet)
            dataChecked     = findViewById(R.id.ivCheckData)
            statusChecked   = findViewById(R.id.ivCheckStatus)
        }

        initUIData()
        setOnCLickListeners()

        return view
    }

    private fun initUIData() {
        tvMacSelected.text  = root.bleMac
        tvIpResult.text     = root.ipAssigned
        tvSsidResult.text   = root.ssidAssigned
        tvRssiResult.text   = root.rssiAssigned

        val iconAp
                = if (root.apAssigned) resources.getDrawable(R.drawable.ic_ok, null)
                else resources.getDrawable(R.drawable.ic_nok, null)
        apChecked.setImageDrawable(iconAp)
    }

    private fun setOnCLickListeners() {
        btnBack.setOnClickListener(this)
    }






    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBackDispositivos -> root.backFragment()
        }
    }






    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = TesterFragment::class.java.simpleName

        @JvmStatic fun getInstance() = TesterFragment()
    }

}