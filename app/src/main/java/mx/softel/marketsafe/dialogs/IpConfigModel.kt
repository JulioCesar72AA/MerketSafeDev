package mx.softel.marketsafe.dialogs

import android.util.Patterns

class IpConfigModel (private val ipAddress: String, private val maskAddress: String, private val gateway: String) {
/*    private var dhcp    : Boolean = false
    private var static  : Boolean = false*/

    public fun ipAddress () : String = ipAddress

    public fun maskAddress () : String = maskAddress

    public fun gateway () : String = gateway

    public fun isAValidIpAddress () : Boolean = Patterns.IP_ADDRESS.matcher(ipAddress).matches()

    public fun isAValidMaskAddress () : Boolean = Patterns.IP_ADDRESS.matcher(maskAddress).matches()

    public fun isAValidGateway () : Boolean = Patterns.IP_ADDRESS.matcher(gateway).matches()

    public fun allParametersAreCorrect () : Boolean = isAValidIpAddress() && isAValidMaskAddress() && isAValidGateway()

/*    public fun getDHCP () : Boolean = dhcp

    public fun getStatic () : Boolean = static

    public fun setDHCP (dhcpValue: Boolean) { dhcp = dhcpValue }

    public fun setStatic (staticValue: Boolean) { static = staticValue }*/
}
