package mx.softel.cirwireless.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView

import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.RootActivity
import mx.softel.cirwireless.extensions.toast
import mx.softel.cirwireless.interfaces.FragmentNavigation
import mx.softel.cirwirelesslib.enums.DisconnectionReason
import mx.softel.cirwirelesslib.enums.StateMachine

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment(), View.OnClickListener {

    // BLUETOOTH
    private lateinit var navigation     : FragmentNavigation
    private lateinit var root           : RootActivity

    // VIEW's
    private lateinit var ivBack         : ImageView
    private lateinit var cvConfigure    : CardView
    private lateinit var cvTest         : CardView
    private lateinit var tvMac          : TextView

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation  = (activity!! as FragmentNavigation)
        root        = (activity!! as RootActivity)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        view.apply {
            // Asignamos las vistas por su ID
            ivBack          = findViewById(R.id.ivBack)
            tvMac           = findViewById(R.id.tvMacSelected)
            cvConfigure     = findViewById(R.id.cvConfigurar)
            cvTest          = findViewById(R.id.cvProbar)

            // Asignamos el texto de los argumentos recibidos
            tvMac.text      = root.bleMac
        }
        setOnClick()
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    /**
     * ## setOnClick
     * Inicializa los eventos de click para los elementos en la vista
     */
    private fun setOnClick() {
        ivBack      .setOnClickListener(this)
        cvTest      .setOnClickListener(this)
        cvConfigure .setOnClickListener(this)
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
        when (v!!.id) {
            R.id.ivBack         -> root.finishActivity(DisconnectionReason.NORMAL_DISCONNECTION)
            R.id.cvConfigurar   -> clickConfigure()
            R.id.cvProbar       -> clickTest()
        }
    }

    /**
     * ## clickConfigure
     * Refresca los Access Points del dispositivo, y la actividad [RootActivity]
     * realiza el manejo de eventos por respuesta recibida del comando
     */
    private fun clickConfigure() {
        Log.d(TAG, "clickConfigure")

        // Actualizamos los AccessPoints que el dispositivo ve
        if (root.service!!.characteristicWrite == null)
            clickConfigure()
        else{
            toast("Actualizando datos")
            root.service!!.apply {
                getMacListCmd()
                currentState = StateMachine.GET_AP
            }
        }
    }


    private fun clickTest() {
        Log.d(TAG, "clickTest -> startBleService")
        toast("Probar")

        // TODO: Implementar el flujo de obtención de datos para el nuevo fragment
    }







    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = MainFragment::class.java.simpleName

        /**
         * Singleton access to [MainFragment]
         */
        @JvmStatic fun getInstance() = MainFragment()
    }

}
