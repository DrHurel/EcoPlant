package fr.hureljeremy.gitea.ecoplant

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import fr.hureljeremy.gitea.ecoplant.services.AuthService


@Page(route = "setting", isDefault = false)
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
        setContentView(R.layout.setting_page)

        loggedInContainer = findViewById(R.id.logged_in_container)
        loginSignupContainer = findViewById(R.id.login_signup_container)
        loginContainer = findViewById(R.id.login_container)

        findViewById<Button>(R.id.go_back_button).setOnClickListener {
            navigationService.navigate(this, "home")
        }

        updateUIBasedOnLoginStatus()

        setupNavigationButtons()

        setupActionButtons()

        setupInputFields()

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

    private fun setupNavigationButtons() {
        findViewById<Button>(R.id.go_to_signup_button).setOnClickListener {
            loggedInContainer.visibility = View.GONE
            loginSignupContainer.visibility = View.VISIBLE
            loginContainer.visibility = View.GONE
        }
        findViewById<Button>(R.id.login_account_button).setOnClickListener {
            loggedInContainer.visibility = View.GONE
            loginSignupContainer.visibility = View.GONE
            loginContainer.visibility = View.VISIBLE
        }
    }

    private fun setupActionButtons() {
        findViewById<Button>(R.id.login_button).setOnClickListener {
            val email = findViewById<EditText>(R.id.email_login).text.toString()
            val password = findViewById<EditText>(R.id.pswd_login).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val result = authService.login(email, password)
                result.onSuccess {
                    Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show()
                    updateUIBasedOnLoginStatus()
                }.onFailure {
                    Toast.makeText(this, "Échec de la connexion: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.create_account_button).setOnClickListener {
            val email = findViewById<EditText>(R.id.email_signup).text.toString()
            val password = findViewById<EditText>(R.id.pswd_signup).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.confirm_pswd_signup).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    authService.register(email, password)
                    Toast.makeText(this, "Compte créé avec succès", Toast.LENGTH_SHORT).show()
                    updateUIBasedOnLoginStatus()
                } else {
                    Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton de suppression de compte
        findViewById<Button>(R.id.delete_user_button).setOnClickListener {
            Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show()
            updateUIBasedOnLoginStatus()
        }

        // Bouton de modification du nom d'utilisateur
        findViewById<Button>(R.id.change_user_name_button).setOnClickListener {
            val newUsername = findViewById<EditText>(R.id.user_name_edit_text).text.toString()
            if (newUsername.isNotEmpty()) {
                Toast.makeText(this, "Nom d'utilisateur modifié", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupInputFields() {
        // Configuration du clavier pour les champs de saisie
        findViewById<EditText>(R.id.pswd_login).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                findViewById<Button>(R.id.login_button).performClick()
                return@setOnEditorActionListener true
            }
            false
        }

        findViewById<EditText>(R.id.confirm_pswd_signup).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                findViewById<Button>(R.id.create_account_button).performClick()
                return@setOnEditorActionListener true
            }
            false
        }
    }


}