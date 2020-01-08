package mx.softel.cirwirelesslib.utils

import android.util.Log
import kotlin.experimental.and


object CommandUtils {

    private val TAG = CommandUtils::class.java.simpleName

    fun getCrc16(buffer: ByteArray): Int {
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
        return crc
    }

}