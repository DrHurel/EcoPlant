package fr.hureljeremy.gitea.ecoplant

data class HistoryItem(
    val id: Long,
    val name: String,
    val imageUrl: String? = null
)