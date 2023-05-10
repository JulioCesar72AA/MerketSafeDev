package mx.softel.marketsafe.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import mx.softel.marketsafe.R
import mx.softel.marketsafe.extensions.toast

class PasswordDialog: DialogFragment(), View.OnClickListener {

    private lateinit var btnContinue    : Button
    private lateinit var btnCancel      : Button
    private lateinit var tvAccessPoints : TextView
    private lateinit var etPassword     : EditText

    internal lateinit var apSelected    : String

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pop_up_password, container, false)
        view.apply {
            // Conectamos las vistas
            tvAccessPoints  = findViewById(R.id.tvAccessPoint)
            btnContinue     = findViewById(R.id.btnAccept)
            btnCancel       = findViewById(R.id.btnCancel)
            etPassword      = findViewById(R.id.etPassword)

            // Iniciamos la vista
            tvAccessPoints.text = apSelected

            // Establecemos los eventos de click en la vista
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

        val parent = activity as (OnDialogClickListener)
        when (v!!.id) {
            R.id.btnAccept -> {
                if (etPassword.text.isBlank() || etPassword.text.isEmpty()) {
                    toast("Debes ingresar un password válido")
                    return
                }
                parent.dialogAccept(etPassword.text.toString())
            }
            R.id.btnCancel -> parent.dialogCancel()
        }
        dismiss()
    }

    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    interface OnDialogClickListener {
        fun dialogAccept(password: String)
        fun dialogCancel()
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = PasswordDialog::class.java.simpleName

        fun getInstance() = PasswordDialog()
    }

}