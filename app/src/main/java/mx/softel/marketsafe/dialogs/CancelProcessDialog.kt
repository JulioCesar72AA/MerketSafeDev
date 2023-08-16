package mx.softel.marketsafe.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import mx.softel.cirwirelesslib.utils.CirCommands
import mx.softel.marketsafe.R
import mx.softel.marketsafe.activities.RootActivity



class CancelProcessDialog: DialogFragment(), View.OnClickListener {

    private lateinit var root   : RootActivity

    private lateinit var tvInfo : TextView
    private lateinit var btnPositive : Button
    private lateinit var btnNegative : Button

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root        = (activity!! as RootActivity)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.pop_up_cancel_process, container, false)
        view.apply {
            tvInfo          = findViewById(R.id.tvCancelProcessMsg)
            btnPositive     = findViewById(R.id.btnCancelProcessPositive)
            btnNegative     = findViewById(R.id.btnCancelProcessNegative)
        }

        btnPositive.setOnClickListener(this)
        btnNegative.setOnClickListener(this)
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


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnCancelProcessPositive -> {
                dismiss()
            }
            R.id.btnCancelProcessNegative -> {

            }
        }
    }

    companion object {
        private const val TAG = "CancelProcessDialog"
    }
}