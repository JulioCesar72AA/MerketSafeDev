package mx.softel.cirwirelesslib.enums

enum class StateMachine {
    UNKNOWN,        // OTRO
    POLING,         // Estatus base
    WIFI_CONFIG,    // Configurando el WIFI en el CIR WIRELESS
    GET_AP,         // Obteniendo MAC's de AP que el dispositivo detecta
    SET_MODE,       // Obteniendo confirmación de que se configuró correctamente
    GET_CONFIG_AP,  // Obteniendo el SSID configurado
    GET_IP,         // Obteniendo la IP
    GET_STATUS_AP,  // Obteniendo el RSSI
    PING,           // Obteniendo status de PING
    DATA_CONNECTION // Obteniendo status de conexión a servidor
}