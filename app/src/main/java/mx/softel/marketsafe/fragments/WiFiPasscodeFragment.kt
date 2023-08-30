package mx.softel.marketsafe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import mx.softel.marketsafe.R
import mx.softel.marketsafe.activities.RootActivity
import mx.softel.marketsafe.extensions.toast

private const val TAG = "WiFiPasscodeFragment"

class WiFiPasscodeFragment : Fragment() {

    private lateinit var root           : RootActivity

    private lateinit var etRouterPasscode   : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = (activity!! as RootActivity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_wi_fi_passcode, container, false)
        loadViews(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        root.setStandardUI()
    }

    private fun loadViews (view: View) {
        view.findViewById <Button> (R.id.btnWiFiPasscodeNext).setOnClickListener {
            checkPasscode()
        }

        view.findViewById <TextView> (R.id.tvWiFiPasscodeWiFiName).text = root.ssidSelected

        etRouterPasscode = view.findViewById(R.id.etWiFiPasscodePasscode)
    }

    private fun checkPasscode () {
        if (etRouterPasscode.text.isNotEmpty()) {
            root.passwordTyped = etRouterPasscode.text.toString()
            root.goToConfigAndTest()

        } else {
            toast(getString(R.string.empty_passcode))
        }
    }

    companion object {
        @JvmStatic fun getInstance() = WiFiPasscodeFragment ()
    }
}