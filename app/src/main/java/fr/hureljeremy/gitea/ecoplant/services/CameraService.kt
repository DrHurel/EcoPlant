package fr.hureljeremy.gitea.ecoplant.services

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import fr.hureljeremy.gitea.ecoplant.framework.BaseActivity
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider


@ServiceProvider
class CameraService : BaseService() {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lastImageUri: Uri? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("CameraService", "Service created")
    }

    fun initializeCamera(activity: BaseActivity, previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    activity,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraService", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    fun takePicture(): Boolean {
        return imageCapture != null
    }

    fun savePicture(bitmap: Bitmap): Uri? {
        try {
            val filename = "ECO_PLANT_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            applicationContext?.contentResolver?.let { resolver ->
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { imageUri ->
                    resolver.openOutputStream(imageUri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    lastImageUri = imageUri
                    return imageUri
                }
            }
        } catch (e: Exception) {
            Log.e("CameraService", "Error saving picture", e)
        }
        return null
    }

    fun deleteLastPicture(): Boolean {
        lastImageUri?.let { uri ->
            try {
                applicationContext?.contentResolver?.let { resolver ->
                    val deletedRows = resolver.delete(uri, null, null)
                    if (deletedRows > 0) {
                        Log.d("CameraService", "Image supprimée avec succès: $uri")
                        lastImageUri = null
                        return true
                    } else {
                        Log.d("CameraService", "Échec de la suppression: $uri")
                    }
                }
            } catch (e: Exception) {
                Log.e("CameraService", "Erreur lors de la suppression de l'image", e)
            }
        }
        return false
    }
}