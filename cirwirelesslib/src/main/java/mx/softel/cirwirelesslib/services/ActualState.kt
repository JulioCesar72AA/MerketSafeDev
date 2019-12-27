package mx.softel.cirwirelesslib.services

enum class ActualState {
    STANDBY,
    CONNECTING,
    CONNECTED,
    GETTING_SERVICES,
    GETTING_CHARACTERISTICS,
    WRITING,
    READING,
    NOTIFYING
}