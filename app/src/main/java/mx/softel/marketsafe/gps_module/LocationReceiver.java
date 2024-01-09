package mx.softel.marketsafe.gps_module;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class LocationReceiver extends BroadcastReceiver {
    private final ILocation locationCallBack;

    /**
     * initializes receiver with callback
     * @param iLocationCallBack Location callback
     */
    public LocationReceiver(ILocation iLocationCallBack){
        this.locationCallBack = iLocationCallBack;
    }

    /**
     * triggers on receiving external broadcast
     * @param context Context
     * @param intent Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            locationCallBack.onLocationTriggered();

            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (gpsEnabled) locationCallBack.onGpsEnabled(); else locationCallBack.onGpsDisabled();

            if (networkEnabled) locationCallBack.onNetworkEnabled(); else locationCallBack.onNetworkDisabled();
        }
    }
}
