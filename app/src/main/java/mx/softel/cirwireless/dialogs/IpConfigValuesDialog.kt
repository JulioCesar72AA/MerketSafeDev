package mx.softel.cirwireless.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.extensions.toast

const val TAG = "IpConfigValuesDialog"

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
                val ipConfigModel = IpConfigModel(
                    etIpAddress.text.toString(),
                    etMaskAddress.text.toString(),
                    etGatewayAddress.text.toString())

                if (checkIpValues(ipConfigModel))  {
                    parent.staticIpValues(ipConfigModel)
                    dismiss()
                }
            }
        }
    }


    private fun checkIpValues (ipConfigModel: IpConfigModel) : Boolean {
        var allValuesAreGood = true

        if (!ipConfigModel.isAValidIpAddress()) {
            toast(getString(R.string.invalid_ip_address))
            allValuesAreGood = false
        }

        if (!ipConfigModel.isAValidMaskAddress()) {
            toast(getString(R.string.invalid_mask_address))
            allValuesAreGood = false
        }

        if (!ipConfigModel.isAValidGateway()) {
            toast(getString(R.string.invalid_gateway))
            allValuesAreGood = false
        }

        return allValuesAreGood
    }


    interface OnDialogClickListener {
        fun staticIpValues (ipValues: IpConfigModel)
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = IpConfigValuesDialog::class.java.simpleName

        fun getInstance() = PasswordDialog()
    }
}