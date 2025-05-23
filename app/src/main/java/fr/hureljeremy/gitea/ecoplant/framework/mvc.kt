package fr.hureljeremy.gitea.ecoplant.framework

abstract class View {
    abstract fun render()
    abstract fun update(data: Any)
}

abstract class Model(val view: View) {
    abstract fun fetchData()
    abstract fun notify()
}

abstract  class Controller(val view: View, val model: Model) {

    abstract fun updateView()
    abstract fun handleUserInput(input: String)
}

