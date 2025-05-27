package fr.hureljeremy.gitea.ecoplant.framework

import android.os.Bundle

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

    abstract fun initializeViewModel()
    abstract fun setupObservers()

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