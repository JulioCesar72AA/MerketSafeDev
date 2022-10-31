package mx.softel.cirwireless.web_services_module.web_service

import com.google.gson.annotations.SerializedName

data class ScanPostResponse (
    @SerializedName("mac")
    var mac: String,

    @SerializedName("serial_number")
    var serialNumber: String,

    @SerializedName("asset_type")
    var assetType: String,

    @SerializedName("asset_model")
    var assetModel: String,

    @SerializedName("is_transmitting")
    var isTransmitting: Boolean
)