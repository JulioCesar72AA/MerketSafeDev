package mx.softel.marketsafe.web_services_module.web_service

import com.google.gson.annotations.SerializedName

data class LinkPostResponse(
    @SerializedName("serial_number")
    var serialNumber: String,

    @SerializedName("asset_type")
    var assetType: String,

    @SerializedName("asset_model")
    var assetModel: String
)
