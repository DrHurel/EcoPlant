package fr.hureljeremy.gitea.ecoplant.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import fr.hureljeremy.gitea.ecoplant.utils.PictureType

class PlantNetService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    fun identifyPlant(imagePath: String,type : PictureType): String {
        // TODO("Not yet implemented")
        // Call the PlantNet API to identify the plant
        // Return the result as a string
        return ""
    }

    fun displayPlantDetails(plantId: String): String {
        // TODO("Not yet implemented")
        // Call the PlantNet API to get the plant details
        // Return the result as a string
        return ""
    }
}