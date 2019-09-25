package com.taiwan.justvet.justpet.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Outline
import android.os.Bundle
import android.renderscript.Allocation
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.PHOTO_FROM_GALLERY
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogNewProfileBinding
import kotlinx.android.synthetic.main.activity_main.*

class PetProfileDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogNewProfileBinding
    private lateinit var petImage: ImageView
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

        petImage = binding.imagePet
        petImage.clipToOutline = true
        petImage.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.setRoundRect(0, 0, view.width, view.height, 36f)
            }
        }

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
                dismiss()
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

        binding.layoutImage.setOnClickListener {
            startGallery()
        }

        viewModel.leaveDialog.observe(this, Observer {
            it?.let {
                if (it) {
                    dismiss()
                    viewModel.leaveDialogCompleted()
                }
            }
        })

        return binding.root
    }

    fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PHOTO_FROM_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let { data ->
                            data.data?.let {
                                viewModel.petImage.value = it.toString()
                                Glide.with(this).load(it).into(petImage)
                            }
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResult", resultCode.toString())
                    }
                }
            }
        }
    }

}