package mx.softel.cirwirelesslib.extensions

fun ByteArray.toHex(): String {
    return joinToString(" ") { "%02x".format(it) }
}

fun ByteArray.toCharString(): String {
    return joinToString ("") { it.toChar().toString() }
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


private const val HEX_CHARS = "0123456789ABCDEF"

fun String.hexStringToByteArray() : ByteArray {

    val clean = this.replace(":", "")
    val result = ByteArray(clean.length / 2)

    for (i in 0 until clean.length step 2) {
        val firstIndex = HEX_CHARS.indexOf(clean[i]);
        val secondIndex = HEX_CHARS.indexOf(clean[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}