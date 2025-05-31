package fr.hureljeremy.gitea.ecoplant.framework

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowInsetsCompat

@Target(AnnotationTarget.CLASS)
annotation class Page(
    val route: String,
    val layout: String,
    val isDefault: Boolean = false
)

class NavigationManager private constructor() {
    private data class PageInfo(
        val activityClass: Class<out Activity>,
        val layoutResName: String
    )

    private val routes = mutableMapOf<String, PageInfo>()
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
                        registerPage(
                            pageAnnotation.route,
                            activityClass as Class<out Activity>,
                            pageAnnotation.layout
                        )
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

    fun registerPage(route: String, activityClass: Class<out Activity>, layoutName: String) {
        routes[route] = PageInfo(activityClass, layoutName)
    }

    fun navigate(context: Context, route: String, bundle: Bundle? = null) {
        val pageInfo = routes[route] ?: run {
            throw IllegalArgumentException("No activity found for route: $route")
        }

        val intent = Intent(context, pageInfo.activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            bundle?.let { putExtras(it) }
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

    fun getLayoutResourceId(activity: Activity): Int {
        val route = routes.entries.find { it.value.activityClass == activity.javaClass }?.key
            ?: throw IllegalStateException("Activity not registered: ${activity.javaClass.name}")

        val layoutName = routes[route]?.layoutResName
            ?: throw IllegalStateException("No layout found for route: $route")

        val packageName = activity.packageName
        val layoutId = activity.resources.getIdentifier(layoutName, "layout", packageName)

        if (layoutId == 0) {
            throw IllegalStateException("Layout resource not found: $layoutName")
        }

        return layoutId
    }
}

abstract class HideAndroidHUD {
    companion object {
        fun hideSystemBars(activity: Activity) {
            hideNavigationBar(activity)
            hideNotificationBar(activity)
        }

        fun hideNavigationBar(activity: Activity) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                activity.window.insetsController?.let {
                    it.hide(WindowInsetsCompat.Type.navigationBars())
                    it.systemBarsBehavior =
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility = (
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        )
            }
        }

        fun hideNotificationBar(activity: Activity) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                activity.window.insetsController?.let {
                    it.hide(WindowInsetsCompat.Type.statusBars())
                    it.systemBarsBehavior =
                        android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility = (
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                        )
            }
        }
    }
}

