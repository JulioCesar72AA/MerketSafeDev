package mx.softel.cirwirelesslib.enums

enum class StateMachine {
    UNKNOWN,
    POLING,
    STATUS,
    REFRESHING_AP,
    WIFI_CONFIG,
    GET_AP,
    AT_WAIT_RESPONSE,
    STANDBY
}