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
    import fr.hureljeremy.gitea.ecoplant.models.ParcelItem
    import fr.hureljeremy.gitea.ecoplant.models.ParcelsAdapter
    import fr.hureljeremy.gitea.ecoplant.services.NavigationService

    @Page(route = "parcels", layout = "parcels_page", isDefault = false)
    class ParcelsActivity : BaseActivity() {
        @Inject
        private lateinit var navigationService: NavigationService

        private lateinit var recyclerView: RecyclerView
        private lateinit var adapter: ParcelsAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            recyclerView = findViewById(R.id.parcels_recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this)

            val parcelItems = createSampleData()
            adapter = ParcelsAdapter(parcelItems) { item ->
                Toast.makeText(this, "Parcelle sélectionnée : ${item.title}", Toast.LENGTH_SHORT).show()
            }
            recyclerView.adapter = adapter
        }

        @OnClick("home_button")
        fun navigateToHome() {
            navigationService.navigate(this, "home")
        }

        private fun createSampleData(): List<ParcelItem> {
            return listOf(
                ParcelItem(1, "Parcelle A", listOf(), 70.0, true),
                ParcelItem(2, "Parcelle B", listOf(), 50.0, false),
                ParcelItem(3, "Parcelle C", listOf(), 60.0, true),
                ParcelItem(4, "Parcelle D", listOf(), 80.0, false)
            )
        }
    }