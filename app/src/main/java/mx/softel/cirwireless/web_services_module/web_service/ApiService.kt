package mx.softel.cirwireless.web_services_module.web_service

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

private const val TAG = "ScanPermission"

interface ApiService {

    @GET(URLs.LOGIN_URL)
    fun fetchLoginPost(): Call <LinkPostResponse>


    @Headers("Content-Type: application/json")
    @POST(URLs.SCAN_URL)
    fun fetchScanPost(@Header("Authorization") token: String, @Body macsBody : RequestBody): Call< List <ScanPostResponse> >


    @GET(URLs.STATUS_URL)
    fun fetchLinkPost(@Header("Authorization") token: String, @Query("mac") mac: String): Call <LinkPostResponse>
}