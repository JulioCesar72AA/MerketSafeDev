package mx.softel.marketsafe.web_services_module.web_service

import com.google.gson.annotations.SerializedName

data class LastLoginGetRequest(
    @SerializedName("variable_name")
    var variable_name: String,

    @SerializedName("status")
    var status: String,

    @SerializedName("timestamp")
    var timestamp: String,

    @SerializedName("mac")
    var mac: String
)
