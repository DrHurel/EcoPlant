package fr.hureljeremy.gitea.ecoplant.framework

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


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