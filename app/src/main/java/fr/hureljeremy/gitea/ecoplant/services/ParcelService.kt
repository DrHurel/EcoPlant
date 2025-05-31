package fr.hureljeremy.gitea.ecoplant.services

import android.content.Context
import fr.hureljeremy.gitea.ecoplant.framework.AppDatabase
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItemResultCrossRef
import fr.hureljeremy.gitea.ecoplant.framework.ParcelWithResults
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import java.util.concurrent.atomic.AtomicBoolean

@ServiceProvider
class ParcelService : BaseService() {
    private var database: AppDatabase? = null
    private var initialized = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        initialize(this)
    }

    fun initialize(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            database = AppDatabase.getInstance(context)
        }
    }

    private fun getDao() = database?.serviceDao() ?: throw IllegalStateException("ParcelService n'est pas initialisé")

    fun loadEditableParcel(id: Int): ParcelItem? {
        return getDao().getParcelById(id.toLong())?.parcel
    }

    fun updateParcel(parcel: ParcelItem): Boolean {
        return try {
            val dao = getDao()
            val rowsUpdated = if (dao.getParcelById(parcel.id) != null) {
                dao.updateParcel(parcel)
            } else {
                dao.insertParcel(parcel)
                1
            }
            rowsUpdated > 0
        } catch (e: Exception) {
            false
        }
    }

    fun addIdentificationResult(parcelId: Int, identificationResult: SavedIdentificationResult) {
        val dao = getDao()
        dao.insertIdentificationResult(identificationResult)

        val crossRef = ParcelItemResultCrossRef(
            parcelId = parcelId.toLong(),
            species = identificationResult.species,
            date = identificationResult.date
        )
        dao.insertCrossRef(crossRef)
    }

    fun getParcelWithResults(parcelId: Int): ParcelWithResults? {
        return getDao().getParcelById(parcelId.toLong())
    }

    fun getParcels(): Iterator<ParcelItem> {
        return LazyParcelItemIterator(this)
    }

    private class LazyParcelItemIterator(private val service: ParcelService) :
        Iterator<ParcelItem> {
        private val BATCH_SIZE = 20
        private var currentBatch: List<ParcelItem> = emptyList()
        private var currentIndex = 0
        private var offset = 0
        private var hasMoreData = true

        init {
            loadNextBatch()
        }

        private fun loadNextBatch() {
            if (!hasMoreData) return

            currentBatch = service.getDao().getParcelsPaginated(offset, BATCH_SIZE)
            currentIndex = 0
            offset += currentBatch.size
            hasMoreData = currentBatch.size == BATCH_SIZE
        }

        override fun hasNext(): Boolean {
            if (currentIndex >= currentBatch.size && hasMoreData) {
                loadNextBatch()
            }
            return currentIndex < currentBatch.size
        }

        override fun next(): ParcelItem {
            if (!hasNext()) {
                throw NoSuchElementException("No more ParcelItems available")
            }
            return currentBatch[currentIndex++]
        }
    }

    fun deleteParcel(parcel: ParcelItem): Boolean {
        return try {
            val dao = getDao()
            dao.deleteParcel(parcel)
            true
        } catch (e: Exception) {
            false
        }
    }

fun getService(parcel: ParcelItem): List<SavedIdentificationResult> {
    // Récupérer la parcelle avec ses résultats associés
    val parcelWithResults = getDao().getParcelById(parcel.id) ?: return emptyList()

    // Retourner la liste des résultats d'identification (services)
    return parcelWithResults.services
}
}