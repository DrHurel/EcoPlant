package fr.hureljeremy.gitea.ecoplant.pages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.components.MenuScannerFragment
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.CameraService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

@Page(route = "scanner", layout = "scanner_page", isDefault = false)
class ScannerActivity : BaseActivity() {
    @Inject
    lateinit var navigationService: NavigationService

    @Inject
    lateinit var cameraService: CameraService

    private lateinit var previewView: PreviewView
    private lateinit var capturedImageView: ImageView
    private var isShowingCapturedImage = false

    companion object {
        const val REQUEST_IMAGE_PICK = 1001
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previewView = findViewById(R.id.preview_view)
        capturedImageView = findViewById(R.id.captured_image_view)
        checkCameraPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                // Stocke l'URI dans CameraService pour réutilisation
                cameraService.setLastImageUri(imageUri)
                // Affiche l'image sélectionnée dans l'ImageView
                capturedImageView.setImageURI(imageUri)
                previewView.visibility = View.GONE
                capturedImageView.visibility = View.VISIBLE
                isShowingCapturedImage = true
                // Affiche le menu de sélection
                supportFragmentManager.beginTransaction()
                    .replace(R.id.camera_buttons_fragment, MenuScannerFragment())
                    .commit()
            }
        }
    }

    fun loadImage(intent: Intent, requestCode: Int) {
        startActivityForResult(intent, requestCode)
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        cameraService.initializeCamera(this, previewView)
    }

    fun onPhotoTaken(bitmap: Bitmap) {
        capturedImageView.setImageBitmap(bitmap)
        previewView.visibility = View.GONE
        capturedImageView.visibility = View.VISIBLE
        isShowingCapturedImage = true
    }

    fun returnToCamera() {
        if (isShowingCapturedImage) {
            previewView.visibility = View.VISIBLE
            capturedImageView.visibility = View.GONE
            isShowingCapturedImage = false
        }
    }

    fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}