package mx.softel.marketsafe.activities

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import mx.softel.marketsafe.R
import mx.softel.marketsafe.web_services_module.ui_login.LoginActivity
import mx.softel.marketsafe.utils.Permissions
import mx.softel.marketsafe.utils.Utils

class SplashActivity : AppCompatActivity() {

    private val adapter = BluetoothAdapter.getDefaultAdapter()

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }


    override fun onResume() {
        super.onResume()

        Handler(mainLooper).postDelayed({
            if (Permissions.hasPermissions(this, *Permissions.getDefaultPermissions()))
                goToLoginActivity()

            else
                ActivityCompat.requestPermissions(this,
                Permissions.getDefaultPermissions(),
                Permissions.MULTIPLE_PERMISSIONS
            )
        }, 2000)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var canContinue = true

        if (requestCode == Permissions.MULTIPLE_PERMISSIONS) {
            if (grantResults.size == Permissions.getDefaultPermissions().size) {
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        canContinue = false
                        break
                    }
                }

                if (canContinue)
                    goToLoginActivity()

                else {
                    Utils.showToastLong(this, getString(R.string.permissions_ungranted))
                }
            }
        }
    }


    private fun goToLoginActivity() {
        Handler(mainLooper).postDelayed({
            val intent: Intent =
                Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }, 2000)
    }

    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = SplashActivity::class.java.simpleName
    }
}
