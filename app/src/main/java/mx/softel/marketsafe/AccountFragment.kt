package mx.softel.marketsafe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import mx.softel.marketsafe.activities.RootActivity
import mx.softel.marketsafe.activities.TabMainActivity


class AccountFragment : Fragment() {
    private lateinit var root : TabMainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = (activity as TabMainActivity)
    }

    override fun onCreateView (inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        getViews(view)

        return view
    }


    private fun getViews (view: View) {
        view.findViewById <TextView> (R.id.tvAccountUsername).text = "user"
    }

    companion object {
        @JvmStatic
        fun getInstance() = AccountFragment()
    }
}