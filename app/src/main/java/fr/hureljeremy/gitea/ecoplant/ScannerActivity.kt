package fr.hureljeremy.gitea.ecoplant

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import fr.hureljeremy.gitea.ecoplant.services.CameraService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService

class ScannerActivity : FragmentActivity() {

    lateinit var navigationService: NavigationService
    lateinit var cameraService: CameraService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scanner_page)

        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = findViewById<PreviewView>(R.id.preview_view).surfaceProvider
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview
                )
            } catch (e: Exception) {
                Log.e("Camera", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as NavigationService.LocalBinder
            navigationService = binder.getService()

            binder.updateCurrentDestination(this::class.java)

        }

        override fun onServiceDisconnected(name: ComponentName) {

        }
    }

//    private  val cameraConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            val binder = service as CameraService.LocalBinder
//            cameraService = binder.getService()
//            binder.bindPreview(findViewById(R.id.preview_view))
//        }
//
//        override fun onServiceDisconnected(name: ComponentName) {
//
//        }
//    }

    override fun onStart() {
        super.onStart()
        Intent(this, NavigationService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)

        }
//        Intent(this, NavigationService::class.java).also { intent ->
//            bindService(intent, cameraConnection, Context.BIND_AUTO_CREATE)
//        }


    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
//        unbindService(cameraConnection)
    }

}