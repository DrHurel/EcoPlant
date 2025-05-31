package fr.hureljeremy.gitea.ecoplant.models

import android.net.Uri

data class HistoryItem(
    val id: Long,
    val name: String,
    val imageUrl: Uri? = null
)