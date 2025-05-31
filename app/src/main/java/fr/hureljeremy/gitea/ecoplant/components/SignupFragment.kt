package fr.hureljeremy.gitea.ecoplant.components

import android.os.Bundle
import android.util.Log
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

class SignupFragment : BaseFragment() {

    @Inject
    private lateinit var authService: AuthService

    private var signupSuccessCallback: (() -> Unit)? = null
    private var goToLoginCallback: (() -> Unit)? = null

    @Input("email_signup", twoWay = true)
    var email: String = ""

    @Input("pswd_signup", twoWay = true)
    var password: String = ""

    @Input("confirm_pswd_signup", twoWay = true)
    var confirmPassword: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    @OnEditorAction("confirm_pswd_signup")
    fun onConfirmPasswordDone() {
        createAccount()
    }

    fun setCallbacks(signupSuccess: () -> Unit, goToLogin: () -> Unit) {
        signupSuccessCallback = signupSuccess
        goToLoginCallback = goToLogin
    }

    @OnClick("create_account_button")
    fun createAccount() {
        Log.d("SignupFragment", "Creating account with email: $email")

        if (!validateInputs()) {
            return
        }


        authService.register(email, password)
        Toast.makeText(context, "Compte créé avec succès", Toast.LENGTH_SHORT).show()
        signupSuccessCallback?.invoke()
    }

    @OnClick("login_account_button")
    fun goToLogin() {
        goToLoginCallback?.invoke()
    }

    private fun validateInputs(): Boolean {

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(context, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true
    }
}