package mx.softel.cirwirelesslib.utils

import android.util.Log
import mx.softel.cirwirelesslib.extensions.toByteArray
import mx.softel.cirwirelesslib.extensions.toCharString
import mx.softel.cirwirelesslib.extensions.toHex

object CommandUtils {

    private val TAG = CommandUtils::class.java.simpleName
    private val wrapper = CLibraryWrapper()

    private val REFRESH_AP          = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x47)
    private val GET_AP_MAC_LIST     = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x49)
    private val SET_SSID            = byteArrayOf(0x55, 0x13, 0x10, 0x00, 0x21)
    private val SET_PASSWORD        = byteArrayOf(0x55, 0x13, 0x10, 0x00, 0x24)
    private val WIFI_STATUS         = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x27)
    private val AT_GENERIC          = byteArrayOf(0x55, 0x13, 0x10, 0x00, 0x4B)
    private val AT_READ             = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x34)
    private const val CLOSE_LOCK    = 0x0E
    private const val OPEN_LOCK     = 0x0F

    private val PASSCODE            = byteArrayOf(
        0x4a, 0xb0.toByte(), 0x0d, 0xc6.toByte(), 0xfc.toByte(), 0x4e,
        0x3e, 0x8c.toByte(), 0xf6.toByte(), 0x1a, 0x5a, 0xcb.toByte(),
        0x94.toByte(), 0xe6.toByte(), 0x53, 0x15
    )

    /************************************************************************************************/
    /**     COMANDOS PLANOS                                                                         */
    /************************************************************************************************/
    fun refreshAccessPointsCmd(): ByteArray
            = REFRESH_AP + getCrc16(REFRESH_AP)

    fun getAccessPointsCmd(): ByteArray
            = GET_AP_MAC_LIST + getCrc16(GET_AP_MAC_LIST)

    fun getWifiStatusCmd(): ByteArray
            = WIFI_STATUS + getCrc16(WIFI_STATUS)

    fun setSsidCmd(ssid: String): ByteArray {
        val nameBytes = ssid.toByteArray()
        return getCompleteCommand(SET_SSID, nameBytes)
    }

    fun setPasswordCmd(password: String): ByteArray {
        val passBytes = password.toByteArray()
        return getCompleteCommand(SET_PASSWORD, passBytes)
    }

    fun readAtCmd(): ByteArray {
        return AT_READ + getCrc16(AT_READ)
    }


    /************************************************************************************************/
    /**     COMANDOS ENCRIPTADOS                                                                    */
    /************************************************************************************************/
    fun configureAccessPointCmd(ssid: String, password: String, mac: ByteArray): ByteArray {
        val atCommand = "AT+CWJAP=\"$ssid\",\"$password\"".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun checkIpAddressCmd(mac: ByteArray): ByteArray {
        val atCommand = "AT+CIFSR".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun checkApConnectionCmd(mac: ByteArray): ByteArray {
        val atCommand = "AT+CWJAP?".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun pingApCmd(domain: String, mac: ByteArray): ByteArray {
        val atCommand = "AT+PING=\"$domain\"".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun closeSocketCmd(mac: ByteArray): ByteArray {
        val atCommand = "AT+CIPCLOSE".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun openSocketCmd(server: String, port: String, mac: ByteArray): ByteArray {
        val atCommand = "AT+CIPSTART=\"TCP\",\"$server\",$port".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun setDeviceWifiModeCmd(mode: Int, mac: ByteArray): ByteArray {
        val atCommand = "AT+CWMODE=$mode".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun setInternalNameAPCmd(ssid: String, password: String, flag: Int, mac: ByteArray): ByteArray {
        val atCommand = "AT+CWSAP=\"ID_$ssid\",\"$password\",6,0,4,$flag".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun getInternalNameAPCmd(mac: ByteArray): ByteArray {
        val atCommand = "AT+CWSAP?".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun setAutoConnCmd(enable: Int, mac: ByteArray): ByteArray {
        val atCommand = "AT+CWAUTOCONN=$enable".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun resetWifiCmd(mac: ByteArray): ByteArray {
        val atCommand = "AT+RST".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun getWirelessFirmwareCmd(mac: ByteArray): ByteArray {
        val atCommand = "AT+GMR".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun checkCipStatusCmd(mac: ByteArray): ByteArray {
        val atCommand = "AT+CIPSTATUS".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun initialCmd(mac: ByteArray): ByteArray {
        val atCommand = "S_T_R_T".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun terminateCmd(mac: ByteArray): ByteArray {
        val atCommand = "E_N_D".toByteArray()
        return getCompleteEncryptedCommand(AT_GENERIC, atCommand, mac)
    }

    fun openLockCmd (mac: ByteArray): ByteArray {
        val openLockCmd: ByteArray = byteArrayOf((PASSCODE.size + 2).toByte(), OPEN_LOCK.toByte()) + PASSCODE
        // Log.e(TAG, "OPEN_LOCK_COMMAND: ${openLockCmd.toHex()}")
        return getCompleteEncryptedCommand(openLockCmd, mac)
    }

    fun closeLockCmd (mac: ByteArray): ByteArray {
        val closeLockCmd: ByteArray = byteArrayOf((PASSCODE.size + 2).toByte(), CLOSE_LOCK.toByte())  + PASSCODE
        return getCompleteEncryptedCommand(closeLockCmd, mac)
    }

    /************************************************************************************************/
    /**      CRC                                                                                    */
    /************************************************************************************************/
    private fun getCompleteCommand(startArray: ByteArray, atCmd: ByteArray): ByteArray {
        val size = if (startArray.contentEquals(AT_GENERIC)) atCmd.size + 8 else atCmd.size + 7
        var cmd = startArray + atCmd + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    private fun getCompleteEncryptedCommand(startArray: ByteArray, atCmd: ByteArray, mac: ByteArray): ByteArray {
        val size = if (startArray.contentEquals(AT_GENERIC)) atCmd.size + 8 else atCmd.size + 7
        var cmd = atCmd + 0x00.toByte()

        // Encriptando el bloque de datos
        val encCmd = wrapper.getEnc(mac, cmd, 1)

        cmd = startArray + encCmd
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    private fun getCompleteEncryptedCommand (cmd: ByteArray, mac: ByteArray): ByteArray {
        return wrapper.getEnc(mac, cmd, 1)
    }

    fun decryptResponse(response: ByteArray, mac: ByteArray): ByteArray {
        val noCrc = response.dropLast(2).toByteArray()
        val noHeader = noCrc.drop(5).toByteArray()
        val result = wrapper.getDec(mac, noHeader, null)
        // Log.e(TAG, "${response.toHex()} -> ${response.toCharString()}")
        // Log.e(TAG, "${noHeader.toHex()} -> ${noHeader.toCharString()}")
        // Log.e(TAG, "${result.toHex()} -> ${result.toCharString()}")

        // Respuesta desencriptada
        return result
    }

    private fun getCrc16(buffer: ByteArray): ByteArray {
        var crc = 0
        for (element in buffer) {
            crc = crc.ushr(8) or (crc shl 8) and 0xffff
            crc = crc xor (element.toInt() and 0xff)//byte to int, trunc sign
            crc = crc xor (crc and 0xff shr 4)
            crc = crc xor (crc shl 12 and 0xffff)
            crc = crc xor (crc and 0xFF shl 5 and 0xffff)
        }

        crc = crc and 0xffff
        val data = crc.toByteArray()
        return byteArrayOf(data[2], data[3])
    }

}