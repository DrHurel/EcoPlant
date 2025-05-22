package fr.hureljeremy.gitea.ecoplant.framework

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Target(AnnotationTarget.CLASS)
annotation class ServiceProvider

@Target(AnnotationTarget.PROPERTY)
annotation class Inject

class ServiceLocator private constructor() {
    private val services = mutableMapOf<KClass<*>, Any>()
    private var context: Context? = null

    fun initialize(context: Context, vararg servicePackages: String) {
        this.context = context
        discoverServices(servicePackages)
    }

    private fun discoverServices(packages: Array<out String>) {
        packages.forEach { packageName ->
            val classLoader = Thread.currentThread().contextClassLoader
            try {
                val classes = getClassesInPackage(packageName, classLoader)
                classes.forEach { clazz ->
                    val kClass = clazz.kotlin
                    if (kClass.hasAnnotation<ServiceProvider>()) {
                        registerService(kClass)
                    }
                }
            } catch (e: Exception) {
                throw IllegalStateException(
                    "Failed to discover services in package $packageName",
                    e
                )
            }
        }
    }

    private fun registerService(serviceClass: KClass<*>) {
        if (services.containsKey(serviceClass)) return

        val instance = serviceClass.createInstance()
        services[serviceClass] = instance

        if (BaseService::class.java.isAssignableFrom(serviceClass.java)) {
            val serviceIntent = Intent(context, serviceClass.java)
            context?.bindService(serviceIntent, object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as? BaseService.LocalBinder
                    binder?.getService()?.let { services[serviceClass] = it }
                }
                override fun onServiceDisconnected(name: ComponentName?) {}
            }, Context.BIND_AUTO_CREATE)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getService(serviceClass: KClass<T>): T {
        return services[serviceClass] as? T
            ?: throw IllegalStateException("Service ${serviceClass.simpleName} not found")
    }

    private fun getClassesInPackage(packageName: String, classLoader: ClassLoader): List<Class<*>> {
        val classes = mutableListOf<Class<*>>()

        try {
            // Add known service classes manually
            classes.addAll(listOf(
                Class.forName("fr.hureljeremy.gitea.ecoplant.services.NavigationService"),
                Class.forName("fr.hureljeremy.gitea.ecoplant.services.PlantNetService"),
                Class.forName("fr.hureljeremy.gitea.ecoplant.services.CameraService") // Add this line
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return classes
    }

    companion object {
        private var instance: ServiceLocator? = null

        fun getInstance(): ServiceLocator {
            return instance ?: synchronized(this) {
                instance ?: ServiceLocator().also { instance = it }
            }
        }

        fun destroy() {
            instance?.services?.clear()
            instance?.context = null
            instance = null
        }
    }

    fun injectServices(target: Any) {
        val targetClass = target::class
        for (property in targetClass.memberProperties) {
            if (property.hasAnnotation<Inject>()) {
                property.isAccessible = true
                val serviceProvider = property.returnType.classifier as? KClass<*> ?: continue
                val instance = getService(serviceProvider)
                try {
                    (property as? KMutableProperty1<Any, Any?>)?.set(target, instance)
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "Failed to inject service for property ${property.name}",
                        e
                    )
                }
            }
        }
    }
}

abstract class BaseService : Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BaseService = this@BaseService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        injectDependencies()
    }

    private fun injectDependencies() {
        val serviceClass = this::class
        for (property in serviceClass.memberProperties) {
            if (property.hasAnnotation<Inject>()) {
                property.isAccessible = true
                val serviceProvider = property.returnType.classifier as? KClass<*> ?: continue
                val instance = ServiceLocator.getInstance().getService(serviceProvider)
                try {
                    (property as? KMutableProperty1<BaseService, Any?>)?.set(this, instance)
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "Failed to inject dependency for property ${property.name}",
                        e
                    )
                }
            }
        }
    }
}