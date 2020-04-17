package mx.softel.cirwireless.fragments

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
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

    private var lockOptionsEnabled       : Boolean = true

    // BLUETOOTH
    private lateinit var navigation     : FragmentNavigation
    private lateinit var root           : RootActivity

    // VIEW's
    private lateinit var ivBack         : ImageView
    private lateinit var ivMenu         : ImageView
    private lateinit var cvConfigure    : CardView
    private lateinit var cvTest         : CardView
    private lateinit var cvOpenLock     : CardView
    private lateinit var cvCloseLock    : CardView
    private lateinit var tvMac          : TextView
    private lateinit var cvLockOrConfig : CardView
    private lateinit var ivLockOrConfig : ImageView

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
            cvOpenLock      = findViewById(R.id.cvOpenLock)
            cvCloseLock     = findViewById(R.id.cvCloseLock)
            cvLockOrConfig  = findViewById(R.id.cvConfigurationOrLock)
            ivLockOrConfig  = findViewById(R.id.ivConfigOrLock)

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
        cvOpenLock  .setOnClickListener(this)
        cvCloseLock .setOnClickListener(this)
        cvLockOrConfig.setOnClickListener(this)
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
            R.id.cvConfigurationOrLock -> showConfigOrLockBtns()
            R.id.cvCloseLock    -> sendCloseLockCommand()
            R.id.cvOpenLock     -> sendOpenLockCommand()
        }
    }

    private fun sendCloseLockCommand () {
        if (root.cirService.getQuickCommandsCharacteristic() == null)
            sendCloseLockCommand()
        else {
            toast("Cerrando cerradura")
            root.apply {
                CirCommands.closeLock(service!!, cirService.getQuickCommandsCharacteristic()!!, root.bleMacBytes)
                cirService.setCurrentState(StateMachine.CLOSING_LOCK)
            }
        }
    }

    private fun sendOpenLockCommand () {
        if (root.cirService.getQuickCommandsCharacteristic() == null)
            sendOpenLockCommand()
        else {
            toast("Abriendo cerradura")
            root.apply {
                CirCommands.openLock(service!!, cirService.getQuickCommandsCharacteristic()!!, root.bleMacBytes)
                cirService.setCurrentState(StateMachine.OPENNING_LOCK)
            }
        }
    }

    private fun showConfigOrLockBtns () {
        val animationForLockBtns : Int
        val animationForConfigBtns : Int
        val visibilityForLockBtns : Int
        val visibilityForConfigBtns : Int
        val image : Int

        if (lockOptionsEnabled) {
            animationForLockBtns = R.anim.slide_right_to_left
            animationForConfigBtns = R.anim.slide_left_to_right

            visibilityForLockBtns = View.GONE
            visibilityForConfigBtns = View.VISIBLE

            image = R.drawable.ic_abrir_chapa

            lockOptionsEnabled = false

        } else {
            animationForLockBtns = R.anim.slide_left_to_right
            animationForConfigBtns = R.anim.slide_right_to_left

            visibilityForLockBtns = View.VISIBLE
            visibilityForConfigBtns = View.GONE

            image = R.drawable.ic_config

            lockOptionsEnabled = true
        }

        cvOpenLock.startAnimation(AnimationUtils.loadAnimation(root, animationForLockBtns))
        cvCloseLock.startAnimation(AnimationUtils.loadAnimation(root, animationForLockBtns))

        cvConfigure.startAnimation(AnimationUtils.loadAnimation(root, animationForConfigBtns))
        cvTest.startAnimation(AnimationUtils.loadAnimation(root, animationForConfigBtns))

        cvOpenLock.visibility = visibilityForLockBtns
        cvCloseLock.visibility = visibilityForLockBtns

        cvConfigure.visibility = visibilityForConfigBtns
        cvTest.visibility = visibilityForConfigBtns

        ivLockOrConfig.setImageResource(image)
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
