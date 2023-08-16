package mx.softel.marketsafe.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mx.softel.marketsafe.R


class HelpMainFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_help_main, container, false)

        return view
    }

    companion object {
        private const val TAG = "HelpMainFragment"

        @JvmStatic
        fun getInstance() = HelpMainFragment()
    }
}