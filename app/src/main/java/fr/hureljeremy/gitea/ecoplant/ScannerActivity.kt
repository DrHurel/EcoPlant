package fr.hureljeremy.gitea.ecoplant

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.BaseFragmentActivity
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Page
import fr.hureljeremy.gitea.ecoplant.services.CameraService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import java.io.File

@Page(route = "scanner", isDefault = false)
class ScannerActivity : BaseActivity() {
    @Inject
    lateinit var navigationService: NavigationService

    @Inject
    lateinit var cameraService: CameraService

    private lateinit var previewView: PreviewView
    private lateinit var capturedImageView: ImageView
    private var isShowingCapturedImage = false

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
        setContentView(R.layout.scanner_page)
        previewView = findViewById(R.id.preview_view)
        capturedImageView = findViewById(R.id.captured_image_view)
        checkCameraPermission()
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
}