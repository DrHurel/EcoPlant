package fr.hureljeremy.gitea.ecoplant.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import fr.hureljeremy.gitea.ecoplant.utils.Pages
import fr.hureljeremy.gitea.ecoplant.utils.Route

class NavigationService : Service() {

    private val routes: Map<Pages, Route> = emptyMap()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }


    fun navigate(destination: Pages): Boolean {
        return routes[destination]?.load() ?: false
    }


}