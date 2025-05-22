package fr.hureljeremy.gitea.ecoplant

import android.app.Application
import android.util.Log
import fr.hureljeremy.gitea.ecoplant.framework.NavigationManager
import fr.hureljeremy.gitea.ecoplant.framework.ServiceLocator

class EcoPlant : Application() {
    override fun onCreate() {
        super.onCreate()

        Log.d("EcoPlant", "Application started")
        ServiceLocator.getInstance().initialize(
            this,
            "fr.hureljeremy.gitea.ecoplant.services",  // Add the correct package where NavigationService is located
            "fr.hureljeremy.gitea.ecoplant.framework"
        )
        NavigationManager.initialize(this, "fr.hureljeremy.gitea.ecoplant")

    }
}