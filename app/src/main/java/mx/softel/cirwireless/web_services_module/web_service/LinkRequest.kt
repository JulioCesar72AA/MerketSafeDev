package mx.softel.cirwireless.web_services_module.web_service

import com.google.gson.annotations.SerializedName

data class LinkRequest(
    @SerializedName("mac")
    var mac: String,
)
