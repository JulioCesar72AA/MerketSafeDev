package mx.softel.cirwirelesslib.services

enum class ReceivedCmd {
    UNKNOWN,
    POLEO,
    STATUS,
    REFRESH_AP,
    AT_OK,
    AT_NOK,
    WAIT_AP,
    GET_AP
}