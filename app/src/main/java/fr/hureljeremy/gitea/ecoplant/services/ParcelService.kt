package fr.hureljeremy.gitea.ecoplant.services

import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.framework.ParcelWithResults
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider


@ServiceProvider
class ParcelService : BaseService() {


    fun loadEditableParcel(id : Int): ParcelItem? {
        // This method should return a ParcelItem that can be edited
        // For now, we return null as a placeholder
        return null
    }

    fun updateParcel(id : Int) {
    }

    fun addIdentificationResult(parcelId: Int, identificationResult: SavedIdentificationResult) {
    }





}