package fr.hureljeremy.gitea.ecoplant.framework

import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.CLASS)
annotation class Model

@Target(AnnotationTarget.CLASS)
annotation class View(
    val model: KClass<*>
)

@Target(AnnotationTarget.CLASS)
annotation class Controller(
    val model: KClass<*>,
    val view: KClass<*>
)

abstract class BaseModel {
    private val observers = mutableListOf<ModelObserver>()

    fun addObserver(observer: ModelObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: ModelObserver) {
        observers.remove(observer)
    }

    protected fun notifyObservers() {
        observers.forEach { it.onModelChanged(this) }
    }
}

interface ModelObserver {
    fun onModelChanged(model: BaseModel)
}


abstract class BaseController {
    protected lateinit var model: BaseModel
    protected lateinit var view: BaseView

    fun bind(model: BaseModel, view: BaseView) {
        this.model = model
        this.view = view
        model.addObserver(view)
        initialize()
    }

    abstract fun initialize()
}

abstract class BaseView : ModelObserver {
    init {

    }

    abstract fun updateView()
}

class MVCFramework {
    private val models = mutableMapOf<KClass<*>, BaseModel>()
    private val views = mutableMapOf<KClass<*>, BaseView>()
    private val controllers = mutableMapOf<KClass<*>, BaseController>()

    fun initialize(packageName: String) {
        val reflections = Class.forName(packageName)
            .kotlin
            .nestedClasses

        reflections
            .filter { it.findAnnotation<Model>() != null }
            .forEach { registerModel(it) }

        reflections
            .filter { it.findAnnotation<View>() != null }
            .forEach { registerView(it) }

        reflections
            .filter { it.findAnnotation<Controller>() != null }
            .forEach { registerController(it) }

        bindComponents()
    }

    private fun registerModel(modelClass: KClass<*>) {
        models[modelClass] = modelClass.createInstance() as BaseModel
    }

    private fun registerView(viewClass: KClass<*>) {
        val viewAnnotation = viewClass.findAnnotation<View>()
        views[viewClass] = viewClass.createInstance() as BaseView
    }

    private fun registerController(controllerClass: KClass<*>) {
        val controllerAnnotation = controllerClass.findAnnotation<Controller>()
        controllers[controllerClass] = controllerClass.createInstance() as BaseController
    }

    private fun bindComponents() {
        controllers.forEach { (controllerClass, controller) ->
            val annotation = controllerClass.findAnnotation<Controller>()!!
            val model = models[annotation.model]!!
            val view = views[annotation.view]!!
            controller.bind(model, view)
        }
    }
}