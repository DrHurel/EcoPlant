package fr.hureljeremy.gitea.ecoplant.components

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.view.PreviewView
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseFragment
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.pages.ScannerActivity
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

class CameraButtonsFragment : BaseFragment() {

    private val scannerActivity: ScannerActivity?
        get() = activity as? ScannerActivity


    @Inject
    private lateinit var navigationService : NavigationService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera_buttons, container, false)
    }

    @OnClick("take_pic_button")
    fun onTakePictureClick() {
        scannerActivity?.let { scanner ->
            if (scanner.cameraService.takePicture()) {
                scanner.findViewById<PreviewView>(R.id.preview_view)?.bitmap?.let { bitmap ->
                    scanner.cameraService.savePicture(bitmap)
                    scanner.onPhotoTaken(bitmap)
                }

                replaceWithMenuFragment()
            }
        }
    }

    @OnClick("load_pic_button")
    fun onLoadPictureClick() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        scannerActivity?.loadImage(intent, ScannerActivity.REQUEST_IMAGE_PICK)
    }

    @OnClick("go_home_button")
    fun onGoHomeClick() {
        navigationService.navigate(requireContext(), "home")
    }

    private fun replaceWithMenuFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.camera_buttons_fragment, MenuScannerFragment())
            .commit()
    }
}