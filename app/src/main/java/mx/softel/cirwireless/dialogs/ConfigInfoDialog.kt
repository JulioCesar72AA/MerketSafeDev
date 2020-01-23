package mx.softel.cirwireless.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import mx.softel.cirwireless.R

class ConfigInfoDialog(var code: Int): DialogFragment(), View.OnClickListener {

    private lateinit var ivIcon: ImageView
    private lateinit var tvInfo: TextView
    private lateinit var btnOk : Button

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView -------------------------------------------------------------")
        val view = inflater.inflate(R.layout.pop_up_config_error, container, false)
        view.apply {
            ivIcon  = findViewById(R.id.ivApConfig)
            tvInfo  = findViewById(R.id.tvInfo)
            btnOk   = findViewById(R.id.btnAcceptConfig)
        }
        initUI()
        btnOk.setOnClickListener(this)
        isCancelable = false

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(context!!)
        dialog.apply {
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return dialog
    }

    private fun initUI() {
        when (code) {
            0 -> tvInfo.text = getString(R.string.error_no_communication)
            1 -> tvInfo.text = getString(R.string.error_no_ap)
            2 -> tvInfo.text = getString(R.string.error_no_ip)
            3 -> tvInfo.text = getString(R.string.error_no_ping)
            4 -> tvInfo.text = getString(R.string.error_no_data)
            100 -> {
                tvInfo.text = getString(R.string.error_no_error)
                ivIcon.setColorFilter(ContextCompat.getColor(context!!, R.color.colorOk))
                btnOk.setTextColor(resources.getColor(R.color.colorOk, null))
            }
        }
    }




    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnAcceptConfig -> { dismiss() }
        }
    }




    companion object {
        private val TAG = ConfigInfoDialog::class.java.simpleName
    }


}