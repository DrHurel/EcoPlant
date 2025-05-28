package fr.hureljeremy.gitea.ecoplant.framework

import android.content.Context
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.hureljeremy.gitea.ecoplant.R
import java.io.File
import java.io.FileOutputStream


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

@Entity(
    tableName = "identification_results",
    primaryKeys = ["species", "date"]
)
data class SavedIdentificationResult(
    val species: String,
    val date: String,
    val description: String,
    @ColumnInfo(name = "image_uri")
    val imageUri: Uri,
)

@Dao
interface ServiceDao {
    @Query("SELECT * FROM plant_score")
    fun getAll(): List<ServiceEntry>

    @Query("SELECT * FROM plant_score WHERE species = :species AND reliability >= :reliability")
    fun getBySpecies(species: String, reliability: Double): List<ServiceEntry>

    @Query("SELECT service FROM plant_score GROUP BY service ")
    fun getAllServiceNames(): List<String>

}


@Database(entities = [ServiceEntry::class], version = 1, exportSchema = false)
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
