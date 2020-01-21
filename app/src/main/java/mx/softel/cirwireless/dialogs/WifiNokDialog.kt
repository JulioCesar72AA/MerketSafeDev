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

class WifiNokDialog: DialogFragment(), View.OnClickListener {

    private lateinit var btnOk : Button

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pop_up_wifi_nok, container, false)
        view.apply {
            btnOk = findViewById(R.id.btnAcceptWifiNok)
        }
        btnOk.setOnClickListener(this)
        isCancelable = false
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(context!!)
        dialog.apply{
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }



    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnAcceptWifiNok -> { dismiss() }
        }
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = WifiNokDialog::class.java.simpleName

        fun getInstance() = WifiNokDialog()
    }

}