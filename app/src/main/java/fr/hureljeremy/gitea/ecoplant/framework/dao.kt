package fr.hureljeremy.gitea.ecoplant.framework


import android.content.Context
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import fr.hureljeremy.gitea.ecoplant.R
import java.io.File
import java.io.FileOutputStream

class Converters {
    @TypeConverter
    fun fromString(value: String?): Uri? {
        return value?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }
}

@Entity(
    tableName = "plant_score",
    primaryKeys = ["service", "species", "cultural_condition"]
)
data class ServiceEntry(
    val service: String,
    val species: String,
    val value: Double,
    val reliability: Double,
    @ColumnInfo(name = "cultural_condition")
    val culturalCondition: String
)

// Modifier l'entité pour supprimer la relation
@Entity(
    tableName = "identification_results",
    primaryKeys = ["species", "date"]
)
data class SavedIdentificationResult(
    val species: String,
    val date: String,
    val description: String,
    @ColumnInfo(name = "image_uri")
    val imageUri: Uri
    // Suppression de la relation ici
)

// Créer une classe distincte pour la relation
data class SavedIdentificationWithServices(
    @Embedded val identification: SavedIdentificationResult,
    @Relation(
        parentColumn = "species",
        entityColumn = "species"
    )
    val services: List<ServiceEntry>
)

@Entity(
    tableName = "parcel_items",
)
data class ParcelItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val minimumReliabilityScore: Double = 50.0,
    val isPublic: Boolean = false,
    val latitude: String = "",
    val longitude: String = "",
) {

}

@Entity(
    tableName = "parcel_item_results",
    primaryKeys = ["parcelId", "species", "date"]
)
data class ParcelItemResultCrossRef(
    val parcelId: Long,
    val species: String,
    val date: String
)


data class ParcelWithResults(
    @Embedded val parcel: ParcelItem,

    @Relation(
        parentColumn = "id",
        entityColumn = "species",  // Must match entity's key
        associateBy = Junction(
            value = ParcelItemResultCrossRef::class,
            parentColumn = "parcelId",
            entityColumn = "species" // Room will use this with additional filtering
        )
    )
    val services: List<SavedIdentificationResult>

)


@Dao
interface ServiceDao {
    @Query("SELECT * FROM plant_score")
    fun getAll(): List<ServiceEntry>

    @Query("SELECT * FROM plant_score WHERE species = :species AND reliability >= :reliability")
    fun getBySpecies(species: String, reliability: Double): List<ServiceEntry>

    @Query("SELECT service FROM plant_score GROUP BY service ")
    fun getAllServiceNames(): List<String>

    @Query("SELECT * FROM identification_results WHERE species = :species AND date = :date")
    fun getIdentificationResult(species: String, date: String): SavedIdentificationResult?

    @Query("SELECT * FROM identification_results WHERE species = :species")
    fun getIdentificationResultsBySpecies(species: String): List<SavedIdentificationResult>

    @Query("SELECT * FROM parcel_items")
    fun getAllParcels(): List<ParcelItem>

    @Query("SELECT * FROM parcel_items WHERE id = :id")
    fun getParcelById(id: Long): ParcelWithResults?

    @Query("SELECT * FROM parcel_items WHERE isPublic = 1")
    fun getPublicParcels(): List<ParcelItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertParcel(parcel: ParcelItem): Long

    @Update
    fun updateParcel(parcel: ParcelItem): Int


    @Delete
    fun deleteParcel(parcel: ParcelItem): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertIdentificationResult(result: SavedIdentificationResult)

    @Update
    fun updateIdentificationResult(result: SavedIdentificationResult): Int

    @Delete
    fun deleteIdentificationResult(result: SavedIdentificationResult): Int


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCrossRef(crossRef: ParcelItemResultCrossRef)

    @Query("SELECT * FROM parcel_item_results WHERE parcelId = :parcelId")
    fun getCrossRefsByParcelId(parcelId: Long): List<ParcelItemResultCrossRef>

    @Delete
    fun deleteCrossRef(crossRef: ParcelItemResultCrossRef): Int

    @Query("SELECT * FROM parcel_items LIMIT :batchSize OFFSET :offset")
    fun getParcelsPaginated(offset: Int, batchSize: Int): List<ParcelItem>

    @Transaction
    @Query("SELECT * FROM identification_results WHERE species = :species AND date = :date")
    fun getIdentificationWithServices(
        species: String,
        date: String
    ): SavedIdentificationWithServices

    @Query(
        "SELECT r.* FROM identification_results r " +
                "JOIN parcel_item_results cr ON r.species = cr.species AND r.date = cr.date " +
                "WHERE cr.parcelId = :parcelId"
    )
    fun getIdentificationResultsForParcel(parcelId: Long): List<SavedIdentificationResult>


    @Transaction
    fun getParcelServices(parcelId: Long): List<ServiceEntry> {

        val identifications = getIdentificationResultsForParcel(parcelId)


        val allServices = mutableListOf<ServiceEntry>()
        identifications.forEach { identification ->
            val speciesServices = getServicesForSpecies(identification.species)
            allServices.addAll(speciesServices)
        }

        return allServices.groupBy { it.service }.map { it.value.first() }
    }

    // Ajouter une méthode pour récupérer les services associés à une identification
    @Query("SELECT * FROM plant_score WHERE species = :species")
    fun getServicesForSpecies(species: String): List<ServiceEntry>
}


@Database(
    entities = [
        ServiceEntry::class,
        SavedIdentificationResult::class,
        ParcelItem::class,
        ParcelItemResultCrossRef::class
    ], version = 1, exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serviceDao(): ServiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbFile = File(context.getDatabasePath("score_name.db").path)



                if (!dbFile.exists()) {
                    copyDbFromRaw(context, R.raw.score_name, dbFile)
                }

                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "score_name.db"
                )
                    .createFromFile(dbFile)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun copyDbFromRaw(context: Context, rawResId: Int, outputFile: File) {
            context.resources.openRawResource(rawResId).use { inputStream ->
                outputFile.parentFile?.mkdirs()
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}

