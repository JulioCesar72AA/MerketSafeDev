package mx.softel.cirwireless.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import mx.softel.bleservicelib.enums.DisconnectionReason

import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.RootActivity
import mx.softel.cirwireless.extensions.toast
import mx.softel.cirwireless.interfaces.FragmentNavigation
import mx.softel.cirwirelesslib.enums.StateMachine
import mx.softel.cirwirelesslib.utils.CirCommands

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment(), View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    // BLUETOOTH
    private lateinit var navigation     : FragmentNavigation
    private lateinit var root           : RootActivity

    // VIEW's
    private lateinit var ivBack         : ImageView
    private lateinit var ivMenu         : ImageView
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
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        view.apply {
            // Asignamos las vistas por su ID
            ivBack          = findViewById(R.id.ivBack)
            ivMenu          = findViewById(R.id.ivMenuUpdate)
            tvMac           = findViewById(R.id.tvMacSelected)
            cvConfigure     = findViewById(R.id.cvConfigurar)
            cvTest          = findViewById(R.id.cvProbar)

            // Asignamos el texto de los argumentos recibidos
            tvMac.text      = root.bleMac
            ivMenu.visibility = View.GONE
        }
        setOnClick()
        return view
    }

    /**
     * ## setOnClick
     * Inicializa los eventos de click para los elementos en la vista
     */
    private fun setOnClick() {
        ivBack      .setOnClickListener(this)
        ivMenu      .setOnClickListener(this)
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
            R.id.ivBack         -> root.finishAndDisconnectActivity(DisconnectionReason.UNKNOWN.status)
            R.id.ivMenuUpdate   -> createMenu()
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
        // Actualizamos los AccessPoints que el dispositivo ve
        if (root.cirService.getCharacteristicWrite() == null)
            clickConfigure()
        else{
            toast("Actualizando datos")
            root.apply {
                CirCommands.initCmd(service!!, cirService.getCharacteristicWrite()!!, root.bleMacBytes)
                cirService.setCurrentState(StateMachine.GET_AP)
            }
        }
    }


    private fun clickTest() {
        if (root.cirService.getCharacteristicWrite() == null)
            clickTest()
        else {
            toast("Solicitando los datos del dispositivo")
            root.apply{
                setScanningUI()
                CirCommands.initCmd(service!!, cirService.getCharacteristicWrite()!!, root.bleMacBytes)
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
    /**     MENU                                                                                    */
    /************************************************************************************************/
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_update -> toast("Actualización pendiente")
        }
        return true
    }


    private fun createMenu() {
        val popup = PopupMenu(context, ivMenu)
        popup.apply {
            menuInflater.inflate(R.menu.menu_main, popup.menu)
            setOnMenuItemClickListener(this@MainFragment)
            show()
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
        @JvmStatic fun getInstance() = MainFragment()
    }

}
