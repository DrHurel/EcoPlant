package fr.hureljeremy.gitea.ecoplant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class MenuScannerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retour au fragment caméra quand on clique sur la croix
        view.findViewById<ImageButton>(R.id.take_pic_button).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
                .commit()
        }
    }
}