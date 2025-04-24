package fr.hureljeremy.gitea.ecoplant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.widget.ImageButton
import android.content.Intent
import fr.hureljeremy.gitea.ecoplant.ui.theme.EcoPlantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.home_page)

        // Bouton de scan pour aller vers le scanner
        findViewById<ImageButton>(R.id.scanner_button).setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }

        // Bouton history
        findViewById<ImageButton>(R.id.history_button).setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Bouton map
        findViewById<ImageButton>(R.id.map_button).setOnClickListener {
            val intent = Intent(this, HistoryMapActivity::class.java)
            startActivity(intent)
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EcoPlantTheme {
        Greeting("Android")
    }
}