package mx.softel.cirwirelesslib.enums

enum class StateMachine {
    UNKNOWN,        // OTRO
    POLING,         // Estatus base
    GET_FIRMWARE_WIFI_MODULE, // Se obtiene el firmware del modulo WiFi
    WIFI_CONFIG,    // Configurando el WIFI en el CIR WIRELESS
    GET_CLIENT,     // Obtiene el cliente del dispositivo
    SET_WIFI_PASSCODE,     // Se envía la contraseña al dispositivo CIR Wireless
    GOING_BACK, // Indica que vamos atras en un fragmento, solo para asegurar que hay un estado controlado
    GO_TO_CONFIG_AND_TEST, // Se envía a la UI a la sección de configuración y prueba
    SETTING_REPOSITORY_URL, // Se actualiza la url del repositorio
    SETTING_FIRMWARE_PATH, // Se actualiza la ruta del archivo que esta en el servidor
    SETTING_FIRMWARE_VERSION, // Se actualiza la version de firmware a descargar
    SETTING_FIRMWARE_DELAY,
    SHOW_CONFIG_MODES, // Muestra las formas de configurar la CIR WIRELESS
    SHOW_PARAMETERS_STATIC_IP, // Se muestra el cuando de dialogo
    SETTING_MODE_IP_CONFIG, // Se configura el tipo de IP - DHCP o estatica
    SETTING_STATIC_IP_VALUES, // Se configuran los valores de la ip estatica en la CIR
    GET_AP,         // Obteniendo MAC's de AP que el dispositivo detecta
    SET_MODE,       // Obteniendo confirmación de que se configuró correctamente
    GET_CONFIG_AP,  // Obteniendo el SSID configurado
    GET_IP,         // Obteniendo la IP
    GET_STATUS_AP,  // Obteniendo el RSSI
    PING,           // Obteniendo status de PING
    DATA_CONNECTION,// Obteniendo status de conexión a servidor
    OPENNING_LOCK, // Abriendo chapa (cerradura)
    CLOSING_LOCK, // Cerrando chapa (cerradura)
    RELOADING_FRIDGE, // Cargando refrigerador
    UPDATING_DATE // Actualiza la fecha
}