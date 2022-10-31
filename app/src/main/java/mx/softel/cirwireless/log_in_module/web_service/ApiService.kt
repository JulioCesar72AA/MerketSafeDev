package mx.softel.cirwireless.log_in_module.web_service

import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

private const val TAG = "ScanPermission"

interface ApiService {

    @GET(URLs.LOGIN_URL)
    fun fetchLoginPost(): Call<ScanPostResponse>

    @Headers("Content-Type: application/json")
    @POST(URLs.SCAN_URL)
    fun fetchScanPost(@Header("Authorization") token: String, @Body macsBody : RequestBody): Call< List <ScanPostResponse> >
}