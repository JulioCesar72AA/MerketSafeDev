package mx.softel.cirwirelesslib.utils

import android.util.Log
import mx.softel.cirwirelesslib.extensions.toByteArray
import mx.softel.cirwirelesslib.extensions.toHex
import kotlin.experimental.and


object CommandUtils {

    private val TAG = CommandUtils::class.java.simpleName

    private val POLEO           = byteArrayOf(0x55, 0x10, 0x13, 0x07, 0xC5.toByte())
    private val REFRESH_AP      = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x47)
    private val GET_AP_MAC_LIST = byteArrayOf(0x55, 0x13, 0x10, 0x07, 0x49)


    fun refreshAccessPointsCmd(): ByteArray {
        Log.d(TAG, "refreshAccessPointCmd")

        val crc = getCrc16(REFRESH_AP)
        var cmd = REFRESH_AP
        cmd += crc[0]
        cmd += crc[1]

        return cmd
    }

    fun getAccessPointsCmd(): ByteArray {
        Log.d(TAG, "getAccessPointsCmd")

        val crc = getCrc16(GET_AP_MAC_LIST)
        var cmd = GET_AP_MAC_LIST
        cmd += crc[0]
        cmd += crc[1]

        return cmd
    }



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