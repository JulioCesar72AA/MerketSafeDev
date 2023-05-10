package mx.softel.marketsafe.interfaces

import android.os.Bundle
import androidx.fragment.app.Fragment

interface FragmentNavigation {
    fun navigateTo(fragment: Fragment,
                   addToBackStack: Boolean,
                   args: Bundle?,
                   animIn: Int = android.R.animator.fade_in,
                   animOut: Int = android.R.animator.fade_out)
}