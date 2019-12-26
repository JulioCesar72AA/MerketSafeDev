package mx.softel.cirwireless.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import kotlinx.android.synthetic.main.activity_main.*
import mx.softel.cirwireless.R
import mx.softel.cirwireless.adapters.ScanRecyclerAdapter
import mx.softel.scanblelib.ble.BleDevice
import mx.softel.scanblelib.ble.BleManager

class MainActivity: AppCompatActivity(),
                    SwipeRefreshLayout.OnRefreshListener,
                    View.OnClickListener,
                    ScanRecyclerAdapter.OnScanClickListener {

    private var bleDevices = ArrayList<BleDevice>()
    private var isScanning = false

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")
        srlScan.apply {
            setOnRefreshListener(this@MainActivity)
            setColorSchemeColors(getColor(R.color.colorAccent),
                                 getColor(R.color.colorIconBlue),
                                 getColor(R.color.colorPrimary))
        }
        setScanningUI()
        setOnClick()
        scanDevices()
    }

    override fun onRefresh() {
        Log.d(TAG, "onRefresh")

        scanMask.visibility = View.VISIBLE
        scanDevices()

        // Detenemos el escaneo en pantalla
        Handler().postDelayed({
            srlScan.isRefreshing = false
        }, TIMEOUT)
    }


    /************************************************************************************************/
    /**     ON CLICK                                                                                */
    /************************************************************************************************/
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.scanMask -> { /* Ignoramos el click para bloquear los demás elementos */ }
        }
    }

    private fun setOnClick() {
        scanMask.setOnClickListener(this)
    }


    /************************************************************************************************/
    /**     INTERFACES                                                                              */
    /************************************************************************************************/
    override fun onScanClickListener(position: Int) {
        Log.d(TAG, "onScanClickListener")

        val intent = Intent(this, RootActivity::class.java)
        intent.apply {
            val dev = bleDevices[position]
            putExtra(EXTRA_DEVICE,              dev.getBleDevice())
            putExtra(EXTRA_NAME,                dev.getName())
            putExtra(EXTRA_MAC,                 dev.getMac())
            putExtra(EXTRA_BEACON,              dev.getBeaconDeviceString())
            putExtra(EXTRA_BEACON_ENCRYPTED,    dev.getDeviceBeaconIsEncrypted())
            putExtra(EXTRA_BEACON_TYPE,         dev.getBeaconType())
            putExtra(EXTRA_IS_ENCRYPTED,        dev.isEncrypted())
            startActivity(this)
        }
    }


    /************************************************************************************************/
    /**     VISTA                                                                                   */
    /************************************************************************************************/
    private fun initUI() {
        Log.d(TAG, "initUI")

        pbScanning.visibility   = View.GONE
        scanMask.visibility     = View.GONE
    }

    private fun setScanningUI() {
        Log.d(TAG, "setScanningUI")

        pbScanning.visibility   = View.VISIBLE
        scanMask.visibility     = View.VISIBLE
    }

    private fun setRecyclerUI() {
        Log.d(TAG, "setRecyclerUI")

        rvBleList.apply {
            val manager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            layoutManager = manager
            adapter = ScanRecyclerAdapter(bleDevices, this@MainActivity)
        }

        initUI()
    }


    /************************************************************************************************/
    /**     AUXILIARES                                                                              */
    /************************************************************************************************/
    private fun scanDevices() {
        Log.d(TAG, "scanDevices")

        isScanning = true
        val bleManager = BleManager(this, TIMEOUT)
        bleManager.scanBleDevices {
            Log.d(TAG, "scanBleDevices")
            bleDevices = it
            for (dev in it) {
                Log.d(TAG, "Dispositivo: ${dev.getBleDevice()}")
                setRecyclerUI()
                isScanning = false
            }
        }
    }



    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = MainActivity::class.java.simpleName

        const val EXTRA_MAC                 = "mac"
        const val EXTRA_NAME                = "name"
        const val EXTRA_BEACON              = "beacon"
        const val EXTRA_BEACON_ENCRYPTED    = "beacon_encrypted"
        const val EXTRA_BEACON_TYPE         = "beacon_type"
        const val EXTRA_IS_ENCRYPTED        = "is_encrypted"
        const val EXTRA_DEVICE              = "device"

        private val TIMEOUT                 = 10_000L

    }


}
