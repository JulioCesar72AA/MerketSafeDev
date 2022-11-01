package mx.softel.cirwireless.web_services_module.web_service

import com.google.gson.annotations.SerializedName

data class TransmitPostResponse(
    @SerializedName("serial_number")
    var serialNumber: String,

    @SerializedName("last_timestamp")
    var lastTimestamp: String,

    @SerializedName("status")
    var status: String
)