package fr.hureljeremy.gitea.ecoplant

import android.os.Bundle
import android.widget.ImageButton
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService


@Page(route = "home", isDefault = true)
class MainActivity  : BaseActivity() {

    @Inject
    private lateinit var navigationService: NavigationService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_page)



        findViewById<ImageButton>(R.id.scanner_button).setOnClickListener {
            navigationService.navigate(this, "scanner")
        }

        findViewById<ImageButton>(R.id.history_button).setOnClickListener {
            navigationService.navigate(this, "history")
        }

        findViewById<ImageButton>(R.id.map_button).setOnClickListener {
            navigationService.navigate(this, "map")
        }

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            navigationService.navigate(this, "setting")
        }

        findViewById<ImageButton>(R.id.my_parcels_button).setOnClickListener {
            navigationService.navigate(this, "parcels")
        }

        findViewById<ImageButton>(R.id.search_button).setOnClickListener {
            navigationService.navigate(this, "find_parcel")
        }
    }
}