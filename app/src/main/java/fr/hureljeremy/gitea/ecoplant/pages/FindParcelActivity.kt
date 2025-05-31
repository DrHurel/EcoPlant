package fr.hureljeremy.gitea.ecoplant.pages

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Input
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.OnEditorAction
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.models.FindParcelAdapter
import fr.hureljeremy.gitea.ecoplant.models.FindParcelItem
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "find_parcel", layout = "find_parcel_page", isDefault = false)
class FindParcelActivity : BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService


    private lateinit var recyclerView: RecyclerView

    @Input(id = "search_parcels")
    private var query: String = ""

    private lateinit var adapter: FindParcelAdapter
    private val allParcelItems: List<FindParcelItem> by lazy { createSampleData() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<View>(android.R.id.content).post {
            initViews()
        }
    }

    private fun initViews() {
        val rv = findViewById<RecyclerView>(R.id.find_parcels_recycler_view)
            ?: throw IllegalStateException("RecyclerView non trouvé")
        recyclerView = rv


        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FindParcelAdapter(allParcelItems) { parcel ->
            Toast.makeText(this, "Parcelle sélectionnée: ${parcel.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }

    @OnEditorAction(id = "search_parcels", actionId = EditorInfo.IME_ACTION_SEARCH)
    fun onSearchAction() {
        performSearch(query)
    }

    @OnClick("home_button")
    fun navigateToHome() {
        navigationService.navigate(this, "home")
    }

    @OnClick("search_button")
    fun onSearchButtonClick() {
        performSearch(query)
    }

    private fun performSearch(query: String) {
        Log.d("FindParcelActivity", "Performing search with query: $query")
        val filteredList = if (query.isEmpty()) {
            allParcelItems
        } else {
            allParcelItems.filter {
                it.name.contains(query, ignoreCase = true)
            }
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
            FindParcelItem(30, "Parcelle Agroforesterie")
        )
    }
}