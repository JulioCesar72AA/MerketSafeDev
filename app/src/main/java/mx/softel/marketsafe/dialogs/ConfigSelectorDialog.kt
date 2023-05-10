package mx.softel.marketsafe.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import mx.softel.marketsafe.R

class ConfigSelectorDialog : DialogFragment (), View.OnClickListener {

    private lateinit var cvStaticIp     : CardView
    private lateinit var cvDynamicIp    : CardView


    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pop_up_config_selector, container, false)
        view.apply {
            // Conectamos las vistas
            cvStaticIp  = findViewById(R.id.cvStaticIp)
            cvDynamicIp = findViewById(R.id.cvDynamicIp)

            setOnClickListeners()
        }
        return view
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(context!!)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


    private fun setOnClickListeners () {
        cvStaticIp.setOnClickListener(this)
        cvDynamicIp.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        val parent = activity as (OnDialogClickListener)

        when (v!!.id) {
            R.id.cvStaticIp     -> parent.staticIpSelected()
            R.id.cvDynamicIp    -> parent.dynamicIpSelected()
        }
        dismiss()
    }


    interface OnDialogClickListener {
        fun staticIpSelected ()

        fun dynamicIpSelected ()
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = ConfigSelectorDialog::class.java.simpleName

        fun getInstance() = PasswordDialog()
    }

}