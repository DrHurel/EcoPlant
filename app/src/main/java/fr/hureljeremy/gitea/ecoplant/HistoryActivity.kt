package fr.hureljeremy.gitea.ecoplant

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.widget.ImageButton
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Button
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.ui.theme.EcoPlantTheme
import fr.hureljeremy.gitea.ecoplant.utils.Pages


@Page(route = "history", isDefault = false)
class HistoryActivity: ComponentActivity() {

    @Inject
    private lateinit var navigationService: NavigationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_page)

        findViewById<Button>(R.id.home_button).setOnClickListener {
            navigationService.navigate(this, "home")
        }

        findViewById<Button>(R.id.map_button).setOnClickListener {
            navigationService.navigate(this, "map")
        }
    }

}
