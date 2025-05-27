package fr.hureljeremy.gitea.ecoplant.framework

import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

// Data Classes (Models)
data class Status(
    val status: String
)

data class Project(
    val id: String,
    val title: String,
    val description: String,
    val speciesCount: Int
)

data class Species(
    val id: String,
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val gbifId: Long?,
    val powoId: String?,
    val iucnCategory: String?,
    val commonNames: List<String>,
    val genus: String,
    val family: String
)

data class Account(
    val id: String,
    val username: String,
    val name: Name,
    val created: String
)

data class Name(
    val first: String,
    val last: String
)

data class Contract(
    val plan: String,
    val status: String,
    val firstSignatureDate: String,
    val latestSignatureDate: String,
    val nextSignatureDate: String,
    val indicativeYearlyQuota: Long,
    val currency: String
)

data class Count(
    val identify: Long
)

data class AboveQuota(
    val identify: Long
)

data class HistoryPeriod(
    val period: String,
    val startDate: String,
    val endDate: String,
    val count: Count,
    val aboveQuota: AboveQuota
)

data class Billing(
    val nextDueDate: String,
    val estimatedAmount: Double
)

data class Security(
    val exposeKey: Boolean,
    val ips: List<String>,
    val domains: List<String>
)

data class Subscription(
    val account: Account,
    val contract: Contract,
    val history: List<HistoryPeriod>,
    val billing: Billing,
    val security: Security
)

data class PlantNetQuery(
    val project: String,
    val images: List<String>,
    val organs: List<String>,
    val includeRelatedImages: Boolean,
    val noReject: Boolean,
    val type: String
)

data class Genus(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String
)

data class Family(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String
)

data class SpeciesDetail(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String,
    val genus: Genus,
    val family: Family,
    val commonNames: List<String>
)

data class ImageDate(
    val timestamp: Long,
    val string: String
)

data class ImageUrl(
    val o: String,
    val m: String,
    val s: String
)

data class Image(
    val organ: String,
    val author: String,
    val license: String,
    val date: ImageDate,
    val citation: String,
    val url: ImageUrl
)

data class Gbif(
    val id: Long
)

data class Powo(
    val id: String
)

data class Iucn(
    val id: String,
    val category: String
)

data class Result(
    val score: Double,
    val species: SpeciesDetail,
    val images: List<Image>,
    val gbif: Gbif?,
    val powo: Powo?,
    val iucn: Iucn?
)

data class PredictedOrgan(
    val image: String,
    val filename: String,
    val organ: String,
    val score: Double
)

data class IdentificationResult(
    val query: PlantNetQuery,
    val language: String,
    val preferedReferential: String,
    val switchToProject: String?,
    val bestMatch: String,
    val results: List<Result>,
    val remainingIdentificationRequests: Long,
    val version: String,
    val predictedOrgans: List<PredictedOrgan>
)

data class Remaining(
    val identify: Long
)

data class QuotaInfo(
    val day: String,
    val count: Count,
    val remaining: Remaining
)

// Enums
enum class Language(val code: String) {
    EN("en"), FR("fr"), ES("es"), PT("pt"), DE("de"), IT("it"),
    AR("ar"), CS("cs"), NL("nl"), SK("sk"), ZH("zh"), RU("ru"),
    TR("tr"), PL("pl"), UK("uk"), HE("he"), EL("el"), FI("fi"),
    ID("id"), MS("ms"), CA("ca"), JA("ja"), HU("hu"), HR("hr"),
    DA("da"), RO("ro"), BG("bg"), NB("nb"), SL("sl"), SV("sv"),
    ET("et"), EU("eu"), UR("ur"), ML("ml"), CY("cy"), KU("ku"),
    GL("gl"), EO("eo"), SAT("sat"), ZH_TW("zh-tw"), PT_BR("pt-br"),
    HI("hi"), DE_AT("de-at"), SR("sr"), ZH_HANT("zh-hant"), BN("bn"),
    FA("fa"), BE("be"), OC("oc"), LT("lt"), EN_AU("en-au"), BR("br")
}

enum class ProjectType(val value: String) {
    KT("kt"), LEGACY("legacy")
}

enum class Organ(val value: String) {
    LEAF("leaf"), FLOWER("flower"), FRUIT("fruit"),
    BARK("bark"), AUTO("auto"), HABIT("habit"), OTHER("other")
}

interface PlantNetApiService {

    @GET("/v2/_status")
    suspend fun getStatus(): Response<Status>

