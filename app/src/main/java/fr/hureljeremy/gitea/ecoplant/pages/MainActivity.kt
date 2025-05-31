package fr.hureljeremy.gitea.ecoplant.pages

import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "home", layout = "home_page", isDefault = true)
class MainActivity : BaseActivity() {

    @Inject
    private lateinit var navigationService: NavigationService

    @OnClick("scanner_button")
    fun navigateToScanner() {
        navigationService.navigate(this, "scanner")
    }

    @OnClick("history_button")
    fun navigateToHistory() {
        navigationService.navigate(this, "history")
    }

    @OnClick("map_button")
    fun navigateToMap() {
        navigationService.navigate(this, "history_map")
    }

    @OnClick("settings_button")
    fun navigateToSettings() {
        navigationService.navigate(this, "setting")
    }

    @OnClick("my_parcels_button")
    fun navigateToParcels() {
        navigationService.navigate(this, "parcels")
    }

    @OnClick("search_button")
    fun navigateToFindParcel() {
        navigationService.navigate(this, "find_parcel")
    }
}