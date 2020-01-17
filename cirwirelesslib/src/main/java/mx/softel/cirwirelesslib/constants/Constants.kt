package mx.softel.cirwirelesslib.constants

// Paso de parámetros entre actividades
const val EXTRA_MAC                 = "mac"
const val EXTRA_NAME                = "name"
const val EXTRA_BEACON              = "beacon"
const val EXTRA_BEACON_ENCRYPTED    = "beacon_encrypted"
const val EXTRA_BEACON_TYPE         = "beacon_type"
const val EXTRA_IS_ENCRYPTED        = "is_encrypted"
const val EXTRA_DEVICE              = "device"


// Intent Filters para Broadcast Receiver
const val BLE_SERVICE_BROADCAST_FILTER  = "mx.softel.cirwirelesslib.BLE_SERVICE"
const val BLE_SERVICE_EXTRA             = "BLE_SERVICE"

// Identificadores de estados en el BLE_SERVICE
const val SERVICE_INIT_FAILED           = 0
const val SERVICE_INIT_OK               = 1
const val DEVICE_CONNECTED              = 2
const val DEVICE_DISCONNECTED           = 3



// Validaciones de WIFI
const val WIFI_VALIDATION_IP_NOT_ASSIGNED   = "0.0.0.0"
const val WIFI_NOT_IP_STRING                = "+CIFSR:STAIP,\"0.0.0.0\""
const val WIFI_SUBSTRING_IP_AFTER           = "+CIFSR:STAIP,"
const val WIFI_SUBSTRING_IP_BEFORE          = "+CIFSR:STAMAC"
const val WIFI_SUBSTRING_AP_AFTER           = "+CWJAP:"

const val PING_OK                           = "+PING:"

const val AT_CMD_OK                         = "OK"
const val AT_CMD_ERROR                      = "ERROR"