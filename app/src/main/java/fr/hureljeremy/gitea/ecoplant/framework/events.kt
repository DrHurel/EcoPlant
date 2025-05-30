package fr.hureljeremy.gitea.ecoplant.framework

    import android.app.Activity
    import android.text.Editable
    import android.text.TextWatcher
    import android.view.View
    import android.widget.EditText
    import androidx.fragment.app.Fragment
    import java.lang.reflect.Method

    enum class EventType {
        CLICK,
        LONG_CLICK,
        TEXT_CHANGED,
    }

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class On(val type: EventType, val id: String)

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class OnClick(val id: String = "")

    class EventLocator private constructor() {
        companion object {
            private var instance: EventLocator? = null

            fun getInstance(): EventLocator {
                return instance ?: synchronized(this) {
                    instance ?: EventLocator().also { instance = it }
                }
            }
        }

        fun bindEvents(target: Any, rootView: View? = null) {
            val methods = target.javaClass.declaredMethods
            for (method in methods) {
                method.isAccessible = true
                processAnnotations(method, target, rootView)
            }
        }

        private fun processAnnotations(method: Method, target: Any, rootView: View?) {
            method.getAnnotation(On::class.java)?.let { annotation ->
                val view = findView(target, annotation.id, rootView) ?: return
                when (annotation.type) {
                    EventType.CLICK -> bindClickEvent(method, target, view)
                    EventType.LONG_CLICK -> bindLongClickEvent(method, target, view)
                    EventType.TEXT_CHANGED -> bindTextChangedEvent(method, target, view)
                }
            }

            method.getAnnotation(OnClick::class.java)?.let { annotation ->
                val viewId = if (annotation.id.isEmpty()) method.name else annotation.id
                val view = findView(target, viewId, rootView) ?: return
                bindClickEvent(method, target, view)
            }
        }

        private fun findView(target: Any, viewId: String, rootView: View?): View? {
            val context = when (target) {
                is Activity -> target
                is Fragment -> target.context
                else -> rootView?.context ?: NavigationManager.getInstance()
                    .getCurrentActivity()?.baseContext
            } ?: return null

            val packageName = context.packageName
            val id = context.resources.getIdentifier(viewId, "id", packageName)
            if (id == 0) return null

            return when {
                rootView != null -> rootView.findViewById(id)
                target is Activity -> target.findViewById(id)
                target is Fragment -> target.view?.findViewById(id)
                else -> NavigationManager.getInstance().getCurrentActivity()?.findViewById(id)
            }
        }

        private fun bindClickEvent(method: Method, target: Any, view: View) {
            view.setOnClickListener {
                try {
                    invokeMethod(method, target, view)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private fun bindLongClickEvent(method: Method, target: Any, view: View) {
            view.setOnLongClickListener {
                try {
                    invokeMethod(method, target, view)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }

        private fun bindTextChangedEvent(method: Method, target: Any, view: View) {
            if (view !is EditText) return

            view.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    try {
                        when {
                            method.parameterTypes.isEmpty() -> method.invoke(target)
                            method.parameterTypes.size == 1 && method.parameterTypes[0] == Editable::class.java ->
                                method.invoke(target, s)
                            method.parameterTypes.size == 1 && method.parameterTypes[0] == String::class.java ->
                                method.invoke(target, s.toString())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }

        private fun invokeMethod(method: Method, target: Any, view: View) {
            when {
                method.parameterTypes.isEmpty() -> method.invoke(target)
                method.parameterTypes.size == 1 && View::class.java.isAssignableFrom(method.parameterTypes[0]) ->
                    method.invoke(target, view)
            }
        }
    }

    // Extensions pour simplifier l'utilisation
    fun Activity.bindEvents() {
        EventLocator.getInstance().bindEvents(this)
    }

    fun Fragment.bindEvents(view: View) {
        EventLocator.getInstance().bindEvents(this, view)
    }