    @GET("/v2/languages")
    suspend fun getLanguages(
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<List<String>>

    @GET("/v2/projects")
    suspend fun getProjects(
        @retrofit2.http.Query("lang") lang: String? = "en",
        @retrofit2.http.Query("lat") lat: Double? = null,
        @retrofit2.http.Query("lon") lon: Double? = null,
        @retrofit2.http.Query("type") type: String? = "kt",
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<List<Project>>

    @GET("/v2/species")
    suspend fun getSpecies(
        @retrofit2.http.Query("lang") lang: String? = "en",
        @retrofit2.http.Query("type") type: String? = "kt",
        @retrofit2.http.Query("pageSize") pageSize: Int? = null,
        @retrofit2.http.Query("page") page: Int? = null,
        @retrofit2.http.Query("prefix") prefix: String? = null,
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<List<Species>>

    @GET("/v2/subscription")
    suspend fun getSubscription(
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<Subscription>

    @GET("/v2/identify/{project}")
    suspend fun identifyPlantByUrl(
        @Path("project") project: String = "all",
        @retrofit2.http.Query("images") images: List<String>,
        @retrofit2.http.Query("organs") organs: List<String>? = null,
        @retrofit2.http.Query("include-related-images") includeRelatedImages: Boolean? = false,
        @retrofit2.http.Query("no-reject") noReject: Boolean? = false,
        @retrofit2.http.Query("nb-results") nbResults: Int? = 10,
        @retrofit2.http.Query("lang") lang: String? = "en",
        @retrofit2.http.Query("type") type: String? = null,
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<IdentificationResult>

    @Multipart
    @POST("/v2/identify/{project}")
    suspend fun identifyPlantByUpload(
        @Path("project") project: String = "all",
        @Part images: List<MultipartBody.Part>,
        @Part("organs") organs: List<RequestBody>? = null,
        @retrofit2.http.Query("include-related-images") includeRelatedImages: Boolean? = false,
        @retrofit2.http.Query("no-reject") noReject: Boolean? = false,
        @retrofit2.http.Query("nb-results") nbResults: Int? = 10,
        @retrofit2.http.Query("lang") lang: String? = "en",
        @retrofit2.http.Query("type") type: String? = null,
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<IdentificationResult>

    @GET("/v2/quota/daily")
    suspend fun getDailyQuota(
        @retrofit2.http.Query("day") day: String? = null,
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<QuotaInfo>

    @GET("/v2/quota/history")
    suspend fun getQuotaHistory(
        @retrofit2.http.Query("year") year: String? = null,
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<List<QuotaInfo>>

    @GET("/v2/projects/{project}/species")
    suspend fun getSpeciesByProject(
        @Path("project") project: String,
        @retrofit2.http.Query("lang") lang: String? = "en",
        @retrofit2.http.Query("pageSize") pageSize: Int? = null,
        @retrofit2.http.Query("page") page: Int? = null,
        @retrofit2.http.Query("prefix") prefix: String? = null,
        @retrofit2.http.Query("api-key") apiKey: String? = null
    ): Response<List<Species>>
}


class PlantNetClient(
    private val apiKey: String? = null,
    private val enableLogging: Boolean = false
) {
    private val apiService: PlantNetApiService

    init {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            httpClient.addInterceptor(loggingInterceptor)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://my-api.plantnet.org/")
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(PlantNetApiService::class.java)
    }

    // Convenience methods
    suspend fun checkStatus(): kotlin.Result<Status> = safeApiCall {
        apiService.getStatus()
    }

    suspend fun getAvailableLanguages(): kotlin.Result<List<String>> = safeApiCall {
        apiService.getLanguages(apiKey)
    }

    suspend fun getProjects(
        language: Language = Language.EN,
        latitude: Double? = null,
        longitude: Double? = null,
        type: ProjectType = ProjectType.KT
    ): kotlin.Result<List<Project>> = safeApiCall {
        apiService.getProjects(language.code, latitude, longitude, type.value, apiKey)
    }

    suspend fun getAllSpecies(
        language: Language = Language.EN,
        type: ProjectType = ProjectType.KT,
        pageSize: Int? = null,
        page: Int? = null,
        prefix: String? = null
    ): kotlin.Result<List<Species>> = safeApiCall {
        apiService.getSpecies(language.code, type.value, pageSize, page, prefix, apiKey)
    }

    suspend fun getSpeciesForProject(
        projectId: String,
        language: Language = Language.EN,
        pageSize: Int? = null,
        page: Int? = null,
        prefix: String? = null
    ): kotlin.Result<List<Species>> = safeApiCall {
        apiService.getSpeciesByProject(projectId, language.code, pageSize, page, prefix, apiKey)
    }

    suspend fun getSubscriptionInfo(): kotlin.Result<Subscription> = safeApiCall {
        apiService.getSubscription(apiKey)
    }

    suspend fun identifyPlantFromUrls(
        imageUrls: List<String>,
        organs: List<Organ>? = null,
        project: String = "all",
        includeRelatedImages: Boolean = false,
        noReject: Boolean = false,
        maxResults: Int = 10,
        language: Language = Language.EN,
        type: ProjectType? = null
    ): kotlin.Result<IdentificationResult> = safeApiCall {
        apiService.identifyPlantByUrl(
            project = project,
            images = imageUrls,
            organs = organs?.map { it.value },
            includeRelatedImages = includeRelatedImages,
            noReject = noReject,
            nbResults = maxResults,
            lang = language.code,
            type = type?.value,
            apiKey = apiKey
        )
    }

    suspend fun identifyPlantFromFiles(
        imageFiles: List<File>,
        organs: List<Organ>? = null,
        project: String = "all",
        includeRelatedImages: Boolean = false,
        noReject: Boolean = false,
        maxResults: Int = 10,
        language: Language = Language.EN,
        type: ProjectType? = null
    ): kotlin.Result<IdentificationResult> = safeApiCall {
        val imageParts = imageFiles.mapIndexed { index, file ->
            val requestBody = file.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("images", file.name, requestBody)
        }

        val organParts = organs?.map { organ ->
            organ.value.toRequestBody("text/plain".toMediaType())
        }

        apiService.identifyPlantByUpload(
            project = project,
            images = imageParts,
            organs = organParts,
            includeRelatedImages = includeRelatedImages,
            noReject = noReject,
            nbResults = maxResults,
            lang = language.code,
            type = type?.value,
            apiKey = apiKey
        )
    }

    suspend fun getDailyQuota(day: String? = null): kotlin.Result<QuotaInfo> = safeApiCall {
        apiService.getDailyQuota(day, apiKey)
    }

    suspend fun getQuotaHistory(year: String? = null): kotlin.Result<List<QuotaInfo>> = safeApiCall {
        apiService.getQuotaHistory(year, apiKey)
    }

    // Error handling wrapper
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): kotlin.Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    kotlin.Result.success(body)
                } ?: kotlin.Result.failure(Exception("Empty response body"))
            } else {
                kotlin.Result.failure(PlantNetException(response.code(), response.message()))
            }
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
}

// Custom Exception
class PlantNetException(
    val code: Int,
    message: String
) : Exception("HTTP $code: $message")

// Usage Example
/*
class PlantNetUsageExample {
    private val client = PlantNetClient(
        apiKey = "your-api-key-here",
        enableLogging = true
    )

    suspend fun example() {
        // Check API status
        client.checkStatus().fold(
            onSuccess = { status -> println("API Status: ${status.status}") },
            onFailure = { error -> println("Error: ${error.message}") }
        )

        // Get available projects
        client.getProjects().fold(
            onSuccess = { projects ->
                println("Found ${projects.size} projects")
                projects.forEach { println("- ${it.title}") }
            },
            onFailure = { error -> println("Error: ${error.message}") }
        )

        // Identify plant from URLs
        val imageUrls = listOf("https://example.com/plant-image.jpg")
        val organs = listOf(Organ.LEAF)

        client.identifyPlantFromUrls(
            imageUrls = imageUrls,
            organs = organs,
            maxResults = 5
        ).fold(
            onSuccess = { result ->
                println("Best match: ${result.bestMatch}")
                result.results.forEach { r ->
                    println("${r.species.scientificName} - Score: ${r.score}")
                }
            },
            onFailure = { error -> println("Identification failed: ${error.message}") }
        )

        // Identify plant from local files
        val imageFiles = listOf(File("path/to/plant-photo.jpg"))

        client.identifyPlantFromFiles(
            imageFiles = imageFiles,
            organs = listOf(Organ.FLOWER),
            project = "k-world-flora"
        ).fold(
            onSuccess = { result ->
                println("Identification complete!")
                result.results.take(3).forEach { r ->
                    println("${r.species.scientificName}: ${(r.score * 100).toInt()}%")
                }
            },
            onFailure = { error -> println("Error: ${error.message}") }
        )
    }
}
*/