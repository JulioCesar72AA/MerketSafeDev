package mx.softel.cirwirelesslib.utils

import mx.softel.cirwirelesslib.enums.ReceivedCmd

object CirWirelessParser {

    /**
     * ## receivedCommand
     * A partir de la respuesta recibida del dispositivo, parsea la
     * respuesta a un elemento de [ReceivedCmd]
     *
     * @param response Arreglo de bytes de la respuesta del dispositivo
     * @return Elemento [ReceivedCmd] con la descripción de la respuesta
     */
    fun receivedCommand(response: ByteArray): ReceivedCmd {
        return when (response[4]) {
            POLEO               -> ReceivedCmd.POLEO
            STATUS              -> ReceivedCmd.STATUS
            REFRESH_AP_OK       -> ReceivedCmd.REFRESH_AP_OK
            GET_AP              -> ReceivedCmd.GET_AP
            AT_OK               -> ReceivedCmd.AT_OK
            AT_NOK              -> ReceivedCmd.AT_NOK
            AT_RESPONSE_READY   -> ReceivedCmd.AT_READY
            WAIT_RESPONSE       -> ReceivedCmd.WAIT_AP
            WIFI_SSID_OK        -> ReceivedCmd.WIFI_SSID_OK
            WIFI_SSID_FAIL      -> ReceivedCmd.WIFI_SSID_FAIL
            WIFI_PASS_OK        -> ReceivedCmd.WIFI_PASS_OK
            WIFI_PASS_FAIL      -> ReceivedCmd.WIFI_PASS_FAIL
            WIFI_STATUS         -> ReceivedCmd.WIFI_STATUS
            else                -> ReceivedCmd.UNKNOWN
        }
    }

    fun reloadResponse (response: ByteArray): ReceivedCmd {
        return when (response[1]) {
            RELOAD_OK               -> ReceivedCmd.RELOAD_OK
            RELOAD_NOT_ENABLED      -> ReceivedCmd.RELOAD_NOT_ENABLED
            else                    -> ReceivedCmd.UNKNOWN
        }
    }


    fun dateUpdateResponse (response: ByteArray): ReceivedCmd {
        return when (response[1]) {
            DATE_UPDATED            -> ReceivedCmd.DATE_UPDATED
            else                    -> ReceivedCmd.DATE_ERROR
        }
    }


    fun lockResponse (response: ByteArray): ReceivedCmd {
        return when (response[1]) {
            LOCK_CLOSED_OR_OPEN_OK  -> ReceivedCmd.LOCK_OK
            LOCK_NOT_ENABLED        -> ReceivedCmd.LOCK_NOT_ENABLED
            else                    -> ReceivedCmd.UNKNOWN
        }
    }

    // Discriminantes de respuestas
    private const val WIFI_SSID_OK          = 0x22.toByte()
    private const val WIFI_SSID_FAIL        = 0x23.toByte()
    private const val WIFI_PASS_OK          = 0x25.toByte()
    private const val WIFI_PASS_FAIL        = 0x26.toByte()
    private const val WIFI_STATUS           = 0x28.toByte()
    private const val AT_RESPONSE_READY     = 0x35.toByte()
    private const val WAIT_RESPONSE         = 0x36.toByte()
    private const val REFRESH_AP_OK         = 0x48.toByte()
    private const val GET_AP                = 0x4A.toByte()
    private const val AT_OK                 = 0x4C.toByte()
    private const val AT_NOK                = 0x4D.toByte()
    private const val STATUS                = 0xC1.toByte()
    private const val POLEO                 = 0xC5.toByte()
    private const val LOCK_CLOSED_OR_OPEN_OK= 0x01.toByte()
    private const val LOCK_NOT_ENABLED      = 0x1E.toByte()
    private const val RELOAD_OK             = 0x01.toByte() // This is repeated 'cause this answers could change for this command
    private const val RELOAD_NOT_ENABLED    = 0x1E.toByte() // This is repeated 'cause this answers could change for this command
    private const val DATE_UPDATED          = 0x01.toByte()
    private const val DATE_ERROR            = 0x00.toByte()
}