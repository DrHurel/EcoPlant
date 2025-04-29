package fr.hureljeremy.gitea.ecoplant.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import fr.hureljeremy.gitea.ecoplant.utils.Pages


class NavigationService : Service() {

    private val binder = LocalBinder()
    private val destinations = mutableMapOf<Pages, Class<*>>()
    private val pageIntent = mutableMapOf<Pages, Intent>()
    private var currentDestination: Pages? = null

    inner class LocalBinder : Binder() {
        fun getService(): NavigationService = this@NavigationService
        fun updateCurrentDestination(page: Class<*>) {
            {
                for (entry in destinations) {
                    if (entry.value == page) {
                        currentDestination = entry.key
                        break
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): LocalBinder {

        return binder
    }


    fun navigate(context: Context, page: Pages, apply: Bundle? = null) {

        val destination = destinations[page]
        if (destination != null) {
            this.currentDestination = page
            val intent = pageIntent[page] ?: Intent(context, destination)
            if (pageIntent[page] == null) {
                pageIntent[page] = intent
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (apply != null) {
                intent.putExtras(apply)
            }
            context.startActivity(intent)
            Log.d("NavigationService", "Navigating to $page")
        } else {
            Log.e("NavigationService", "Destination $page not registered")
        }
    }

    fun registerDestination(page: Pages, ui: Class<*>) {
        destinations[page] = ui
        Log.d("NavigationService", "Registered destination: $page -> $ui")
    }

    fun unregisterDestination(page: Pages) {
        destinations.remove(page)
        Log.d("NavigationService", "Unregistered destination: $page")
    }

    fun getCurrentDestination(): Pages? {
        return currentDestination
    }

    fun clearDestinations() {
        destinations.clear()
        Log.d("NavigationService", "Cleared all destinations")
    }

    fun getDestinations(): List<Pages> {
        return destinations.keys.toList()
    }

}
