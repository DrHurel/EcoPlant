package fr.hureljeremy.gitea.ecoplant.utils

interface Guard {
    fun isAuthorized(): Boolean
}