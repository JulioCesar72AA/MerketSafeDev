package mx.softel.cirwireless.web_services_module

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "SolkosServerResponse"


class SolkosServerResponse (val response: JSONObject) {

    var token                       : String
    var decodeTokenBody             : String
    var organizationName            : String
    var organizationType            : String
    var email                       : String
    var name                        : String
    var permissions                 : JSONArray
    var tokenCreation               : String
    var permissionsStr              : String

    var organizationId  : Int = -1
    var userId          : Int = -1


    init {
        this.token              = response.get("token") as String
        this.decodeTokenBody    = JWTUtils.decoded(this.token)
        this.tokenCreation      = JSONObject(this.decodeTokenBody).get("created_at") as String
        this.organizationId     = response.get("organization_id") as Int
        this.organizationName   = response.get("organization_name") as String
        this.organizationType   = response.get("organization_type") as String
        this.userId             = response.get("user_id") as Int
        this.email              = response.get("email") as String
        this.name               = response.get("name") as String
        this.permissions        = response.get("permissions") as JSONArray
        this.permissionsStr     = permissionsToStr()
        Log.e(TAG, "permissionsStr: $permissionsStr")
    }

    private fun permissionsToStr () : String {
        val permissionsArray = Array(this.permissions.length()) {
            permissions.getString(it)
        }

        var permissionStr = ""
        for (permission in permissionsArray)
            permissionStr += permission + ","

        return permissionStr
    }
}