package mx.softel.cirwireless.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.RootActivity
import mx.softel.cirwireless.interfaces.FragmentUiUpdate

class TesterFragment: Fragment(), View.OnClickListener, FragmentUiUpdate {

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
        }

        initUIData()
        setOnCLickListeners()

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        root.apply {
            ipAssigned = ""
            ssidAssigned = ""
            rssiAssigned = ""
            pingAssigned = false
            apAssigned = false
            dataAssigned = false
        }
    }

    private fun setAccessPointUI() {
        tvSsidResult.text   = root.ssidAssigned
        tvRssiResult.text   = root.rssiAssigned
    }

    private fun setIpAddressUI() {
        tvIpResult.text     = root.ipAssigned
        val iconAp = if (root.apAssigned)
            resources.getDrawable(R.drawable.ic_ok, null)
        else resources.getDrawable(R.drawable.ic_nok, null)
        apChecked.setImageDrawable(iconAp)
    }

    private fun setPingUI() {
        val iconInternet = if (root.pingAssigned)
            resources.getDrawable(R.drawable.ic_ok, null)
        else resources.getDrawable(R.drawable.ic_nok, null)
        internetChecked.setImageDrawable(iconInternet)
    }

    private fun setDataUI() {
        val iconData = if (root.dataAssigned)
            resources.getDrawable(R.drawable.ic_ok, null)
        else resources.getDrawable(R.drawable.ic_nok, null)
        dataChecked.setImageDrawable(iconData)
    }

    private fun initUIData() {
        tvMacSelected.text  = root.bleMac
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




    override fun fragmentUiUpdate(state: Int) {
        when (state) {
            0 -> initUIData()
            1 -> tvSsidResult.text = root.ssidAssigned
            2 -> tvRssiResult.text = root.rssiAssigned
            3 -> setIpAddressUI()
            4 -> setPingUI()
            5 -> setDataUI()
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