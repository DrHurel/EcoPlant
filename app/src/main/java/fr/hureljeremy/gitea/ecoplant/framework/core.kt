package fr.hureljeremy.gitea.ecoplant.framework

import android.app.Dialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        HideAndroidHUD.hideNavigationBar(this)

        val layoutId = NavigationManager.getInstance().getLayoutResourceId(this)
        setContentView(layoutId)

        ServiceLocator.getInstance().injectServices(this)
        NavigationManager.getInstance().setCurrentActivity(this)
        EventLocator.getInstance().bindEvents(this)
        InputBinder.bindInputs(this, findViewById(android.R.id.content))
    }
}

abstract class BaseFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.getInstance().injectServices(this)
        NavigationManager.getInstance().setCurrentActivity(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventLocator.getInstance().bindEvents(this, view)
        InputBinder.bindInputs(this, view)

    }
}

abstract class BaseDialog(context: Context) {
    protected val dialog: Dialog = Dialog(context)

    init {
        setupDialog()
        ServiceLocator.getInstance().injectServices(this)
    }

    private fun setupDialog() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        configureDialog()
        EventLocator.getInstance().bindEvents(this, dialog.findViewById(android.R.id.content))
    }

    protected abstract fun configureDialog()

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
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
                val instance =
                    ServiceLocator.getInstance().getService(serviceProvider as KClass<Any>)
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