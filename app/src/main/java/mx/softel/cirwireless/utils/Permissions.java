package mx.softel.cirwireless.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;


public class Permissions {
    private static final String TAG = "Permissions";

    public static int MULTIPLE_PERMISSIONS  = 5_000;
    public static int LOCATION_PERMISSIONS  = 5_001;
    public static int BLUETOOTH_PERMISSIONS = 5_002;
    public static int READ_PERMISSIONS      = 5_003;


    public static String [] getDefaultPermissions () {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? PERMISSIONS_ANDROID_12 : PERMISSIONS ;
    }


    private static final String [] PERMISSIONS = new String [] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    @RequiresApi(api = Build.VERSION_CODES.S)
    private static final String [] PERMISSIONS_ANDROID_12 = new String [] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    public static final String [] READ_PERMISSION = new String [] {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    public static final String [] BLUETOOTH_PERMISSION = new String [] {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
    };


    public static final String [] LOCATION_PERMISSION = new String [] {
            Manifest.permission.ACCESS_FINE_LOCATION,
    };



    public static boolean hasPermissions (@NonNull Context context, @NonNull String... permissions) {
        boolean allPermissionsGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
        }

        return allPermissionsGranted;
    }
}