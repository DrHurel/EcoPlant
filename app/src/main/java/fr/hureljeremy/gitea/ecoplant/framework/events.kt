package fr.hureljeremy.gitea.ecoplant.framework

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Listener(val type : String = "click")


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

    @RequiresApi(Build.VERSION_CODES.O)
    fun findListener(instance: BaseActivity) {
        val clazz = instance.javaClass
        val methods = clazz.declaredMethods
        for (method in methods) {
            if (method.isAnnotationPresent(Listener::class.java)) {
                val annotation = method.getAnnotation(Listener::class.java)
                
                //asset that the function has Parameter id with default value
                val parameters = method.parameters
                if (parameters.size != 1) {
                    throw IllegalArgumentException("Listener method must have exactly one parameter")
                }
                val parameter = parameters[0]
                if (parameter.type != Int::class.java) {
                    throw IllegalArgumentException("Listener method parameter must be of type Int")
                }
                // get the default value of the parameter
                val viewId = method.getAnnotation(Listener::class.java)?.type?.toIntOrNull()

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

