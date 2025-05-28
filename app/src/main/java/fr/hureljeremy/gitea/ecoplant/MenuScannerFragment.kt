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
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.services.CameraService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.services.PlantNetService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.toString

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

    @Inject
    private lateinit var navigationService: NavigationService


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
            identifyPlant("bark", Organ.BARK)
        }

        view.findViewById<ImageButton>(R.id.flower_button).setOnClickListener {
            identifyPlant("flower", Organ.FLOWER)
        }

        view.findViewById<ImageButton>(R.id.leaf_button).setOnClickListener {
            identifyPlant("leaf", Organ.LEAF)
        }

        view.findViewById<ImageButton>(R.id.fruit_button).setOnClickListener {
            identifyPlant("fruit", Organ.FRUIT)
        }


    }

    private fun identifyPlant(part : String,organ: Organ) {
        val imagePath = cameraService.getLastImageUri()
        if (imagePath != null) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val res = plantNetService.identifyPlant(imagePath, organ)
                Log.d(TAG, "Flower identification result: $res")
                if (res.isFailure) {
                    navigationService.navigateToDefault(requireContext())
                }
                navigateToDisplayPlantInfo(part, res.getOrThrow())

            }
        } else {
            parentFragmentManager.beginTransaction()
                .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
                .commit()
        }
    }

    private fun navigateToDisplayPlantInfo(plantPart: String,identificationResult: SavedIdentificationResult) {
        navigationService.navigate(requireContext(), "plant_info", Bundle().apply {
            putString("PLANT_PART", plantPart)
            putString(
                "PLANT_NAME",
                identificationResult.species
            )
            putString(
                "PLANT_DESCRIPTION",
                identificationResult.description
            )
            putString("PLANT_IMAGE_URI", cameraService.getLastImageUri()?.toString())
        })
    }

}