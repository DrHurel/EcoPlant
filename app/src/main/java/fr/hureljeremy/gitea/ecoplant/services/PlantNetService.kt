package fr.hureljeremy.gitea.ecoplant.services


import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.Organ
import fr.hureljeremy.gitea.ecoplant.framework.PlantNetClient
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
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
        client = PlantNetClient("2b10KhKYpR6P3Y4Y29bvfFHG", true)
    }


    data class PlantIdentificationResult(
        val name: String,
        val description: String
    )

    suspend fun identifyPlant(imageUri: Uri, type: Organ): Result<PlantIdentificationResult> {
        if (isIdentifing.get()) {
            Log.w("PlantNetService", "Identification already in progress")
            return Result.failure(IllegalStateException("Identification already in progress"))
        }
        isIdentifing.set(true)

        return try {
            // Create a temporary file from the content URI
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("plant_image", ".jpg", applicationContext.cacheDir)
            }
            applicationContext.contentResolver.openInputStream(imageUri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val response = client.identifyPlantFromFiles(
                imageFiles = listOf(tempFile),
                organs = listOf(type),
                maxResults = 1
            )

            // Delete the temporary file
            tempFile.delete()

            response.fold(
                onSuccess = { result ->
                    result.results.firstOrNull()?.species?.genus?.let { genus ->
                        Log.d("PlantNetService", "Identified genus: $genus")
                    }
                    val name = result.results.firstOrNull()?.species?.scientificNameWithoutAuthor ?: "Unknown plant"
                    val description  = "$name is a plant of the family ${result.results.firstOrNull()?.species?.family ?: "Unknown family"}. It is commonly known as " +
                        (result.results.firstOrNull()?.species?.commonNames?.firstOrNull() ?: "Unknown common name") + "."
                    result.results.firstOrNull()?.species?.commonNames?.firstOrNull() ?: "Unknown plant"

                    Result.success(PlantIdentificationResult(name, description))

                 },
                onFailure = {
                    Log.e("PlantNetService", "Identification failed", it)
                    Result.failure(it)
                }
            )
        } catch (e: Exception) {
            Log.e("PlantNetService", "Error during plant identification", e)
            Result.failure(e)
        } finally {
            isIdentifing.set(false)
        }
    }

    private val BASE_URL = "https://www.tela-botanica.org/?s="

    fun displayPlantDetails(plantName: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("${BASE_URL}${plantName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
    }

    suspend fun getPlantScore(plantName: String) {
        return withContext(Dispatchers.IO) {
            try {
                val data = hashMapOf("plant_name" to plantName)
                val result = functions
                    .getHttpsCallable("getPlantScore")
                    .call(data)
                    .await()
                result.data as String
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }
}