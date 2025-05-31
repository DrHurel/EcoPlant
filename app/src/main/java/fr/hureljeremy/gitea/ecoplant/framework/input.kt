package fr.hureljeremy.gitea.ecoplant.framework

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import java.lang.reflect.Field
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Input(val id: String, val twoWay: Boolean = false)

class InputBinder {
    companion object {
        private const val TAG = "InputBinder"

        fun bindInputs(target: Any, rootView: View) {
            val targetClass = target::class

            for (property in targetClass.memberProperties) {
                val inputAnnotation = property.findAnnotation<Input>() ?: continue

                val viewId = rootView.context.resources.getIdentifier(
                    inputAnnotation.id, "id", rootView.context.packageName
                )

                if (viewId == 0) {
                    Log.e(TAG, "Vue non trouvée pour l'id ${inputAnnotation.id}")
                    continue
                }

                val view = rootView.findViewById<View>(viewId) ?: continue
                property.isAccessible = true

                when (view) {
                    is EditText -> bindEditText(view, property, target, inputAnnotation.twoWay)
                    is CheckBox -> bindCheckBox(view, property, target, inputAnnotation.twoWay)
                    is RadioButton -> bindRadioButton(view, property, target, inputAnnotation.twoWay)
                    is TextView -> bindTextView(view, property, target, inputAnnotation.twoWay)
                    else -> throw IllegalArgumentException(
                        "Type de vue non supporté: ${view::class.java.simpleName} pour la propriété ${property.name}"
                    )
                }
            }
        }

        private fun bindEditText(editText: EditText, property: KProperty<*>, target: Any, twoWay: Boolean) {
            if (twoWay) {
                val value = property.getter.call(target)
                editText.setText(value?.toString() ?: "")
            }

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    try {
                        val mutableProperty = property as? KMutableProperty<*>
                        if (mutableProperty != null) {
                            val value = convertToPropertyType(s.toString(), property)
                            mutableProperty.setter.call(target, value)
                            Log.d(TAG, "Propriété ${property.name} mise à jour avec $value")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erreur lors de la mise à jour de ${property.name}", e)
                    }
                }
            })
        }

        private fun bindCheckBox(checkBox: CheckBox, property: KProperty<*>, target: Any, twoWay: Boolean) {
            if (twoWay) {
                val value = property.getter.call(target)
                if (value is Boolean) checkBox.isChecked = value
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                try {
                    val mutableProperty = property as? KMutableProperty<*>
                    mutableProperty?.setter?.call(target, isChecked)
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la mise à jour de ${property.name}", e)
                }
            }
        }

        private fun bindRadioButton(radioButton: RadioButton, property: KProperty<*>, target: Any, twoWay: Boolean) {
            if (twoWay) {
                val value = property.getter.call(target)
                if (value is Boolean) radioButton.isChecked = value
            }

            radioButton.setOnCheckedChangeListener { _, isChecked ->
                try {
                    val mutableProperty = property as? KMutableProperty<*>
                    mutableProperty?.setter?.call(target, isChecked)
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la mise à jour de ${property.name}", e)
                }
            }
        }

        private fun bindTextView(textView: TextView, property: KProperty<*>, target: Any, twoWay: Boolean) {
            if (twoWay) {
                val value = property.getter.call(target)
                textView.text = value?.toString() ?: ""
            }
        }

        private fun convertToPropertyType(value: String, property: KProperty<*>): Any? {
            return when (property.returnType.classifier) {
                String::class -> value
                Int::class -> value.toIntOrNull() ?: 0
                Double::class -> value.toDoubleOrNull() ?: 0.0
                Float::class -> value.toFloatOrNull() ?: 0f
                Long::class -> value.toLongOrNull() ?: 0L
                Boolean::class -> value.toBoolean()
                else -> value
            }
        }
    }
}