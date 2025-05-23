package fr.hureljeremy.gitea.ecoplant

import android.os.Bundle
import android.widget.Button
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService


@Page(route = "history_map", isDefault = false)
class HistoryMapActivity : BaseActivity() {

    @Inject
    private lateinit var navigationService: NavigationService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_map_page)

        findViewById<Button>(R.id.home_button).setOnClickListener {
            navigationService.navigate(this, "home")

        }

        findViewById<Button>(R.id.history_button).setOnClickListener {
            navigationService.navigate(this, "history")

        }
    }


}
