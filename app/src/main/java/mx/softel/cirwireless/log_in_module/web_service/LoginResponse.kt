package mx.softel.cirwireless.log_in_module.web_service

import com.google.gson.annotations.SerializedName
import org.json.JSONArray

data class LoginResponse (
    @SerializedName("token")
    var token: String,

    @SerializedName("organization_id")
    var organization_id: Int,

    @SerializedName("organization_name")
    var organization_name: String,

    @SerializedName("email")
    var email: String,

    @SerializedName("name")
    var name: String,

    @SerializedName("permissions")
    var permissions: JSONArray
)