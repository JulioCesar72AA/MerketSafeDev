package mx.softel.scanblelib.extensions

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val REQUEST_PERMISSION_FINE_LOCATION = 101

fun Context.isLocationPermissionGranted(): Boolean
        = ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

fun Context.requestLocationPermission()
        = ActivityCompat.requestPermissions(
            this as Activity,
            arrayOf(permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSION_FINE_LOCATION
        )

fun isLocationPermissionGranted(requestCode: Int, grantResults: IntArray)
        = requestCode == REQUEST_PERMISSION_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED
