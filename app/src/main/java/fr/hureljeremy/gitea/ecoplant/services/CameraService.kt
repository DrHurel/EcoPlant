package fr.hureljeremy.gitea.ecoplant.services

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import fr.hureljeremy.gitea.ecoplant.framework.BaseService
import fr.hureljeremy.gitea.ecoplant.framework.ServiceProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException



@ServiceProvider
class CameraService : BaseService() {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("CameraService", "Service created")
    }

    fun initializeCamera(activity: ComponentActivity, previewView: PreviewView) {
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
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let { imageUri ->
                    resolver.openOutputStream(imageUri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    return imageUri
                }
            }
        } catch (e: Exception) {
            Log.e("CameraService", "Error saving picture", e)
        }
        return null
    }
}