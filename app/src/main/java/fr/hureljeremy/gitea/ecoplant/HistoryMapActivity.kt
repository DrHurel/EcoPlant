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
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.ui.theme.EcoPlantTheme
import fr.hureljeremy.gitea.ecoplant.utils.Pages


class HistoryMapActivity: ComponentActivity() {

    private lateinit var navigationService: NavigationService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.history_map_page)

        findViewById<Button>(R.id.home_button).setOnClickListener {
            navigationService.navigate(this, Pages.HOME)

        }

        findViewById<Button>(R.id.history_button).setOnClickListener {
            navigationService.navigate(this, Pages.HISTORY)

        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()

            binder.updateCurrentDestination(this::class.java)

        }

        override fun onServiceDisconnected(name: ComponentName) {

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
        unbindService(connection)
    }

}
