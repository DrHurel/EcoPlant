package fr.hureljeremy.gitea.ecoplant.services


import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.AppDatabase
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.Organ
import fr.hureljeremy.gitea.ecoplant.framework.PlantNetClient
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.framework.ServiceEntry
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


@ServiceProvider
class PlantNetService : BaseService() {
    private lateinit var API_KEY: String
    private lateinit var client: PlantNetClient
    private val storage = Firebase.storage
    private val functions = Firebase.functions
    private var isIdentifing = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()

        API_KEY = this.resources.getString(R.string.plantnet_api_key)
        client = PlantNetClient("2b10KhKYpR6P3Y4Y29bvfFHG", true)  // temp api key for development
    }


    suspend fun identifyPlant(imageUri: Uri, type: Organ): Result<SavedIdentificationResult> {
        if (isIdentifing.get()) {
            Log.w("PlantNetService", "Identification already in progress")
            return Result.failure(IllegalStateException("Identification already in progress"))
        }
        isIdentifing.set(true)

        return try {

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

            tempFile.delete()

            response.fold(
                onSuccess = { result ->
                    result.results.firstOrNull()?.species?.genus?.let { genus ->
                        Log.d("PlantNetService", "Identified genus: $genus")
                    }
                    val name = result.results.firstOrNull()?.species?.scientificNameWithoutAuthor
                        ?: "Unknown plant"
                    val description =
                        "$name is a plant of the family ${result.results.firstOrNull()?.species?.family?.scientificNameWithoutAuthor ?: "Unknown family"}. It is commonly known as " +
                                (result.results.firstOrNull()?.species?.commonNames?.firstOrNull()
                                    ?: "Unknown common name") + "."
                    result.results.firstOrNull()?.species?.commonNames?.firstOrNull()
                        ?: "Unknown plant"

                    Result.success(
                        SavedIdentificationResult(
                            species = name,
                            date = System.currentTimeMillis().toString(),
                            description = description,
                            imageUri = imageUri
                        )
                    )

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


    suspend fun getPlantScore(
        plantName: String,
        relability: Double = 0.0
    ): Result<List<ServiceEntry>> {
        return withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getInstance(applicationContext)
                val serviceDao = database.serviceDao()
                val entries = serviceDao.getBySpecies(plantName, relability)
                Result.success(entries)
            } catch (e: Exception) {
                Log.e(
                    "PlantNetService",
                    "Erreur lors de la récupération des scores de la plante",
                    e
                )
                Result.failure(e)
            }
        }
    }

}