package mx.softel.cirwireless.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Utils {
    private static final String TAG = "Utils";

    private static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";


    public static void showToastLong(Context ctx, String message) { Toast.makeText(ctx, message, Toast.LENGTH_LONG).show(); }


    public static void showToastShort (Context ctx, String message) { Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show(); }


    public static String getMSB(String string) {
        StringBuilder msbString = new StringBuilder();

        for (int i = string.length(); i > 0; i -= 2) {
            String str = string.substring(i - 2, i);
            msbString.append(str);
        }
        return msbString.toString();
    }


    public static String getHexValue(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (byte byteChar : array) {
            sb.append(String.format("%02x", byteChar));
        }
        return "" + sb;
    }

    public static String getUUID () { return UUID.randomUUID().toString().toUpperCase(); }


    public static SimpleDateFormat getDefaultDateFormat () { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); }


    public static String getTimeandDate () {
        Calendar calendar = Calendar.getInstance();
        return getDefaultDateFormat().format(calendar.getTime());
    }


    public static boolean isNetworkAvailable (Context ctx) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static boolean isEmailValid (CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public static ElapsedDate getDatesDifference (Date pastDate, Date currentDate) {
        long different = currentDate.getTime() - pastDate.getTime();

        /*
        Log.e(TAG, "startDate : " + pastDate);
        Log.e(TAG, "endDate : "+ currentDate);
        Log.e(TAG, "different : " + different);
         */

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli   = minutesInMilli * 60;
        long daysInMilli    = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        /*
        Log.e(TAG, String.format("%d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds));
         */

        return new ElapsedDate(elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
    }


    public static String getWifiMacAddress(Context mContext)
    {

        String wifiMacAddress = "XX:XX:XX:XX:XX:XX";
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                wifiMacAddress = getWifiMacAddressForMarsh();
            } else {
                WifiManager wifiMan = (WifiManager) mContext.getApplicationContext().getSystemService(
                        Context.WIFI_SERVICE);

                if (wifiMan != null) {
                    WifiInfo wifiInf = wifiMan.getConnectionInfo();
                    wifiMacAddress = wifiInf.getMacAddress();
                    // wifiMacAddress = bluetoothAdapter.getAddress();
                }
            }

            if (wifiMacAddress.equals("02:00:00:00:00:00")) {
                wifiMacAddress = getBluetoothMacAddressMetodo2(mContext);
            }
        }
        catch (Exception error)
        {
            wifiMacAddress = "XX:XX:XX:XX:XX:XX";
            // Log.d("Utils", error.getMessage());
            error.printStackTrace();
        }

        // Log.d("Utils", "getWifiMacAddress:wifiMacAddress: " + wifiMacAddress);
        return wifiMacAddress;
    }


    private static String getBluetoothMacAddressMetodo2(Context mContext) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) return "XX:XX:XX:XX:XX:XX";

        String address = mBluetoothAdapter.getAddress();

        if (address.equals("02:00:00:00:00:00")) {

            //  System.out.println(">>>>>G fail to get mac address " + address);

            try {

                ContentResolver mContentResolver = mContext.getContentResolver();

                address = Settings.Secure.getString(mContentResolver, SECURE_SETTINGS_BLUETOOTH_ADDRESS);
                //DebugReportOnLocat.ln(">>>>G >>>> mac " + address);

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return address;
    }


    private static String getWifiMacAddressForMarsh () {
        try {

            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)){
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length()>0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        return "XX:XX:XX:XX:XX:XX";
    }


    public static void setAnimation (Context ctx, View mView, int mAnimation) {
        Animation mSetAnimation = AnimationUtils.loadAnimation(ctx, mAnimation);
        mSetAnimation.setFillAfter(true);
        mView.startAnimation(mSetAnimation);
    }


    public static String getAppVersion (Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }


    public static boolean isAndroid6orGreater () {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }


    public static boolean isAndroid11OrGreater () {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
    }
}
