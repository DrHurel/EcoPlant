package fr.hureljeremy.gitea.ecoplant.pages

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.AuthService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "setting", layout = "setting_page", isDefault = false)
class SettingActivity : BaseActivity() {
    @Inject
    private lateinit var navigationService: NavigationService

    @Inject
    private lateinit var authService: AuthService

    private lateinit var loggedInContainer: LinearLayout
    private lateinit var loginSignupContainer: LinearLayout
    private lateinit var loginContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Plus besoin d'appeler setContentView, géré par BaseActivity

        loggedInContainer = findViewById(R.id.logged_in_container)
        loginSignupContainer = findViewById(R.id.login_signup_container)
        loginContainer = findViewById(R.id.login_container)

        updateUIBasedOnLoginStatus()
        setupInputFields()
    }

    @OnClick("go_back_button")
    fun navigateToHome() {
        navigationService.navigate(this, "home")
    }

    @OnClick("go_to_signup_button")
    fun showSignupForm() {
        loggedInContainer.visibility = View.GONE
        loginSignupContainer.visibility = View.VISIBLE
        loginContainer.visibility = View.GONE
    }

    @OnClick("login_account_button")
    fun showLoginForm() {
        loggedInContainer.visibility = View.GONE
        loginSignupContainer.visibility = View.GONE
        loginContainer.visibility = View.VISIBLE
    }

    @OnClick("login_button")
    fun login() {
        val email = findViewById<EditText>(R.id.email_login).text.toString()
        val password = findViewById<EditText>(R.id.pswd_login).text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            val result = authService.login(email, password)
            result.onSuccess {
                Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
                updateUIBasedOnLoginStatus()
            }.onFailure {
                Toast.makeText(this, "Échec de la connexion: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick("create_account_button")
    fun createAccount() {
        val email = findViewById<EditText>(R.id.email_signup).text.toString()
        val password = findViewById<EditText>(R.id.pswd_signup).text.toString()
        val confirmPassword = findViewById<EditText>(R.id.confirm_pswd_signup).text.toString()

        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (password == confirmPassword) {
                authService.register(email, password)
                Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show()
                updateUIBasedOnLoginStatus()
            } else {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
        }
    }

    @OnClick("delete_user_button")
    fun deleteAccount() {
        Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show()
        updateUIBasedOnLoginStatus()
    }

    @OnClick("change_user_name_button")
    fun changeUsername() {
        val newUsername = findViewById<EditText>(R.id.user_name_edit_text).text.toString()
        if (newUsername.isNotEmpty()) {
            Toast.makeText(this, "Nom d'utilisateur modifié", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIBasedOnLoginStatus() {
        if (authService.isLoggedIn()) {
            // L'utilisateur est connecté, afficher le conteneur de profil
            loggedInContainer.visibility = View.VISIBLE
            loginSignupContainer.visibility = View.GONE
            loginContainer.visibility = View.GONE

            // Mettre à jour les informations du profil si nécessaire
            val userNameEditText = findViewById<EditText>(R.id.user_name_edit_text)
        } else {
            // L'utilisateur n'est pas connecté, afficher le formulaire de connexion par défaut
            loggedInContainer.visibility = View.GONE
            loginSignupContainer.visibility = View.GONE
            loginContainer.visibility = View.VISIBLE
        }
    }

    private fun setupInputFields() {
        // Configuration du clavier pour les champs de saisie
        findViewById<EditText>(R.id.pswd_login).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login()
                return@setOnEditorActionListener true
            }
            false
        }

        findViewById<EditText>(R.id.confirm_pswd_signup).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createAccount()
                return@setOnEditorActionListener true
            }
            false
        }
    }
}