package mx.softel.cirwirelesslib.enums

enum class StateMachine {
    UNKNOWN,
    POLING,
    STATUS,
    REFRESH_AP,
    WIFI_CONFIG,
    GET_AP,
    WIFI_STATUS,
    AT_WAIT_RESPONSE,
    STANDBY
}