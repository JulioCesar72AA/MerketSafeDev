package mx.softel.cirwirelesslib.extensions

fun ByteArray.toHex(): String {
    return joinToString(" ") { "%02x".format(it) }
}