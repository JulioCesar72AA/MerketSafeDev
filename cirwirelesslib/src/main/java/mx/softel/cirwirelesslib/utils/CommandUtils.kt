package mx.softel.cirwirelesslib.utils

import mx.softel.cirwirelesslib.extensions.toByteArray

object CommandUtils {

    private val TAG = CommandUtils::class.java.simpleName

    private val REFRESH_AP          = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x47)
    private val GET_AP_MAC_LIST     = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x49)
    private val SET_SSID            = byteArrayOf(0x55, 0x13, 0x10, 0x00, 0x21)
    private val SET_PASSWORD        = byteArrayOf(0x55, 0x13, 0x10, 0x00, 0x24)
    private val WIFI_STATUS         = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x27)
    private val AT_GENERIC          = byteArrayOf(0x55, 0x13, 0x10, 0x00, 0x4B)
    private val AT_READ             = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x34)


    fun refreshAccessPointsCmd(): ByteArray
            = REFRESH_AP + getCrc16(REFRESH_AP)

    fun getAccessPointsCmd(): ByteArray
            = GET_AP_MAC_LIST + getCrc16(GET_AP_MAC_LIST)

    fun getWifiStatusCmd(): ByteArray
            = WIFI_STATUS + getCrc16(WIFI_STATUS)

    fun setSsidCmd(ssid: String): ByteArray {
        // Iniciamos el cálculo del tamaño, así como la conversión a bytes del SSID
        val nameBytes = ssid.toByteArray()
        val size = SET_SSID.size + nameBytes.size + 2
        var cmd = SET_SSID + nameBytes
        cmd[3] = size.toByte()

        // Calculamos el CRC del comando completo
        val crc = getCrc16(cmd)

        // Lo concatenamos con el comando final
        cmd += crc
        return cmd
    }

    fun setPasswordCmd(password: String): ByteArray {
        // Iniciamos el cálculo del tamaño, así como la conversión a bytes del password
        val passBytes = password.toByteArray()
        val size = SET_PASSWORD.size + passBytes.size + 2
        var cmd = SET_PASSWORD + passBytes
        cmd[3] = size.toByte()

        // Calculamos el CRC del comando completo
        val crc = getCrc16(cmd)

        // Lo concatenamos en el comando final
        cmd += crc
        return cmd
    }

    fun configureAccessPointCmd(ssid: String, password: String): ByteArray {
        val atCommand = "AT+CWJAP=\"$ssid\",\"$password\"".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun checkIpAddressCmd(): ByteArray {
        val atCommand = "AT+CIFSR".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun checkApConnectionCmd(): ByteArray {
        val atCommand = "AT+CWJAP?".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun pingApCmd(domain: String): ByteArray {
        val atCommand = "AT+PING=\"$domain\"".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun closeSocketCmd(): ByteArray {
        val atCommand = "AT+CIPCLOSE".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun openSocketCmd(server: String, port: String): ByteArray {
        val atCommand = "AT+CIPSTART=\"TCP\",\"$server\",$port".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun setDeviceWifiModeCmd(mode: Int): ByteArray {
        val atCommand = "AT+CWMODE=$mode".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun setInternalNameAPCmd(ssid: String, password: String, flag: Int): ByteArray {
        val atCommand = "AT+CWSAP=\"ID_$ssid\",\"$password\",6,0,4,$flag".toByteArray()
        val size = atCommand.size + 8
        var cmd = AT_GENERIC + atCommand + 0x00.toByte()
        cmd[3] = size.toByte()

        val crc = getCrc16(cmd)
        cmd += crc

        return cmd
    }

    fun readAtCmd(): ByteArray {
        return AT_READ + getCrc16(AT_READ)
    }



    /************************************************************************************************/
    /**      CRC                                                                                    */
    /************************************************************************************************/
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