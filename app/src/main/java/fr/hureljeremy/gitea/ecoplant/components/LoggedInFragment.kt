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
    import fr.hureljeremy.gitea.ecoplant.services.AuthService

    class LoggedInFragment : BaseFragment() {

        @Inject
        private lateinit var authService: AuthService

        private var logoutCallback: (() -> Unit)? = null

        @Input(id = "user_name_edit_text", twoWay = true)
        var username: String = ""

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_logged_in, container, false)
        }

        fun setLogoutCallback(logout: () -> Unit) {
            logoutCallback = logout
        }

        @OnClick("change_user_name_button")
        fun changeUsername() {
            if (username.isNotEmpty()) {
                // Ici vous pourriez ajouter un appel à un service pour enregistrer le nom d'utilisateur
                Toast.makeText(context, "Nom d'utilisateur modifié", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Veuillez entrer un nom d'utilisateur", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        @OnClick("logout_button")
        fun logout() {
            authService.logout()
            Toast.makeText(context, "Déconnexion réussie", Toast.LENGTH_SHORT).show()
            logoutCallback?.invoke()
        }

        @OnClick("delete_user_button")
        fun deleteAccount() {
            // Appel au service pour supprimer le compte
            authService.logout() // Déconnexion après suppression
            Toast.makeText(context, "Compte supprimé", Toast.LENGTH_SHORT).show()
            logoutCallback?.invoke()
        }
    }