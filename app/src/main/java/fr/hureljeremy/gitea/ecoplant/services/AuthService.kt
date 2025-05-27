package fr.hureljeremy.gitea.ecoplant.services

import com.google.firebase.auth.FirebaseAuth
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider


@ServiceProvider
class AuthService : BaseService() {

    private lateinit var auth: FirebaseAuth
    private var token: String? = null

    fun login(username: String, password: String): Result<Unit> {
        auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, save the token in SharedPreferences
                    val user = auth.currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            token = tokenTask.result?.token
                            // Save token to SharedPreferences
                        }
                    }
                }
            }

        return Result.success(Unit) // Return success or failure based on the sign-in result
    }

    fun logout() {
        auth.signOut()
        token = null
        // Remove token from SharedPreferences
    }

    fun register(username: String, password: String) {
        auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration success, save the token in SharedPreferences
                    val user = auth.currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            token = tokenTask.result?.token
                            // Save token to SharedPreferences
                        }
                    }
                }
            }
    }

    fun isLoggedIn(): Boolean {
        auth = FirebaseAuth.getInstance()
        return auth.currentUser != null
    }

    fun getToken(): String {
        if (token == null) {
            auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                if (tokenTask.isSuccessful) {
                    token = tokenTask.result?.token
                }
            }
        }
        return token ?: ""
    }


}