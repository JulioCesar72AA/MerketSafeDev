package mx.softel.cirwireless.fragments


import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView

import mx.softel.cirwireless.R
import mx.softel.cirwireless.extensions.toast
import mx.softel.cirwirelesslib.constants.Constants
import mx.softel.cirwirelesslib.services.BleService

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment(), View.OnClickListener {

    // BLUETOOTH
    private          var bleDevice      : BluetoothDevice? = null

    // VIEW's
    private lateinit var cvConfigure    : CardView
    private lateinit var cvTest         : CardView
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
            // Asignamos las vistas por su ID
            //btnConfigure    = findViewById(R.id.btnConfigurar)
            tvMac           = findViewById(R.id.tvMacSelected)
            cvConfigure     = findViewById(R.id.cvConfigurar)
            cvTest          = findViewById(R.id.cvProbar)

            // Asignamos el texto de los argumentos recibidos
            tvMac.text = arguments!!.getString(Constants.EXTRA_MAC)
        }
        setOnClick()
        bleDevice   = arguments!!.getParcelable(Constants.EXTRA_DEVICE)
        Log.d(TAG, "initServices -> bleDevice(${bleDevice?.address})")
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy -> stopBleService")
        stopBleService()
    }

    private fun setOnClick() {
        cvTest      .setOnClickListener(this)
        cvConfigure .setOnClickListener(this)
    }



    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.cvConfigurar   -> clickConfigure()
            R.id.cvProbar       -> clickTest()
        }
    }

    private fun clickConfigure() {
        Log.d(TAG, "clickConfigure -> startBleService")
        activity!!.toast("Configurar")
        startBleService()
    }

    private fun clickTest() {
        Log.d(TAG, "clickTest -> startBleService")
        activity!!.toast("Probar")
        startBleService()
    }





    /************************************************************************************************/
    /**     SERVICES                                                                                */
    /************************************************************************************************/
    private fun startBleService() {

        val intent = Intent(context, BleService::class.java)
        intent.apply {
            putExtra(Constants.EXTRA_DEVICE, bleDevice)
        }
        activity!!.startService(intent)
    }

    private fun stopBleService()
            = activity!!.stopService(Intent(context, BleService::class.java))



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
