package mx.softel.cirwirelesslib.utils

import android.util.Log
import mx.softel.cirwirelesslib.extensions.toByteArray
import mx.softel.cirwirelesslib.extensions.toHex


object CommandUtils {

    private val TAG = CommandUtils::class.java.simpleName

    private val POLEO               = byteArrayOf(0x55, 0x10, 0x13, 0x07, 0xC5.toByte())
    private val REFRESH_AP          = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x47)
    private val GET_AP_MAC_LIST     = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x49)
    private val AT_GENERIC          = byteArrayOf(0x55, 0x13, 0x10, 0x00, 0x4B)
    private val AT_READ             = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x34)


    fun refreshAccessPointsCmd(): ByteArray {
        Log.d(TAG, "refreshAccessPointCmd")
        return REFRESH_AP + getCrc16(REFRESH_AP)
    }

    fun getAccessPointsCmd(): ByteArray {
        Log.d(TAG, "getAccessPointsCmd")
        return GET_AP_MAC_LIST + getCrc16(GET_AP_MAC_LIST)
    }

    /*fun configureAccessPointsCmd(): ByteArray {
        Log.d(TAG, "getAccessPointsCmd")

        val atCmd = "AT+CWLAPOPT=1,8".toByteArray()
        val size = atCmd.size + 1 + 7
        Log.d(TAG, "AT_COMMAND = ${atCmd.toHex()}")

        var cmd = AT_GENERIC + atCmd + 0x00.toByte()
        cmd[3] = size.toByte()
        Log.d(TAG, "COMANDO SIN CRC = ${cmd.toHex()}")

        val crc = getCrc16(cmd)
        cmd += crc
        Log.d(TAG, "COMANDO CON CRC = ${cmd.toHex()}")

        return cmd
    }

    fun readAtCmd(): ByteArray {
        Log.d(TAG, "readAtCmd")
        return AT_READ + getCrc16(AT_READ)
    }*/



    /************************************************************************************************/
    /**      CRC                                                                                    */
    /************************************************************************************************/
    private fun getCrc16(buffer: ByteArray): ByteArray {
        Log.d(TAG, "getCrc16")

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
        val byteArray = byteArrayOf(data[2], data[3])
        Log.d("BleService", "${POLEO.toHex()} ${byteArray.toHex()}")
        return byteArray
    }

}