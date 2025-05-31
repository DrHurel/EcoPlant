package fr.hureljeremy.gitea.ecoplant.services

import fr.hureljeremy.gitea.ecoplant.framework.AppDatabase
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItemResultCrossRef
import fr.hureljeremy.gitea.ecoplant.framework.ParcelWithResults
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider

@ServiceProvider
class ParcelService : BaseService() {
    private val database by lazy { AppDatabase.getInstance(this) }
    private val dao by lazy { database.serviceDao() }

    fun loadEditableParcel(id: Int): ParcelItem? {
        return dao.getParcelById(id.toLong())?.parcel
    }

    fun updateParcel(parcel: ParcelItem): Boolean {
        return try {
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
        // Étape 1: Sauvegarder le résultat d'identification
        dao.insertIdentificationResult(identificationResult)

        // Étape 2: Créer une association via ParcelItemResultCrossRef
        val crossRef = ParcelItemResultCrossRef(
            parcelId = parcelId.toLong(),
            species = identificationResult.species,
            date = identificationResult.date
        )
        dao.insertCrossRef(crossRef)
    }

    fun getParcelWithResults(parcelId: Int): ParcelWithResults? {
        return dao.getParcelById(parcelId.toLong())
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

            currentBatch = service.dao.getParcelsPaginated(offset, BATCH_SIZE)
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
}