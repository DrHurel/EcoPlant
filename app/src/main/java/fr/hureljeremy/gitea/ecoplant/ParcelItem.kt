package fr.hureljeremy.gitea.ecoplant

data class ParcelItem(
    val id: Long,
    val title: String,
    val services: List<String> = listOf(),
    val minimumFiabilityScore: Int = 50,
    val isPublic: Boolean = false
)