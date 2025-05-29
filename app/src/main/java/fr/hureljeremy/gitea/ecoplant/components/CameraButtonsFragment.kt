package fr.hureljeremy.gitea.ecoplant.components


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.camera.view.PreviewView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.pages.ScannerActivity
import fr.hureljeremy.gitea.ecoplant.framework.BaseFragment


class CameraButtonsFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_camera_buttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cliquer sur le bouton de prise photo affiche le menu de sélection
        view.findViewById<ImageButton>(R.id.take_pic_button).setOnClickListener {
            if ((activity as? ScannerActivity)?.cameraService?.takePicture() == true) {
                val previewView =
                    (activity as? ScannerActivity)?.findViewById<PreviewView>(R.id.preview_view)
                previewView?.let { view ->
                    view.bitmap?.let { bitmap ->
                        (activity as? ScannerActivity)?.cameraService?.savePicture(bitmap)
                        (activity as? ScannerActivity)?.onPhotoTaken(bitmap)
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.camera_buttons_fragment, MenuScannerFragment())
                    .commit()


            }

        }
        // Cliquer sur le bouton de retour à la caméra
        view.findViewById<ImageButton>(R.id.load_pic_button).setOnClickListener {
            // Ouvre l'explorateur de fichiers pour sélectionner une image
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            // Lancer l'intent pour sélectionner l'image

            (activity as? ScannerActivity)?.loadImage(intent, ScannerActivity.REQUEST_IMAGE_PICK)
            // La navigation vers MenuScannerFragment se fera après avoir obtenu l'image dans onActivityResult
        }

        view.findViewById<ImageButton>(R.id.go_home_button).setOnClickListener {
            (activity as? ScannerActivity)?.navigationService?.navigate(
                requireContext(),
                "home"
            )
        }
    }


}