package fr.hureljeremy.gitea.ecoplant.services


import android.content.Intent
import android.net.Uri
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import fr.hureljeremy.gitea.ecoplant.utils.PictureType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException


@ServiceProvider
class PlantNetService : BaseService() {
    private val API_URL = "https://my-api.plantnet.org/"
    private lateinit var API_KEY: String
    private val storage = Firebase.storage
    private val functions = Firebase.functions

    override fun onCreate() {
        super.onCreate()
        // Access resources through context
        API_KEY = this.resources.getString(R.string.plantnet_api_key)
        API_KEY.replace("PLANTNET_API_KEY", "") // tempo api key for development
    }

    fun identifyPlant(imagePath: String, type: PictureType): String {
        val client = OkHttpClient()
        val file = File(imagePath)

        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart(
                "images", file.name, file.asRequestBody("image/*".toMediaType())
            ).addFormDataPart("organs", type.toString().lowercase())
            .addFormDataPart("project", "all").addFormDataPart("includeRelatedImages", "true")
            .addFormDataPart("noReject", "true").addFormDataPart("lang", "en")
            .addFormDataPart("bestMatch", "true").build()

        val request =
            Request.Builder().url("${API_URL}v2/identify/all").addHeader("Api-Key", API_KEY)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "multipart/form-data").post(requestBody).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected response $response")
                response.body?.string() ?: ""

            }

        } catch (e: Exception) {
            e.printStackTrace()
            "{\"error\": \"${e.message}\"}"
        }
    }

    private val BASE_URL = "https://www.tela-botanica.org/?s="

    fun displayPlantDetails(plant_name: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("${BASE_URL}${plant_name}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
    }
}