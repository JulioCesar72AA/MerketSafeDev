package mx.softel.scanblelib.extensions

/**
 * Genera el formato "0xFF" en cada byte del arreglo
 *
 * @return Cadena formateada como 0xFF
 */
fun ByteArray.toHexValue(): String {
    val sb = StringBuffer()
    for (byte in this) {
        sb.append(String.format("%02x", byte))
    }
    return sb.toString()
}