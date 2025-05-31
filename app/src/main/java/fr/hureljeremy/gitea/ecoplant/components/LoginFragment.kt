package fr.hureljeremy.gitea.ecoplant.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseFragment
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Input
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.OnEditorAction
import fr.hureljeremy.gitea.ecoplant.services.AuthService

class LoginFragment : BaseFragment() {

    @Inject
    private lateinit var authService: AuthService

    private var loginSuccessCallback: (() -> Unit)? = null
    private var goToSignupCallback: (() -> Unit)? = null

    @Input("email_login", twoWay = true)
    var email: String = ""

    @Input("pswd_login", twoWay = true)
    var password: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    @OnEditorAction("pswd_login")
    fun onPasswordDone() {
        login()
    }

    fun setCallbacks(loginSuccess: () -> Unit, goToSignup: () -> Unit) {
        loginSuccessCallback = loginSuccess
        goToSignupCallback = goToSignup
    }

    @OnClick("login_button")
    fun login() {
        if (validateInputs()) {
            val result = authService.login(email, password)
            result.onSuccess {
                Toast.makeText(context, "Connexion réussie", Toast.LENGTH_SHORT).show()
                loginSuccessCallback?.invoke()
            }.onFailure {
                Toast.makeText(context, "Échec de la connexion: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @OnClick("go_to_signup_button")
    fun goToSignup() {
        goToSignupCallback?.invoke()
    }

    private fun validateInputs(): Boolean {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}