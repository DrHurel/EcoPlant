package fr.hureljeremy.gitea.ecoplant.services


import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import fr.hureljeremy.gitea.ecoplant.R
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import fr.hureljeremy.gitea.ecoplant.utils.PictureType


class PlantNetService : Service() {

    private final val API_URL = "https://my-api.plantnet.org/"
    private lateinit var API_KEY: String


    override fun onBind(intent: Intent): IBinder? {
        // This service doesn't support binding
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // Load API key from resources (you should store this in a more secure way in production)
        API_KEY = resources.getString(R.string.plantnet_api_key);
        API_KEY.replace("PLANTNET_API_KEY", ""); // tempo api key for development

    }

  fun identifyPlant(imagePath: String, type: PictureType): String {
        val client = OkHttpClient()
        val file = File(imagePath)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "images",
                file.name,
                file.asRequestBody("image/*".toMediaType())
            )
            .addFormDataPart("organs", type.toString().lowercase())
            .addFormDataPart("project", "all")
            .addFormDataPart("includeRelatedImages", "true")
            .addFormDataPart("noReject", "true")
            .addFormDataPart("lang", "en")
            .addFormDataPart("bestMatch", "true")
            .build()

        val request = Request.Builder()
            .url("${API_URL}v2/identify/all")
            .addHeader("Api-Key", API_KEY)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "multipart/form-data")
            .post(requestBody)
            .build()

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

    private final val BASE_URL = "https://www.tela-botanica.org/?s="

   fun displayPlantDetails(plant_name: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("${BASE_URL}${plant_name}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}