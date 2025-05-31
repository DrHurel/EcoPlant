package fr.hureljeremy.gitea.ecoplant.pages

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Input
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.OnEditorAction
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.framework.ParcelItem
import fr.hureljeremy.gitea.ecoplant.models.FindParcelAdapter
import fr.hureljeremy.gitea.ecoplant.models.FindParcelItem
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.services.ParcelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Page(route = "find_parcel", layout = "find_parcel_page", isDefault = false)
class FindParcelActivity : BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService

    @Inject
    private lateinit var parcelService: ParcelService

    private lateinit var recyclerView: RecyclerView

    @Input(id = "search_parcels")
    private var query: String = ""

    private lateinit var adapter: FindParcelAdapter
    private val parcelItems = mutableListOf<FindParcelItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parcelService.initialize(this)

        findViewById<View>(android.R.id.content).post {
            initViews()
            loadPublicParcels()
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
        adapter = FindParcelAdapter(parcelItems) { parcel ->
            Toast.makeText(this, "Parcelle sélectionnée: ${parcel.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
    }

    private fun loadPublicParcels() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val publicParcels = mutableListOf<FindParcelItem>()
                val iterator = parcelService.getParcels()

                while (iterator.hasNext()) {
                    val parcel = iterator.next()
                    if (parcel.isPublic) {
                        publicParcels.add(convertToFindParcelItem(parcel))
                    }
                }

                withContext(Dispatchers.Main) {
                    parcelItems.clear()
                    parcelItems.addAll(publicParcels)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FindParcelActivity,
                        "Erreur lors du chargement des parcelles: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun convertToFindParcelItem(parcel: ParcelItem): FindParcelItem {
        return FindParcelItem(
            id = parcel.id,
            name = parcel.title,
        )
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

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val allParcels = mutableListOf<ParcelItem>()
                val iterator = parcelService.getParcels()

                while (iterator.hasNext()) {
                    val parcel = iterator.next()
                    if (parcel.isPublic) {
                        allParcels.add(parcel)
                    }
                }

                val filteredParcels = if (query.isEmpty()) {
                    allParcels
                } else {
                    allParcels.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.latitude.contains(query, ignoreCase = true) ||
                                it.longitude.contains(query, ignoreCase = true)
                    }
                }

                val resultItems = filteredParcels.map { convertToFindParcelItem(it) }

                withContext(Dispatchers.Main) {
                    parcelItems.clear()
                    parcelItems.addAll(resultItems)
                    adapter.notifyDataSetChanged()

                    if (resultItems.isEmpty()) {
                        Toast.makeText(
                            this@FindParcelActivity,
                            "Aucune parcelle trouvée pour \"$query\"",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@FindParcelActivity,
                        "Erreur lors de la recherche: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}