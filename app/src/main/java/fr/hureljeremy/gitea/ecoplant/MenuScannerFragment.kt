package fr.hureljeremy.gitea.ecoplant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment



class MenuScannerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu_scanner, container, false)
    }

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
            navigateToDisplayPlantInfo("bark")
        }

        view.findViewById<ImageButton>(R.id.flower_button).setOnClickListener {
            navigateToDisplayPlantInfo("flower")
        }

        view.findViewById<ImageButton>(R.id.leaf_button).setOnClickListener {
            navigateToDisplayPlantInfo("leaf")
        }

        view.findViewById<ImageButton>(R.id.fruit_button).setOnClickListener {
            navigateToDisplayPlantInfo("fruit")
        }


    }

    private fun navigateToDisplayPlantInfo(plantPart: String) {
        val intent = Intent(requireContext(), DisplayPlantInfoActivity::class.java)
        intent.putExtra("PLANT_PART", plantPart)
        startActivity(intent)
    }

}