package fr.hureljeremy.gitea.ecoplant.framework

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import java.util.ServiceLoader

@Target(AnnotationTarget.CLASS)
annotation class Page(
    val route: String,
    val isDefault: Boolean = false
)

@Target(AnnotationTarget.CLASS)
annotation class NavigationProvider

class NavigationManager private constructor() {
    private val routes = mutableMapOf<String, Class<out Activity>>()
    private var defaultRoute: String? = null
    private var currentActivity: Activity? = null

    companion object {
        private var instance: NavigationManager? = null
        private var applicationContext: Context? = null

        fun getInstance(): NavigationManager {
            if (instance == null) {
                instance = NavigationManager()
            }
            return instance!!
        }

        fun initialize(context: Context, packageName: String) {
            applicationContext = context.applicationContext
            getInstance().scanForPages(packageName)
        }
    }


private fun scanForPages(packageName: String) {
    try {
        val context = applicationContext ?: return
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            android.content.pm.PackageManager.GET_ACTIVITIES
        )

        packageInfo.activities?.forEach { activityInfo ->
            try {
                val activityClass = Class.forName(activityInfo.name)
                val pageAnnotation = activityClass.getAnnotation(Page::class.java)
                if (pageAnnotation != null) {
                    registerPage(pageAnnotation.route, activityClass as Class<out Activity>)
                    if (pageAnnotation.isDefault) {
                        defaultRoute = pageAnnotation.route
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
    fun registerPage(route: String, activityClass: Class<out Activity>) {
        routes[route] = activityClass
    }

    fun navigate(context: Context, route: String) {
        val activityClass = routes[route] ?: run {
            throw IllegalArgumentException("No activity found for route: $route")
        }

        val intent = Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun navigateToDefault(context: Context) {
        defaultRoute?.let { route ->
            navigate(context, route)
        } ?: throw IllegalStateException("No default route set")
    }

    fun setCurrentActivity(activity: Activity) {
        currentActivity = activity
    }



    fun getCurrentActivity(): Activity? = currentActivity
}
abstract class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.getInstance().injectServices(this)
        NavigationManager.getInstance().setCurrentActivity(this)

    }
}

abstract class BaseFragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.getInstance().injectServices(this)
        NavigationManager.getInstance().setCurrentActivity(this)
    }
}