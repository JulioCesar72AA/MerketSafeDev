package mx.softel.marketsafe.gps_module;

public interface ILocation {
    void onLocationTriggered ();

    void onGpsEnabled ();

    void onGpsDisabled ();

    void onNetworkEnabled ();

    void onNetworkDisabled ();
}
