package fr.hureljeremy.gitea.ecoplant.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }


    fun login(username : String, password : String)  {
        // TODO("Not yet implemented")
        // Call the API to login
        // If success, save the token in SharedPreferences
        // If failure, show an error message
    }

    fun logout() {
        // TODO("Not yet implemented")
        // Call the API to logout
        // Remove the token from SharedPreferences
    }

    fun register(username : String, password : String) {
        // TODO("Not yet implemented")
        // Call the API to register
        // If success, save the token in SharedPreferences
        // If failure, show an error message
    }

    fun isLoggedIn() : Boolean {
        // TODO("Not yet implemented")
        // Check if the token is present in SharedPreferences
        return false
    }

    fun getToken() : String {
        // TODO("Not yet implemented")
        // Get the token from SharedPreferences
        return ""
    }


}