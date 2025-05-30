package fr.hureljeremy.gitea.ecoplant.pages

import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "history_map", layout = "history_map_page", isDefault = false)
class HistoryMapActivity : BaseActivity() {

    @Inject
    private lateinit var navigationService: NavigationService

    @OnClick("home_button")
    fun navigateToHome() {
        navigationService.navigate(this, "home")
    }

    @OnClick("history_button")
    fun navigateToHistory() {
        navigationService.navigate(this, "history")
    }
}