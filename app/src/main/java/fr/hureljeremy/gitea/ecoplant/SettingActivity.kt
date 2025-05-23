package fr.hureljeremy.gitea.ecoplant

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton

@Page(route = "setting", isDefault = false)
class SettingActivity : BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_page)

        findViewById<Button>(R.id.go_back_button).setOnClickListener {
            navigationService.navigate(this, "home")
        }

    }

}