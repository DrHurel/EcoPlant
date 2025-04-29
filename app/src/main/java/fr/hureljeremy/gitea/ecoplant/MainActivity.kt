package fr.hureljeremy.gitea.ecoplant

import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.ui.theme.EcoPlantTheme
import fr.hureljeremy.gitea.ecoplant.utils.Pages

class MainActivity : ComponentActivity() {

    lateinit var navigationService: NavigationService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_page)


        // Bouton de scan pour aller vers le scanner
        findViewById<ImageButton>(R.id.scanner_button).setOnClickListener {
            navigationService.navigate(this, Pages.SCANNER)
        }

        // Bouton history
        findViewById<ImageButton>(R.id.history_button).setOnClickListener {
            navigationService.navigate(this, Pages.HISTORY)
        }

        // Bouton map
        findViewById<ImageButton>(R.id.map_button).setOnClickListener {
            navigationService.navigate(this, Pages.MAP)
        }
    }

    var bound = false

    data class Destination(val name: Pages, val activity: Class<out Activity>)

    val pages = listOf(
        Destination(Pages.HOME, MainActivity::class.java),
        Destination(Pages.HISTORY, HistoryActivity::class.java),
        Destination(Pages.MAP, HistoryMapActivity::class.java),
        Destination(Pages.SCANNER, ScannerActivity::class.java)
    )

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()
            if (!bound) {
                val registered = navigationService.getDestinations()
                for (page in pages) {
                    if (registered.contains(page.name)) {
                        continue
                    }
                    navigationService.registerDestination(page.name, page.activity)
                }
            }
            bound = true
            binder.updateCurrentDestination(this::class.java)

        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, NavigationService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)

        }


    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }
}