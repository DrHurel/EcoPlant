package fr.hureljeremy.gitea.ecoplant.framework

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible

// Annotation pour marquer les méthodes d'observation
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Observer(val key: String)

// Annotation pour spécifier le ViewModel
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ViewModel(val viewModelClass: KClass<out MVVMViewModel>)

// MVVMViewModel.kt - Base ViewModel
abstract class MVVMViewModel {
    private val observers = mutableMapOf<String, MutableList<(Any) -> Unit>>()

    protected fun notifyObservers(key: String, value: Any) {
        observers[key]?.forEach { it(value) }
    }

    fun observe(key: String, observer: (Any) -> Unit) {
        if (!observers.containsKey(key)) {
            observers[key] = mutableListOf()
        }
        observers[key]?.add(observer)
    }

    fun removeObserver(key: String, observer: (Any) -> Unit) {
        observers[key]?.remove(observer)
    }

    open fun onCleared() {
        observers.clear()
    }
}

// MVVMActivity.kt - Base Activity for MVVM
abstract class MVVMActivity<VM : MVVMViewModel> : BaseActivity() {
    protected abstract val viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
        setupObservers()
    }

    open fun initializeViewModel() {
        // Implémentation par défaut qui utilise l'annotation @ViewModel si présente
        val viewModelAnnotation = this::class.findAnnotation<ViewModel>()
        if (viewModelAnnotation != null) {
            @Suppress("UNCHECKED_CAST")
            (ViewModelFactory.getViewModel(viewModelAnnotation.viewModelClass.java) as? VM)?.let {
                // Injection via réflexion dans la propriété abstraite
                val field = this::class.java.getDeclaredField("viewModel")
                field.isAccessible = true
                field.set(this, it)
            }
        }
    }

    open fun setupObservers() {
        // Découverte automatique des méthodes annotées avec @Observer
        this::class.functions.filter { it.hasAnnotation<Observer>() }.forEach { method ->
            method.isAccessible = true
            val annotation = method.findAnnotation<Observer>() ?: return@forEach
            viewModel.observe(annotation.key) { value ->
                method.call(this, value)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared()
    }
}

// MVVMFragment.kt - Base Fragment for MVVM
abstract class MVVMFragment<VM : MVVMViewModel> : Fragment() {
    protected abstract val viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
        setupObservers()
    }

    open fun initializeViewModel() {
        // Implémentation par défaut qui utilise l'annotation @ViewModel si présente
        val viewModelAnnotation = this::class.findAnnotation<ViewModel>()
        if (viewModelAnnotation != null) {
            @Suppress("UNCHECKED_CAST")
            (ViewModelFactory.getViewModel(viewModelAnnotation.viewModelClass.java) as? VM)?.let {
                // Injection via réflexion dans la propriété abstraite
                val field = this::class.java.getDeclaredField("viewModel")
                field.isAccessible = true
                field.set(this, it)
            }
        }
    }

    open fun setupObservers() {
        // Découverte automatique des méthodes annotées avec @Observer
        this::class.functions.filter { it.hasAnnotation<Observer>() }.forEach { method ->
            method.isAccessible = true
            val annotation = method.findAnnotation<Observer>() ?: return@forEach
            viewModel.observe(annotation.key) { value ->
                method.call(this, value)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onCleared()
    }
}

// ViewModelFactory.kt - Factory for creating ViewModels
class ViewModelFactory {
    companion object {
        private val viewModels = mutableMapOf<Class<out MVVMViewModel>, MVVMViewModel>()

        @Suppress("UNCHECKED_CAST")
        fun <T : MVVMViewModel> getViewModel(modelClass: Class<T>): T {
            return viewModels.getOrPut(modelClass) {
                modelClass.getDeclaredConstructor().newInstance()
            } as T
        }

        fun clearViewModel(modelClass: Class<out MVVMViewModel>) {
            viewModels.remove(modelClass)
        }
    }
}