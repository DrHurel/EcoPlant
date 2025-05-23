package fr.hureljeremy.gitea.ecoplant


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment


class CameraButtonsFragment : Fragment() {
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
             val previewView = (activity as? ScannerActivity)?.findViewById<PreviewView>(R.id.preview_view)
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

       view.findViewById<ImageButton>(R.id.go_home_button).setOnClickListener {
           (activity as? ScannerActivity)?.navigationService?.navigate(
               requireContext(),
               "home"
           )
        }
    }



}