package mx.softel.cirwireless.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import mx.softel.cirwireless.R

class IpConfigValuesDialog : DialogFragment (), View.OnClickListener {
    private lateinit var etIpAddress        : EditText
    private lateinit var etMaskAddress      : EditText
    private lateinit var etGatewayAddress   : EditText
    private lateinit var cvAccept           : CardView

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pop_up_config_static_ip_values, container, false)
        view.apply {
            // Conectamos las vistas
            etIpAddress         = findViewById(R.id.etIpAddress)
            etMaskAddress       = findViewById(R.id.etMaskAddress)
            etGatewayAddress    = findViewById(R.id.etGatewayAddress)
            cvAccept            = findViewById(R.id.cvIpConfigParameters)
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
        cvAccept.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        val parent = activity as (OnDialogClickListener)

        when (v!!.id) {
            R.id.cvIpConfigParameters -> {
                parent.staticIpSelected()
            }
        }
        dismiss()
    }


    interface OnDialogClickListener {
        fun staticIpSelected ()
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = PasswordDialog::class.java.simpleName

        fun getInstance() = PasswordDialog()
    }
}