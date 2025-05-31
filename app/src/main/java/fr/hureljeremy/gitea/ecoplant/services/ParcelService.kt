package fr.hureljeremy.gitea.ecoplant.services

import android.content.Context
import fr.hureljeremy.gitea.ecoplant.framework.AppDatabase
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItemResultCrossRef
import fr.hureljeremy.gitea.ecoplant.framework.ParcelWithResults
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.framework.ServiceEntry
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private fun getDao() =
        database?.serviceDao() ?: throw IllegalStateException("ParcelService n'est pas initialisé")

    suspend fun loadEditableParcel(id: Int): ParcelItem? = withContext(Dispatchers.IO) {
        getDao().getParcelById(id.toLong())?.parcel
    }

    suspend fun updateParcel(parcel: ParcelItem): Boolean = withContext(Dispatchers.IO) {
        try {
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

    suspend fun addIdentificationResult(
        parcelId: Int,
        identificationResult: SavedIdentificationResult
    ) = withContext(Dispatchers.IO) {
        val dao = getDao()
        dao.insertIdentificationResult(identificationResult)

        val crossRef = ParcelItemResultCrossRef(
            parcelId = parcelId.toLong(),
            species = identificationResult.species,
            date = identificationResult.date
        )
        dao.insertCrossRef(crossRef)
    }

    suspend fun getParcelWithResults(parcelId: Int): ParcelWithResults? =
        withContext(Dispatchers.IO) {
            getDao().getParcelById(parcelId.toLong())
        }

    suspend fun getParcels(): List<ParcelItem> = withContext(Dispatchers.IO) {
        getDao().getAllParcels()
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

    suspend fun deleteParcel(parcel: ParcelItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val dao = getDao()
            dao.deleteParcel(parcel)
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun getService(parcel: ParcelItem): List<ServiceEntry> = withContext(Dispatchers.IO) {
        val dao = getDao()
        dao.getParcelServices(parcel.id)
    }

    suspend fun getIdentificationParcels(parcel: Long): List<SavedIdentificationResult> =
        withContext(Dispatchers.IO) {
            getDao().getIdentificationResultsForParcel(parcel)
        }
}