package mx.softel.cirwirelesslib.enums

enum class DisconnectionReason(val code: Int) {
    // USER DEFINED ERROR CODE'S
    UNKNOWN                             (0),
    DISCONNECTION_OCURRED               (1),
    NORMAL_DISCONNECTION                (3),
    FIRMWARE_UNSOPPORTED                (4),

    // SYSTEM DEFINED ERROR CODE'S
    CONNECTION_FAILED                   (62),
    ERROR_257                           (257),
    ERROR_133                           (133)
}