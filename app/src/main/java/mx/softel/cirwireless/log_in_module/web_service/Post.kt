package mx.softel.cirwireless.log_in_module.web_service

import android.os.StrictMode
import android.util.Log
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


private const val SCAN_URL_SOLKOS_FRIDGES   = "https://cir-wifi-interface-b7agk5thba-uc.a.run.app/assets/scan"
private const val TAG                       = "ScanPermission"

// Endpoints
const val BASE_URL = "https://baseurl.com/"
const val LOGIN_URL = "auth/login"
const val POSTS_URL = "posts"

data class Post (
    @SerializedName("id")
    var id: Int,

    @SerializedName("title")
    var title: String,

    @SerializedName("description")
    var description: String,

    @SerializedName("content")
    var content: String
)