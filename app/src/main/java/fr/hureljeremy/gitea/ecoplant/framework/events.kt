package fr.hureljeremy.gitea.ecoplant.framework

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Listener(val viewId : Int,val type : String = "click")


class ListenerManager {

    companion object {
        private var instance: ListenerManager? = null

        fun getInstance(): ListenerManager {
            if (instance == null) {
                instance = ListenerManager()
            }
            return instance!!
        }
    }

    fun findListener(instance: BaseActivity) {
        val clazz = instance.javaClass
        val methods = clazz.declaredMethods
        for (method in methods) {
            if (method.isAnnotationPresent(Listener::class.java)) {
                val annotation = method.getAnnotation(Listener::class.java)
                // register the listener
                val viewId = annotation?.viewId
                val type = annotation?.type
                val view = viewId?.let { instance.findViewById<View>(it) }
                when (type) {
                    "click" -> {
                        view?.setOnClickListener {
                            method.invoke(this)
                        }
                    }
                    "longClick" -> {
                        view?.setOnLongClickListener {
                            method.invoke(this)
                            true
                        }
                    }
                    "touch" -> view?.setOnTouchListener { _, event ->
                        method.invoke(this, event)
                        true
                    }
                    "focus" -> {
                        view?.setOnFocusChangeListener { _, hasFocus ->
                            method.invoke(this, hasFocus)
                        }
                    }
                    "textChange" -> {
                        if (view is EditText) {
                            view.addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    method.invoke(this@ListenerManager, s)
                                }
                                override fun afterTextChanged(s: Editable?) {}
                            })
                        }
                    }
                    else -> {
                        throw IllegalArgumentException("Unknown listener type: $type")
                    }
                }
            }
        }

    }

}

