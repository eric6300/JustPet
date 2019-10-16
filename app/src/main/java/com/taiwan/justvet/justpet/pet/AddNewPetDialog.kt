package com.taiwan.justvet.justpet.pet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Outline
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.PHOTO_FROM_GALLERY
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogNewPetBinding
import kotlinx.android.synthetic.main.activity_main.*

class AddNewPetDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogNewPetBinding
    private lateinit var petImage: ImageView
    private lateinit var iconCat: ImageView
    private lateinit var iconDog: ImageView
    private lateinit var iconFemale: ImageView
    private lateinit var iconMale: ImageView
    private val viewModel: AddNewPetViewModel by lazy {
        ViewModelProviders.of(this).get(AddNewPetViewModel::class.java)
    }

    private val quickPermissionsOption = QuickPermissionsOptions(
        handleRationale = false,
        permanentDeniedMethod = { permissionsPermanentlyDenied(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_new_pet, container, false
        )

        binding.let {
            it.lifecycleOwner = this
            it.viewModel = viewModel
            petImage = it.imagePet
            iconCat = it.iconCat
            iconDog = it.iconDog
            iconFemale = it.iconFemale
            iconMale = it.iconMale
        }

        petImage.let {
            it.clipToOutline = true
            it.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline?) {
                    outline?.setRoundRect(0, 0, view.width, view.height, 36f)
                }
            }
        }

        dialog?.setOnShowListener {
            val bottomSheetDialog = (it as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheetDialog?.let {
                BottomSheetBehavior.from(bottomSheetDialog).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        setupGenderIcon()
        setupSpeciesIcon()
        setupPetName()
        setupPetBirthday()

        viewModel.showGallery.observe(this, Observer {
            it?.let {
                if (it) {
                    showGallery()
                    viewModel.showGalleryCompleted()
                }
            }
        })

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

    private fun permissionsPermanentlyDenied(req: QuickPermissionsRequest) {
        this.context?.let {
            AlertDialog.Builder(it)
                .setMessage(getString(R.string.text_event_permission_message))
                .setPositiveButton(getString(R.string.text_open_app_setting)) { _, _ -> req.openAppSettings() }
                .setNegativeButton(getString(R.string.text_cancel)) { _, _ -> req.cancel() }
                .setCancelable(true)
                .show()
        }
    }

    fun showGallery() = runWithPermissions(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        options = quickPermissionsOption
    ) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }

    private fun setupSpeciesIcon() {
        viewModel.petSpecies.observe(this, Observer {
            when (it) {
                PetSpecies.CAT.value -> {
                    iconCat.alpha = 1.0f
                    iconDog.alpha = 0.15f
                }
                PetSpecies.DOG.value -> {
                    iconCat.alpha = 0.15f
                    iconDog.alpha = 1.0f
                }
            }
        })
    }

    private fun setupGenderIcon() {
        viewModel.petGender.observe(this, Observer {
            when (it) {
                PetGender.FEMALE.value -> {
                    iconFemale.alpha = 1.0f
                    iconMale.alpha = 0.15f
                }
                PetGender.MALE.value -> {
                    iconFemale.alpha = 0.15f
                    iconMale.alpha = 1.0f
                }
            }
        })
    }

    private fun setupPetName() {
        viewModel.petName.observe(this, Observer {
            it?.let {
                binding.layoutEditTextName.error = ""
            }
        })

        viewModel.errorName.observe(this, Observer {
            it?.let {
                binding.layoutEditTextName.error = it
            }
        })
    }

    private fun setupPetBirthday() {
        viewModel.petBirthday.observe(this, Observer {
            it?.let {
                binding.layoutEditTextBirthday.error = ""
            }
        })

        viewModel.errorBirthday.observe(this, Observer {
            it?.let {
                binding.layoutEditTextBirthday.error = it
            }
        })
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