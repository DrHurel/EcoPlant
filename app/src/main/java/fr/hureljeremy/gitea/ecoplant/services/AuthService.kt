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

                    val user = auth.currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            token = tokenTask.result?.token

                        }
                    }
                }
            }

        return Result.success(Unit)
    }

    fun logout() {
        auth.signOut()
        token = null
    }

    fun register(username: String, password: String) {
        auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            token = tokenTask.result?.token
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