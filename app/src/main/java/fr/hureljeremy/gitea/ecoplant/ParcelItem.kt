package fr.hureljeremy.gitea.ecoplant

import fr.hureljeremy.gitea.ecoplant.framework.ServiceEntry

data class ParcelItem(
    val id: Long,
    val title: String,
    val services: List<ServiceEntry> = listOf(),
    val minimumReliabilityScore: Double = 50.0,
    val isPublic: Boolean = false
)