package fr.hureljeremy.gitea.ecoplant.pages

import android.os.Bundle
import androidx.fragment.app.Fragment
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.components.LoggedInFragment
import fr.hureljeremy.gitea.ecoplant.components.LoginFragment
import fr.hureljeremy.gitea.ecoplant.components.SignupFragment
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

    private var currentFragment: Fragment? = null
    private val loginFragment by lazy { LoginFragment() }
    private val signupFragment by lazy { SignupFragment() }
    private val loggedInFragment by lazy { LoggedInFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFragments()
        updateUIBasedOnLoginStatus()
    }

    override fun onResume() {
        super.onResume()
        updateUIBasedOnLoginStatus()
    }

    private fun setupFragments() {
        loginFragment.setCallbacks(
            loginSuccess = ::onAuthenticationSuccess,
            goToSignup = { showFragment(signupFragment) }
        )

        signupFragment.setCallbacks(
            signupSuccess = ::onAuthenticationSuccess,
            goToLogin = { showFragment(loginFragment) }
        )

        loggedInFragment.setLogoutCallback {
            onLogout()
        }
    }

    private fun onAuthenticationSuccess() {
        updateUIBasedOnLoginStatus()
    }

    private fun onLogout() {
        updateUIBasedOnLoginStatus()
    }

    @OnClick("go_back_button")
    fun navigateToHome() {
        navigationService.navigate(this, "home")
    }

    private fun updateUIBasedOnLoginStatus() {
        val targetFragment = if (authService.isLoggedIn()) {
            loggedInFragment
        } else {
            loginFragment
        }

        showFragment(targetFragment)
    }

    private fun showFragment(fragment: Fragment) {
        if (currentFragment === fragment) return

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        currentFragment = fragment
    }
}