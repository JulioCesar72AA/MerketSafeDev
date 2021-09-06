package mx.softel.cirwireless.dialogs

import android.util.Patterns

class IpConfigModel (private val ipAddress: String, private val maskAddress: String, private val gateway: String) {


    public fun ipAddress () : String = ipAddress


    public fun maskAddress () : String = maskAddress


    public fun gateway () : String = gateway


    public fun isAValidIpAddress () : Boolean = Patterns.IP_ADDRESS.matcher(ipAddress).matches()


    public fun isAValidMaskAddress () : Boolean = Patterns.IP_ADDRESS.matcher(maskAddress).matches()


    public fun isAValidGateway () : Boolean = Patterns.IP_ADDRESS.matcher(gateway).matches()


    public fun allParametersAreCorrect () : Boolean = isAValidIpAddress() && isAValidMaskAddress() && isAValidGateway()
}