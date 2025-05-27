package fr.hureljeremy.gitea.ecoplant

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import fr.hureljeremy.gitea.ecoplant.framework.BaseFragment
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.Organ
import fr.hureljeremy.gitea.ecoplant.services.CameraService
import fr.hureljeremy.gitea.ecoplant.services.PlantNetService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuScannerFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_scanner, container, false)
    }

    @Inject
    private lateinit var plantNetService: PlantNetService

    @Inject
    private lateinit var cameraService: CameraService


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retour au fragment caméra quand on clique sur la croix
        view.findViewById<ImageButton>(R.id.take_pic_button).setOnClickListener {
            (activity as? ScannerActivity)?.cameraService?.deleteLastPicture()
            (activity as? ScannerActivity)?.returnToCamera()
            parentFragmentManager.beginTransaction()
                .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
                .commit()
        }

        // Les bouton des choix : BARK, FLOWER, LEAF, FRUIT
        view.findViewById<ImageButton>(R.id.bark_button).setOnClickListener {
            Log.w(TAG, "Bark button clicked")
            val imagePath = cameraService.getLastImageUri()
            if (imagePath != null) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val res = plantNetService.identifyPlant(imagePath, Organ.BARK)
                    withContext(Dispatchers.Main) {
                        navigateToDisplayPlantInfo("bark")
                    }
                }
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
                    .commit()
            }


        }

        view.findViewById<ImageButton>(R.id.flower_button).setOnClickListener {
            val imagePath = cameraService.getLastImageUri()
            if (imagePath != null) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val res = plantNetService.identifyPlant(imagePath, Organ.FLOWER)
                    Log.d(TAG, "Flower identification result: $res")
                    withContext(Dispatchers.Main) {
                        navigateToDisplayPlantInfo("flower")
                    }
                }
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
                    .commit()
            }
        }

        view.findViewById<ImageButton>(R.id.leaf_button).setOnClickListener {
            val imagePath = cameraService.getLastImageUri()
            if (imagePath != null) {

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val res = plantNetService.identifyPlant(imagePath, Organ.LEAF)
                    withContext(Dispatchers.Main) {
                        navigateToDisplayPlantInfo("leaf")
                    }
                }
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
                    .commit()
            }
        }

        view.findViewById<ImageButton>(R.id.fruit_button).setOnClickListener {
            val imagePath = cameraService.getLastImageUri()
            if (imagePath != null) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val res = plantNetService.identifyPlant(imagePath, Organ.FRUIT)
                    withContext(Dispatchers.Main) {
                        navigateToDisplayPlantInfo("fruit")
                    }
                }

            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
                    .commit()
            }
        }


    }

    private fun navigateToDisplayPlantInfo(plantPart: String) {
        val intent = Intent(requireContext(), DisplayPlantInfoActivity::class.java)
        intent.putExtra("PLANT_PART", plantPart)
        startActivity(intent)
    }

}