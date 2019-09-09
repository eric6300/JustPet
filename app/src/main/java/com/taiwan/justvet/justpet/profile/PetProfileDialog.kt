package com.taiwan.justvet.justpet.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogNewProfileBinding
import com.taiwan.justvet.justpet.home.HomeViewModel
import com.taiwan.justvet.justpet.util.bindGenderIcon

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

        dialog?.setOnShowListener {

            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED

        }

        return binding.root
    }

}