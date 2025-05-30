package fr.hureljeremy.gitea.ecoplant.pages

        import android.os.Bundle
        import android.widget.Toast
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

        @Page(route = "history", layout = "history_page", isDefault = false)
        class HistoryActivity : BaseActivity() {

            @Inject
            private lateinit var navigationService: NavigationService

            private lateinit var recyclerView: RecyclerView
            private lateinit var adapter: HistoryAdapter

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                recyclerView = findViewById(R.id.history_recycler_view)
                recyclerView.layoutManager = LinearLayoutManager(this)

                val historyItems = createSampleData()
                adapter = HistoryAdapter(historyItems) { item ->
                    Toast.makeText(this, "Plante sélectionnée : ${item.name}", Toast.LENGTH_SHORT).show()
                }
                recyclerView.adapter = adapter
            }

            @OnClick("home_button")
            fun navigateToHome() {
                navigationService.navigate(this, "home")
            }

            @OnClick("map_button")
            fun navigateToMap() {
                navigationService.navigate(this, "map")
            }

            private fun createSampleData(): List<HistoryItem> {
                return listOf(
                    HistoryItem(1, "Rose"),
                    HistoryItem(2, "Tulipe"),
                    HistoryItem(3, "Orchidée"),
                    HistoryItem(4, "Cactus"),
                    HistoryItem(5, "Tournesol"),
                    HistoryItem(6, "Lys"),
                    HistoryItem(7, "Pavot"),
                    HistoryItem(8, "Marguerite"),
                    HistoryItem(9, "Violette"),
                )
            }
        }