package mx.softel.cirwirelesslib.extensions

fun ByteArray.toHex(): String {
    return joinToString(" ") { "%02x".format(it) }
}


fun Int.toByteArray(isBigEndian: Boolean = true): ByteArray {
    var bytes = byteArrayOf()
    var n = this
    if (n == 0 || n == -1) {
        bytes += n.toByte()
    } else if (n > 0) {
        while (n != 0) {
            val b = n.and(0xFF).toByte()

            bytes += b

            n = n.shr(Byte.SIZE_BITS)
        }
    } else {
        while (n != -1) {
            val b = n.and(0xFF).toByte()
            bytes += b
            n = n.shr(Byte.SIZE_BITS)
        }
    }
    val padding = if (n < 0) { 0xFF.toByte() } else { 0x00.toByte() }
    var paddings = byteArrayOf()
    repeat(Int.SIZE_BYTES - bytes.count()) {
        paddings += padding
    }
    return if (isBigEndian) {
        paddings + bytes.reversedArray()
    } else {
        paddings + bytes
    }
}