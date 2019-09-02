package com.taiwan.justvet.justpet.event

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.ScrollView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.MainActivity.Companion.PHOTO_FROM_CAMERA
import com.taiwan.justvet.justpet.MainActivity.Companion.PHOTO_FROM_GALLERY
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentEditEventBinding
import com.taiwan.justvet.justpet.tag.TagListAdapter
import com.taiwan.justvet.justpet.tag.TagViewModel
import com.taiwan.justvet.justpet.util.bindGenderIcon
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class EditEventFragment : Fragment() {

    private lateinit var binding: FragmentEditEventBinding
    private val viewModel: EditEventViewModel by lazy {
        ViewModelProviders.of(this).get(EditEventViewModel::class.java)
    }
    lateinit var saveUri: Uri
    lateinit var eventImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditEventBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        eventImage = binding.imageEvent

        viewModel.navigateToCalendar.observe(this, Observer {
            if (it == true) {
                (activity as MainActivity).nav_bottom_view.selectedItemId = R.id.nav_bottom_calendar
                viewModel.navigateToCalendarCompleted()
            }
        })

        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            binding.seekBarAppetite.correctOffsetWhenContainerOnScrolling()
            binding.seekBarSpirit.correctOffsetWhenContainerOnScrolling()
        }

        binding.buttonCamera.setOnClickListener {
            startCamera()
        }

        binding.buttonGallery.setOnClickListener {
            startGallery()
        }

        setupTagRecyclerView()

        return binding.root
    }

    private fun setupTagRecyclerView() {
        val listOfTag = binding.listTags
        val adapter = EditEventTagAdapter(viewModel, EditEventTagAdapter.OnClickListener {
        })
        listOfTag.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PHOTO_FROM_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = data!!.data
                        if (eventImage.visibility == View.GONE) {
                            eventImage.visibility = View.VISIBLE
                        }
                        eventImage.setImageURI(uri)
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResult", resultCode.toString())
                    }
                }
            }

            PHOTO_FROM_CAMERA -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (eventImage.visibility == View.GONE) {
                            eventImage.visibility = View.VISIBLE
                        }
                        Glide.with(this).load(saveUri).into(eventImage)
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResult", resultCode.toString())
                    }
                }

            }
        }
    }

    fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val tmpFile = File(
            JustPetApplication.appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            System.currentTimeMillis().toString() + ".png"
        )
        val uriForCamera = FileProvider.getUriForFile(
            JustPetApplication.appContext,
            "com.taiwan.justvet.justpet.provider",
            tmpFile
        )
        saveUri = uriForCamera
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForCamera)

        startActivityForResult(intent, PHOTO_FROM_CAMERA)
    }

    fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }
}
