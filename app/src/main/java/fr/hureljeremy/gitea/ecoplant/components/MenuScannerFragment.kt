package fr.hureljeremy.gitea.ecoplant.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import fr.hureljeremy.gitea.ecoplant.R
import fr.hureljeremy.gitea.ecoplant.framework.BaseFragment
import fr.hureljeremy.gitea.ecoplant.framework.Inject
import fr.hureljeremy.gitea.ecoplant.framework.OnClick
import fr.hureljeremy.gitea.ecoplant.framework.Organ
import fr.hureljeremy.gitea.ecoplant.framework.SavedIdentificationResult
import fr.hureljeremy.gitea.ecoplant.pages.ScannerActivity
import fr.hureljeremy.gitea.ecoplant.services.CameraService
import fr.hureljeremy.gitea.ecoplant.services.NavigationService
import fr.hureljeremy.gitea.ecoplant.services.PlantNetService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MenuScannerFragment : BaseFragment() {

    @Inject
    private lateinit var plantNetService: PlantNetService

    @Inject
    private lateinit var cameraService: CameraService

    @Inject
    private lateinit var navigationService: NavigationService

    private val scannerActivity: ScannerActivity?
        get() = activity as? ScannerActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_scanner, container, false)
    }


    @OnClick("take_pic_button")
    fun onRetakePhotoClick() {
        scannerActivity?.cameraService?.deleteLastPicture()
        scannerActivity?.returnToCamera()
        replaceByCameraButtonsFragment()
    }

    @OnClick("bark_button")
    fun onBarkClick() {
        identifyPlant("bark", Organ.BARK)
    }

    @OnClick("flower_button")
    fun onFlowerClick() {
        identifyPlant("flower", Organ.FLOWER)
    }

    @OnClick("leaf_button")
    fun onLeafClick() {
        identifyPlant("leaf", Organ.LEAF)
    }

    @OnClick("fruit_button")
    fun onFruitClick() {
        identifyPlant("fruit", Organ.FRUIT)
    }

    private fun identifyPlant(part: String, organ: Organ) {
        val imageUri = cameraService.getLastImageUri()

        if (imageUri == null) {
            replaceByCameraButtonsFragment()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val result = plantNetService.identifyPlant(imageUri, organ)

            if (result.isFailure) {
                navigationService.navigate(requireContext(), "scanner")
                scannerActivity?.runOnUiThread {
                    scannerActivity?.showError("Identification failed: ${result.exceptionOrNull()?.message}")
                }
                return@launch
            }

            navigateToDisplayPlantInfo(part, result.getOrThrow())
        }
    }

    private fun navigateToDisplayPlantInfo(
        plantPart: String,
        identificationResult: SavedIdentificationResult
    ) {
        navigationService.navigate(requireContext(), "plant_info", Bundle().apply {
            putString("PLANT_PART", plantPart)
            putString("PLANT_NAME", identificationResult.species)
            putString("PLANT_DESCRIPTION", identificationResult.description)
            putString("PLANT_IMAGE_URI", cameraService.getLastImageUri()?.toString())
        })
    }

    private fun replaceByCameraButtonsFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.camera_buttons_fragment, CameraButtonsFragment())
            .commit()
    }
}