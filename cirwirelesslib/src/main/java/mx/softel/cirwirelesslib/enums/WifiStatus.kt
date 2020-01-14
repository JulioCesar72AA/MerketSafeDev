package mx.softel.cirwirelesslib.enums

enum class WifiStatus(code: Int) {
    WIFI_CONFIGURING    (0),
    WIFI_NOT_CONNECTED  (1),
    WIFI_SSID_FAILED    (2),
    WIFI_CONNECTING     (3),
    WIFI_CONNECTED      (4),
    WIFI_IP_FAILED      (5),
    WIFI_GET_LOCATION   (6),
    WIFI_INTERNET_READY (7),
    WIFI_TRANSMITING    (8)
}