package fr.hureljeremy.gitea.ecoplant.framework

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import java.lang.reflect.Method

enum class EventType {
    CLICK,
    LONG_CLICK,
    TEXT_CHANGED,
    EDITOR_ACTION
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class On(val type: EventType, val id: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnClick(val id: String = "")

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnEditorAction(val id: String = "", val actionId: Int = EditorInfo.IME_ACTION_DONE)

class EventLocator private constructor() {
    private val TAG = "EventLocator"

    companion object {
        private var instance: EventLocator? = null

        fun getInstance(): EventLocator {
            return instance ?: synchronized(this) {
                instance ?: EventLocator().also { instance = it }
            }
        }
    }

    fun bindEvents(target: Any, rootView: View? = null) {
        Log.d(TAG, "Binding events for ${target.javaClass.simpleName}")
        val methods = target.javaClass.declaredMethods
        for (method in methods) {
            method.isAccessible = true
            processAnnotations(method, target, rootView)
        }
    }

    private fun processAnnotations(method: Method, target: Any, rootView: View?) {
        method.getAnnotation(On::class.java)?.let { annotation ->
            Log.d(
                TAG,
                "Processing @On annotation: ${annotation.type} for ${method.name} with id=${annotation.id}"
            )
            val view = findView(target, annotation.id, rootView)
            if (view == null) {
                Log.w(
                    TAG,
                    "View not found for id=${annotation.id} in ${target.javaClass.simpleName}"
                )
                return
            }

            when (annotation.type) {
                EventType.CLICK -> bindClickEvent(method, target, view)
                EventType.LONG_CLICK -> bindLongClickEvent(method, target, view)
                EventType.TEXT_CHANGED -> bindTextChangedEvent(method, target, view)
                EventType.EDITOR_ACTION -> bindEditorActionEvent(method, target, view, EditorInfo.IME_ACTION_DONE)
            }
        }

        method.getAnnotation(OnClick::class.java)?.let { annotation ->
            val viewId = if (annotation.id.isEmpty()) method.name else annotation.id
            Log.d(TAG, "Processing @OnClick annotation for ${method.name} with id=$viewId")

            val view = findView(target, viewId, rootView)
            if (view == null) {
                Log.w(TAG, "View not found for id=$viewId in ${target.javaClass.simpleName}")
                return
            }

            bindClickEvent(method, target, view)
        }

        method.getAnnotation(OnEditorAction::class.java)?.let { annotation ->
            val viewId = if (annotation.id.isEmpty()) method.name else annotation.id
            Log.d(TAG, "Processing @OnEditorAction annotation for ${method.name} with id=$viewId")

            val view = findView(target, viewId, rootView)
            if (view == null) {
                Log.w(TAG, "View not found for id=$viewId in ${target.javaClass.simpleName}")
                return@let
            }

            bindEditorActionEvent(method, target, view, annotation.actionId)
        }
    }

    private fun bindEditorActionEvent(method: Method, target: Any, view: View, actionId: Int) {
        if (view !is EditText) {
            Log.w(TAG, "Cannot bind editor action event to non-EditText view: ${view.javaClass.simpleName}")
            return
        }

        Log.d(TAG, "Binding editor action event for ${method.name} on view id=${view.id}")
        view.setOnEditorActionListener { _, editorActionId, _ ->
            if (editorActionId == actionId) {
                try {
                    Log.d(TAG, "Editor action event triggered for ${method.name}")
                    invokeMethod(method, target, view)
                    Log.d(TAG, "Editor action event handled for ${method.name}")
                    return@setOnEditorActionListener true
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling editor action event for ${method.name}", e)
                }
            }
            false
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
        if (id == 0) {
            Log.w(TAG, "Resource ID not found for viewId=$viewId")
            return null
        }

        val view = when {
            rootView != null -> rootView.findViewById<View>(id)
            target is Activity -> target.findViewById<View>(id)
            target is Fragment -> target.view?.findViewById<View>(id)
            else -> NavigationManager.getInstance().getCurrentActivity()?.findViewById<View>(id)
        }

        if (view == null) {
            Log.w(TAG, "View not found for viewId=$viewId (resource id=$id)")
        }

        return view
    }

    private fun bindClickEvent(method: Method, target: Any, view: View) {
        Log.d(TAG, "Binding click event for ${method.name} on view id=${view.id}")
        view.setOnClickListener {
            try {
                Log.d(TAG, "Click event triggered for ${method.name}")
                invokeMethod(method, target, view)
                Log.d(TAG, "Click event handled for ${method.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error handling click event for ${method.name}", e)
                e.printStackTrace()
            }
        }
    }

    private fun bindLongClickEvent(method: Method, target: Any, view: View) {
        Log.d(TAG, "Binding long click event for ${method.name} on view id=${view.id}")
        view.setOnLongClickListener {
            try {
                Log.d(TAG, "Long click event triggered for ${method.name}")
                invokeMethod(method, target, view)
                Log.d(TAG, "Long click event handled for ${method.name}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error handling long click event for ${method.name}", e)
                e.printStackTrace()
                false
            }
        }
    }

    private fun bindTextChangedEvent(method: Method, target: Any, view: View) {
        if (view !is EditText) {
            Log.w(
                TAG,
                "Text changed event can only be bound to EditText, but got ${view.javaClass.simpleName}"
            )
            return
        }

        Log.d(TAG, "Binding text changed event for ${method.name} on view id=${view.id}")
        view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                try {
                    Log.d(TAG, "Text changed event triggered for ${method.name}")
                    when {
                        method.parameterTypes.isEmpty() -> method.invoke(target)
                        method.parameterTypes.size == 1 && method.parameterTypes[0] == Editable::class.java ->
                            method.invoke(target, s)

                        method.parameterTypes.size == 1 && method.parameterTypes[0] == String::class.java ->
                            method.invoke(target, s.toString())
                    }
                    Log.d(TAG, "Text changed event handled for ${method.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling text changed event for ${method.name}", e)
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