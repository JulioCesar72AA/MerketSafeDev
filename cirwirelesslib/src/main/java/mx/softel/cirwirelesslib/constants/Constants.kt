package mx.softel.cirwirelesslib.constants

// Paso de parámetros entre actividades
const val EXTRA_MAC                 = "mac"
const val EXTRA_NAME                = "name"
const val EXTRA_BEACON              = "beacon"
const val EXTRA_BEACON_BYTES        = "beacon_bytes"
const val EXTRA_BEACON_ENCRYPTED    = "beacon_encrypted"
const val EXTRA_BEACON_TYPE         = "beacon_type"
const val EXTRA_IS_ENCRYPTED        = "is_encrypted"
const val EXTRA_DEVICE              = "device"
const val SSID                      = "SSID"
const val SSID_PASSCODE             = "SSID_passcode"
const val TOKEN                     = "token"
const val USER_PERMISSIONS          = "permissions"
const val TRANSMITION               = "transmition"
const val SERIAL_NUMBER             = "serial_number"
const val ASSET_TYPE                = "asset_type"
const val ASSET_MODEL               = "asset_model"


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
const val WIFI_SUBSTRING_ROUTER_IP_AFTER    = "+CIFSR:APIP,"
const val WIFI_SUBSTRING_ROUTER_IP_BEFORE   = "+CIFSR:APMAC"
const val WIFI_SUBSTRING_IP_BEFORE          = "+CIFSR:STAMAC"
const val WIFI_SUBSTRING_AP_AFTER           = "+CWJAP:"
const val SSID_SUBSTRING_AFTER              = "+CWSAP:\"ID_"

const val PING_OK                           = "+PING:"

const val AT_CMD_OK                         = "OK"
const val AT_CMD_CLOSED                     = "CLOSED"
const val AT_CMD_ERROR                      = "ERROR"
const val AT_CMD_CONNECT                    = "CONNECT"
const val AT_CMD_STATUS                     = "STATUS"

const val AT_MODE_MASTER_SLAVE              = 3
const val AT_MODE_SLAVE                     = 1
const val AT_NO_SEND_SSID                   = 1
const val AT_SEND_SSID                      = 0