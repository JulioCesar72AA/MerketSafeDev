package mx.softel.cirwireless.log_in_module.web_service

import com.google.gson.annotations.SerializedName

data class ScanPostResponse (
    @SerializedName("serial_number")
    var serialNumber: String,

    @SerializedName("asset_type")
    var assetType: String,

    @SerializedName("asset_model")
    var assetModel: String,

    @SerializedName("is_transmitting")
    var isTransmitting: Boolean
)