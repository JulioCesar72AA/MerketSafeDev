package mx.softel.marketsafe.web_services_module.web_service

import com.google.gson.annotations.SerializedName

data class LoginRequest (
    @SerializedName("email")
    var email: String,

    @SerializedName("user_device_id_type")
    var userId: String,

    @SerializedName("user_device_id")
    var userIdValue: String
)