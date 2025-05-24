package fr.hureljeremy.gitea.ecoplant

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "parcels", isDefault = false)
class ParcelsActivity : BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ParcelsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parcels_page)

        recyclerView = findViewById(R.id.parcels_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val parcelItems = createSampleData()
        adapter = ParcelsAdapter(parcelItems) { item ->
            Toast.makeText(this, "Parcelle sélectionnée : ${item.title}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter


        findViewById<ImageButton>(R.id.home_button).setOnClickListener {
            navigationService.navigate(this, "home")
        }

    }

    private fun createSampleData(): List<ParcelItem> {
        return listOf(
            ParcelItem(1, "Parcelle A", listOf("Arrosage", "Taille", "Fertilisation"), 70, true),
            ParcelItem(2, "Parcelle B", listOf("Arrosage", "Taille"), 50, false),
            ParcelItem(3, "Parcelle C", listOf("Fertilisation"), 60, true),
            ParcelItem(4, "Parcelle D", listOf("Arrosage", "Désherbage", "Fertilisation"), 80, false)
        )
    }
}