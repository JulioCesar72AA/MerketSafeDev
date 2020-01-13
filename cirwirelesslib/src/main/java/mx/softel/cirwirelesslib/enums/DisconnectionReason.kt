package mx.softel.cirwirelesslib.enums

enum class DisconnectionReason(val code: Int) {
    DISCONNECTION_OCURRED               (1),
    DEVICE_NOT_CONFIGURED               (2),
    NORMAL_DISCONNECTION                (3),
    WELL_CONFIGURED                     (4),
    ALL_INFORMATION_NEEDED_EXTRACTED    (5),
    NOT_ALLOWED_FIRMWARE                (6),
    TIME_OUT_DISCONNECTION              (7),
    NOT_COMPATIBLE_FIRMWARE_VARIABLES   (8),
    MISSING_BLE_SERVICES                (9),
    BAD_CONFIGURED                      (10),
    TEMPLATE_ALREADY_INSTALLED          (11),
    FIRMWARE_NOT_COMPATIBLE             (12),
    ERROR_257                           (257),
    ERROR_133                           (133)
}