package mx.softel.cirwireless.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import mx.softel.cirwireless.R
import mx.softel.cirwireless.activities.RootActivity

class AccessPointsFragment: Fragment() {

    private lateinit var root       : RootActivity

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root        = (activity!! as RootActivity)
        root.startBleService()

        Log.d(TAG, "Dispositivo a conectar: ${root.bleDevice.address}")
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_access_points, container, false)

        view.apply {
            //TODO: Crear pantalla de espera de conexión
        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy -> stopBleService")
        root.stopBleService()
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = AccessPointsFragment::class.java.simpleName

        /**
         * Singleton access to [AccessPointsFragment]
         */
        fun getInstance() = AccessPointsFragment()
    }

}