package mx.softel.cirwireless.fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.MainActivity
import mx.softel.cirwireless.extensions.toast

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment(), View.OnClickListener {

    private lateinit var btnConfigure   : Button
    private lateinit var btnTest        : Button
    private lateinit var tvMac          : TextView

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        view.apply {
            btnConfigure    = findViewById(R.id.btnConfigurar)
            btnTest         = findViewById(R.id.btnProbar)
            tvMac           = findViewById(R.id.tvMacSelected)

            tvMac.text = arguments!!.getString(MainActivity.EXTRA_MAC)
        }
        setOnClick()

        return view
    }

    private fun setOnClick() {
        btnConfigure.setOnClickListener(this)
        btnTest.setOnClickListener(this)
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnConfigurar  -> { activity!!.toast("Configurar") }
            R.id.btnProbar      -> { activity!!.toast("Probar") }
        }
    }



    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = MainFragment::class.java.simpleName

        /**
         * Singleton access to [MainFragment]
         */
        fun getInstance() = MainFragment()
    }

}
