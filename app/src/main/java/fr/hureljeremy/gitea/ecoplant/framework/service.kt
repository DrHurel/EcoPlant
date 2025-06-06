package fr.hureljeremy.gitea.ecoplant.framework

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import dalvik.system.PathClassLoader
import java.util.Enumeration
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ServiceProvider

@Retention(AnnotationRetention.RUNTIME)
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
            try {
                val classLoader = Thread.currentThread().contextClassLoader
                val classes = classLoader?.let { getClassesInPackage(packageName, it) }
                if (classes != null) {
                    for (clazz in classes) {
                        try {
                            val annotation = clazz.getAnnotation(ServiceProvider::class.java)
                            if (annotation != null) {
                                registerService(clazz.kotlin)
                            }
                        } catch (e: Exception) {
                            continue
                        }
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
        val classes = ArrayList<Class<*>>()
        try {
            val context = context ?: throw IllegalStateException("Context not initialized")
            val pm = context.packageManager
            val packageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
            val sourceDir = packageInfo.applicationInfo?.sourceDir
                ?: throw IllegalStateException("Source directory not found")

            val pathClassLoader = context.classLoader as PathClassLoader
            val dexPathList = pathClassLoader.javaClass.superclass
                ?.getDeclaredField("pathList")?.apply { isAccessible = true }
                ?.get(pathClassLoader)

            val dexElements = dexPathList?.javaClass
                ?.getDeclaredField("dexElements")?.apply { isAccessible = true }
                ?.get(dexPathList) as Array<*>

            for (element in dexElements) {
                val dexFile = element?.javaClass
                    ?.getDeclaredField("dexFile")
                    ?.apply { isAccessible = true }
                    ?.get(element)

                if (dexFile != null) {
                    val entries = dexFile.javaClass
                        .getDeclaredMethod("entries")
                        .invoke(dexFile) as Enumeration<String>

                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.startsWith(packageName)) {
                            try {
                                val entryClass = Class.forName(entry, false, classLoader)
                                if (!entryClass.isInterface &&
                                    !entryClass.isEnum &&
                                    !entryClass.isSynthetic &&
                                    !entryClass.name.contains("$") &&
                                    !entryClass.name.endsWith("Kt")
                                ) {
                                    classes.add(entryClass)
                                }
                            } catch (e: ClassNotFoundException) {
                                continue
                            }
                        }
                    }
                }
            }
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
                val instance = getService(serviceProvider as KClass<Any>)
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

