package mx.softel.cirwireless.interfaces

import androidx.fragment.app.Fragment

interface FragmentNavigation {
    fun navigateTo(fragment: Fragment,
                   addToBackStack: Boolean,
                   animIn: Int = android.R.animator.fade_in,
                   animOut: Int = android.R.animator.fade_out)
}