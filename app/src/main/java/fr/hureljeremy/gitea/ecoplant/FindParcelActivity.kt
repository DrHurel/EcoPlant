package fr.hureljeremy.gitea.ecoplant

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "find_parcel", isDefault = false)
class FindParcelActivity :  BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FindParcelAdapter
    private lateinit var allParcelItems: List<FindParcelItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_parcel_page)

        // Configuration du bouton Home
        findViewById<ImageButton>(R.id.home_button).setOnClickListener {
            navigationService.navigate(this, "home")
        }

        // Configuration de la barre de recherche
        val searchEditText = findViewById<EditText>(R.id.search_parcels)
        val searchButton = findViewById<ImageButton>(R.id.search_button)

        searchButton.setOnClickListener {
            performSearch(searchEditText.text.toString())
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }

        recyclerView = findViewById(R.id.find_parcels_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        allParcelItems = createSampleData()
        adapter = FindParcelAdapter(allParcelItems) { parcel ->
            Toast.makeText(this, "Parcelle sélectionnée: ${parcel.name}", Toast.LENGTH_SHORT).show()
            // Ajouter ici la navigation vers les détails de la parcelle si nécessaire
        }

        recyclerView.adapter = adapter

    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allParcelItems)
            return
        }

        val filteredList = allParcelItems.filter {
            it.name.contains(query, ignoreCase = true)
        }
        adapter.updateData(filteredList)
    }

    private fun createSampleData(): List<FindParcelItem> {
        return listOf(
            FindParcelItem(1, "Parcelle Potager"),
            FindParcelItem(2, "Parcelle Verger"),
            FindParcelItem(3, "Parcelle Fleurs"),
            FindParcelItem(4, "Parcelle Herbes Aromatiques"),
            FindParcelItem(5, "Parcelle Jardin Zen"),
            FindParcelItem(6, "Parcelle Forêt"),
            FindParcelItem(7, "Parcelle Prairie"),
            FindParcelItem(8, "Parcelle Vigne"),
            FindParcelItem(9, "Parcelle Aquatique"),
            FindParcelItem(10, "Parcelle Montagne"),
            FindParcelItem(11, "Parcelle Désert"),
            FindParcelItem(12, "Parcelle Tropicale"),
            FindParcelItem(13, "Parcelle Arctique"),
            FindParcelItem(14, "Parcelle Urbain"),
            FindParcelItem(15, "Parcelle Historique"),
            FindParcelItem(16, "Parcelle Biodiversité"),
            FindParcelItem(17, "Parcelle Écologique"),
            FindParcelItem(18, "Parcelle Communautaire"),
            FindParcelItem(19, "Parcelle Éducative"),
            FindParcelItem(20, "Parcelle Récréative"),
            FindParcelItem(21, "Parcelle Artisanale"),
            FindParcelItem(22, "Parcelle Scientifique"),
            FindParcelItem(23, "Parcelle Artistique"),
            FindParcelItem(24, "Parcelle Sportive"),
            FindParcelItem(25, "Parcelle Technologique"),
            FindParcelItem(26, "Parcelle Gastronomique"),
            FindParcelItem(27, "Parcelle Aquaponique"),
            FindParcelItem(28, "Parcelle Hydroponique"),
            FindParcelItem(29, "Parcelle Permaculture"),
            FindParcelItem(30, "Parcelle Agroforesterie"),
        )
    }
}