package fr.hureljeremy.gitea.ecoplant.models

data class HistoryItem(
    val id: Long,
    val name: String,
    val imageUrl: String? = null
)