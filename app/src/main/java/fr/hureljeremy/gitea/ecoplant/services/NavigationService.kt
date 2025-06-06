package fr.hureljeremy.gitea.ecoplant.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.NavigationManager
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider

@ServiceProvider
class NavigationService : BaseService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("NavigationService", "Service created")
    }


    fun navigate(context: Context, route: String,bundle: Bundle?= null) {
        try {
            NavigationManager.getInstance().navigate(context, route, bundle)
            Log.d("NavigationService", "Navigating to $route")
        } catch (e: IllegalArgumentException) {
            Log.e("NavigationService", "Navigation failed: ${e.message}")
        }
    }

    fun navigateToDefault(context: Context) {
        try {
            NavigationManager.getInstance().navigateToDefault(context)
            Log.d("NavigationService", "Navigating to default route")
        } catch (e: IllegalStateException) {
            Log.e("NavigationService", "Navigation to default failed: ${e.message}")
        }
    }

}