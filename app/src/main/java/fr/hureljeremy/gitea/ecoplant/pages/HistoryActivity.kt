package fr.hureljeremy.gitea.ecoplant.pages

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.models.HistoryAdapter
import fr.hureljeremy.gitea.ecoplant.models.HistoryItem
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.services.ParcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Page(route = "history", layout = "history_page", isDefault = false)
class HistoryActivity : BaseActivity() {

    @Inject
    private lateinit var navigationService: NavigationService

    @Inject
    private lateinit var parcelService: ParcelService

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val historyItems = mutableListOf<HistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser le service avec le contexte
        parcelService.initialize(this)

        recyclerView = findViewById(R.id.history_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HistoryAdapter(historyItems) { item ->
            Toast.makeText(this, "Plante sélectionnée : ${item.name}", Toast.LENGTH_SHORT).show()
            // Logique pour afficher les détails si nécessaire
        }
        recyclerView.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val historyList = mutableListOf<HistoryItem>()
                val iterator = parcelService.getParcels()

                while (iterator.hasNext()) {
                    val parcel = iterator.next()
                    val parcelWithResults = parcelService.getParcelWithResults(parcel.id.toInt())

                    parcelWithResults?.services?.forEach { result ->
                        historyList.add(HistoryItem(parcel.id, result.species))
                    }
                }

                withContext(Dispatchers.Main) {
                    historyItems.clear()
                    historyItems.addAll(historyList)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@HistoryActivity,
                        "Erreur lors du chargement de l'historique: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    @OnClick("home_button")
    fun navigateToHome() {
        navigationService.navigate(this, "home")
    }

    @OnClick("map_button")
    fun navigateToMap() {
        navigationService.navigate(this, "map")
    }
}