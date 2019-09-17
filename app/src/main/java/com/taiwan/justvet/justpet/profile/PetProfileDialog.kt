package com.taiwan.justvet.justpet.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogNewProfileBinding
import kotlinx.android.synthetic.main.activity_main.*

class PetProfileDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogNewProfileBinding
    private val viewModel: PetProfileViewModel by lazy {
        ViewModelProviders.of(this).get(PetProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogNewProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val iconCat = binding.iconCat
        val iconDog = binding.iconDog
        val iconFemale = binding.iconFemale
        val iconMale = binding.iconMale

        dialog?.setOnShowListener {

            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED

        }

        viewModel.navigateToHomeFragment.observe(this, Observer {
            if (it == true) {
                (activity as MainActivity).nav_bottom_view.selectedItemId = R.id.nav_bottom_home
                viewModel.navigateToHomeFragmentCompleted()
            }
        })

        viewModel.petSpecies.observe(this, Observer {
            when (it) {
                0L -> {
                    iconCat.alpha = 1.0f
                    iconDog.alpha = 0.2f
                }
                1L -> {
                    iconCat.alpha = 0.2f
                    iconDog.alpha = 1.0f
                }
            }
        })

        viewModel.petGender.observe(this, Observer {
            when (it) {
                0L -> {
                    iconFemale.alpha = 1.0f
                    iconMale.alpha = 0.2f
                }
                1L -> {
                    iconFemale.alpha = 0.2f
                    iconMale.alpha = 1.0f
                }
            }
        })

        return binding.root
    }

}