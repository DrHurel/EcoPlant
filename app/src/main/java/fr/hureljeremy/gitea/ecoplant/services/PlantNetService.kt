package fr.hureljeremy.gitea.ecoplant.services


import android.content.Intent
import android.net.Uri
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.Organ
import fr.hureljeremy.gitea.ecoplant.framework.PlantNetClient
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


@ServiceProvider
class PlantNetService : BaseService() {
    private val API_URL = "https://my-api.plantnet.org/"
    private lateinit var API_KEY: String
    private lateinit var client: PlantNetClient
    private val storage = Firebase.storage
    private val functions = Firebase.functions
    private var isIdentifing = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        // Access resources through context
        API_KEY = this.resources.getString(R.string.plantnet_api_key)
        API_KEY.replace(
            "PLANTNET_API_KEY",
            "2b10KhKYpR6P3Y4Y29bvfFHG"
        ) // tempo api key for development
        client = PlantNetClient(API_KEY, true)
    }

    @Throws(Exception::class)
    suspend fun identifyPlant(imagePath: Uri, type: Organ): String {
        if (isIdentifing.get()) {
            return "Identification in progress, please wait."
        }
        isIdentifing.set(true)

        return try {
            val path = File(imagePath.path ?: throw IllegalArgumentException("Invalid image path"))
            val result = client.identifyPlantFromFiles(imageFiles = listOf(path), organs = listOf(type))
            result.fold(
                onSuccess = { response ->
                    response.results.firstOrNull()?.species?.scientificName ?: "Unknown plant"
                },
                onFailure = { "No plant identified" }
            )
        } catch (e: Exception) {
            "Error identifying plant: ${e.message}"
        } finally {
            isIdentifing.set(false)
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