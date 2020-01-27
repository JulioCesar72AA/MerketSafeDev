package mx.softel.cirwireless.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_info.*
import mx.softel.cirwireless.R

class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        val info = this.packageManager.getPackageInfo(packageName, 0)
        tvVersion.text = resources.getString(R.string.tv_version, info.versionName)
    }

}
