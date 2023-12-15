package mx.softel.marketsafe.activities

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.viewbinding.BuildConfig
import mx.softel.Softel_Dinamic_Fw.Common.SoftelDinamicFwAuthenticate
import mx.softel.Softel_Dinamic_Fw.Common.SoftelDinamicFwSupportedList
import mx.softel.cir_wireless_mx.Entity.TagsFirmwareList
import mx.softel.cir_wireless_mx.dataBaseRoom.UserRepository
import mx.softel.marketsafe.R
import mx.softel.marketsafe.utils.Credential
import mx.softel.marketsafe.web_services_module.ui_login.LoginActivity
import mx.softel.marketsafe.utils.Permissions
import mx.softel.marketsafe.utils.Utils
import java.util.ArrayList

private lateinit var token : String
private lateinit var fwList : ArrayList<String>
class SplashActivity : AppCompatActivity() {

    private val adapter = BluetoothAdapter.getDefaultAdapter()

    /************************************************************************************************/
    /**     CICLO DE VIDA                                                                           */
    /************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initAuthenticateVariables()
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

    /******************* Get Token *******************/
    private var authToken : SoftelDinamicFwAuthenticate? = null
    private val authTokenInterface = object : SoftelDinamicFwAuthenticate.AuthenticationResult {
        override fun onSuccessAuthentication(tokenRes : String) {
            token = tokenRes
            //Log.e("token", token)
            initSupportedFwListVariables()
        }

        override fun onErrorAuthentication(error: String) {
            //Log.e(TAG, error)
        }
    }
    private fun initAuthenticateVariables () {
        var user = Credential.USUARIO
        var pass = ""
        Log.e("BuildConfig", BuildConfig.DEBUG.toString())
        if (BuildConfig.DEBUG) {
            pass = Credential.PASS_DEV
        } else {
            pass = Credential.PASS_PROD
        }
        authToken = SoftelDinamicFwAuthenticate(
            this,
            true,//BuildConfig.DEBUG,
            user,
            Credential.PASS_DEV,
            authTokenInterface)
    }
    /******************* Get Fw Support List *******************/
    private var fwSupportedList : SoftelDinamicFwSupportedList? = null
    private val fwSupportedListInterface = object : SoftelDinamicFwSupportedList.SupportedListResult {
        override fun onSuccessSupportedList(supportedFwList: ArrayList<String>) {
            fwList = supportedFwList
            //Log.e("fwList", fwList.toString())
            saveFirmwareList()
        }

        override fun onErrorSupportedList(error: String) {
            //Log.e(TAG, error)
        }
    }
    private fun initSupportedFwListVariables () {
        var appId = applicationContext.packageName
        fwSupportedList = SoftelDinamicFwSupportedList(
            this,
            true,//BuildConfig.DEBUG,
            token,
            appId,
            fwSupportedListInterface)
    }

    private fun saveFirmwareList() {
        var repo = UserRepository(this)
        for (firmware in fwList) {
            var statusId = repo.getFirmwareListWithId(firmware)
            //Log.e("statusId", "statusId: $statusId")
            val firmwareList = TagsFirmwareList(id = firmware, fw = firmware)

            if (statusId != null) { repo.updateFirmwareList(firmwareList) }

            else { repo.insertFirmwareList(firmwareList) }
        }
    }


    /************************************************************************************************/
    /**     COMPANION OBJECT                                                                        */
    /************************************************************************************************/
    companion object {
        private val TAG = SplashActivity::class.java.simpleName
    }
}
