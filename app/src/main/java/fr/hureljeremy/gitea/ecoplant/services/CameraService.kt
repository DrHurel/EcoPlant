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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraService : Service() {

    private lateinit var imageCapture: ImageCapture
    private var savedImagePath: String? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private var previewView: PreviewView? = null

    inner class LocalBinder : Binder() {
        fun getService(): CameraService = this@CameraService
        fun bindPreview(previewView: PreviewView) {
            this@CameraService.previewView = previewView
            startCamera()
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startCamera() {
        val preview = Preview.Builder()
            .build()
            .also {
                previewView?.let { view ->
                    it.surfaceProvider = view.surfaceProvider
                }
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                previewView?.context as LifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
        } catch (e: Exception) {
            Log.e("CameraService", "Use case binding failed", e)
        }
    }


    fun takePicture(): Boolean {
        val photoFile = File(
            requireNotNull(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)),
            "IMG_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        var success = false

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(applicationContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    savedImagePath = photoFile.absolutePath

                    success = true
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraService", "Photo capture failed: ${exc.message}", exc)
                    success = false
                }
            }
        )

        return success
    }

    fun savePicture(bitmap: Bitmap): String? {
        return try {
            val file = File(
                requireNotNull(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)),
                "IMG_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            savedImagePath = file.absolutePath
            file.absolutePath
        } catch (e: IOException) {
            Log.e("CameraService", "Failed to save picture", e)
            null
        }
    }

    fun storePicture(uri: Uri): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            val resolver = applicationContext.contentResolver
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    resolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            } ?: false
        } catch (e: IOException) {
            Log.e("CameraService", "Failed to store picture", e)
            false
        }
    }

    fun deletePicture(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: SecurityException) {
            Log.e("CameraService", "Failed to delete picture", e)
            false
        }
    }

    fun loadPicture(path: String): Bitmap? {

        return try {
            BitmapFactory.decodeFile(path)?.also { bitmap ->

                savedImagePath = path
            }
        } catch (e: IOException) {
            Log.e("CameraService", "Failed to load picture", e)
            null
        }
    }
}