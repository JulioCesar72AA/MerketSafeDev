package mx.softel.marketsafe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import mx.softel.marketsafe.R
import mx.softel.marketsafe.activities.RootActivity
import mx.softel.marketsafe.activities.TabMainActivity


class SettingsMainFragment : Fragment() {
    private lateinit var root : TabMainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = (activity as TabMainActivity)
    }

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings_main, container, false)
        setListeners(view)
        return view
    }

    private fun setListeners (view: View) {
        view.findViewById <Button> (R.id.btnUserAccount).setOnClickListener {

        }
    }

    companion object {
        @JvmStatic fun getInstance () = SettingsMainFragment()
    }
}