package mx.softel.marketsafe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import mx.softel.marketsafe.R
import mx.softel.marketsafe.activities.TabMainActivity


class HomeMainFragment : Fragment() {
    private lateinit var root : TabMainActivity

    private lateinit var llBleStatus        : LinearLayout
    private lateinit var tvBleStatusValue   : TextView

    override fun onCreate (savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = (activity as TabMainActivity)
    }

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_main, container, false)
        getViews(view)
        setTextInViews(view)
        setListeners(view)
        return view
    }


    override fun onResume() {
        super.onResume()
        if (root.isConnected) goodConnection() else badConnection()
    }

    private fun getViews (view: View) {
        llBleStatus = view.findViewById(R.id.llBleStatus)
        tvBleStatusValue = view.findViewById(R.id.tvBleStatusValue)
    }

    private fun setListeners (view: View) {
        view.findViewById <Button> (R.id.btnUnlockCooler).setOnClickListener {
            root.sendOpenLockCommand()
        }

        view.findViewById <Button> (R.id.btnLockCooler).setOnClickListener {
            root.sendCloseLockCommand()
        }

        view.findViewById <Button> (R.id.btnTestCooler).setOnClickListener {
            root.goToTestActivity()
        }
    }

    private fun setTextInViews (view: View) {
        view.findViewById <TextView> (R.id.tvCoolerModelLong).text = "${root.assetType} ${root.assetMode}"
        view.findViewById <TextView> (R.id.tvCoolerBrand).text = "${getString(R.string.brand)}: Imbera"
        view.findViewById <TextView> (R.id.tvCoolerSerial).text = "Serial: ${root.serialNumber}"
        view.findViewById <TextView> (R.id.tvCoolerModelShort).text = "${getString(R.string.model_short)}: ${root.assetMode}"
    }

    internal fun setBleStatus (status: String) {
        tvBleStatusValue.text = status
    }

    internal fun badConnection () {
        llBleStatus.setBackgroundResource(R.drawable.rounded_corners_coral_40)
        setBleStatus(getString(R.string.disconnected))
    }

    internal fun goodConnection () {
        llBleStatus.setBackgroundResource(R.drawable.rounded_corners_green_50)
        setBleStatus(getString(R.string.connected))
    }

    companion object {
        @JvmStatic fun getInstance() = HomeMainFragment()
    }
}