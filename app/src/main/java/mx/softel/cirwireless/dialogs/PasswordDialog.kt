package mx.softel.cirwireless.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.extensions.toast

class PasswordDialog: DialogFragment(), View.OnClickListener {

    private lateinit var btnContinue    : Button
    private lateinit var btnCancel      : Button

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Elegimos un tema por defecto de DialogFragment
        //setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pop_up_password, container, false)
        view.apply {
            btnContinue = findViewById(R.id.btnAccept)
            btnCancel   = findViewById(R.id.btnCancel)
            setOnClickListeners()
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(context!!)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


    private fun setOnClickListeners() {
        btnCancel.setOnClickListener(this)
        btnContinue.setOnClickListener(this)
    }

    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnAccept -> setAP()
            R.id.btnCancel -> cancel()
        }
    }


    private fun setAP() {
        toast("Configurando el dispositivo CIR WIRELESS")
    }

    private fun cancel() {
        toast("Cancelado")
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = PasswordDialog::class.java.simpleName
    }

}