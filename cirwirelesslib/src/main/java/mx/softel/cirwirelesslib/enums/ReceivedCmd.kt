package mx.softel.cirwirelesslib.enums

enum class ReceivedCmd {
    UNKNOWN,
    POLEO,
    STATUS,
    REFRESH_AP_OK,
    AT_OK,
    AT_NOK,
    AT_READY,
    WAIT_AP,
    GET_AP,
    WIFI_SSID_OK,
    WIFI_SSID_FAIL,
    WIFI_PASS_OK,
    WIFI_PASS_FAIL,
    WIFI_STATUS,
    LOCK_OK,
    LOCK_NOT_ENABLED
}