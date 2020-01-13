package mx.softel.cirwirelesslib.enums

enum class ActualState(val code: Int) {
    UNKNOWN         (-1),
    DISCONNECTED    (0),
    CONNECTING      (1),
    CONNECTED       (2),
    DISCONNECTING   (3)
}