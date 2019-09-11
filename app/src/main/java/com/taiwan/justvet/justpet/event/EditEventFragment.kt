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
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.FragmentEditEventBinding
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class EditEventFragment : Fragment() {

    private lateinit var binding: FragmentEditEventBinding
    private lateinit var viewModel: EditEventViewModel
    private lateinit var currentEvent: PetEvent
//    lateinit var saveUri: Uri
//    lateinit var eventPicture: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditEventBinding.inflate(inflater, container, false)
        currentEvent = EditEventFragmentArgs.fromBundle(arguments!!).petEvent
        val viewModelFactory = EditEventViewModelFactory(currentEvent)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(EditEventViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.petProfile = currentEvent.petProfile

        viewModel.navigateToCalendar.observe(this, Observer {
            if (it == true) {
                (activity as MainActivity).nav_bottom_view.selectedItemId = R.id.nav_bottom_calendar
                viewModel.navigateToCalendarCompleted()
            }
        })

        setupSeekBar()

//        binding.buttonCamera.setOnClickListener {
//            startCamera()
//        }

//        binding.buttonGallery.setOnClickListener {
//            startGallery()
//        }

        setupTagRecyclerView()

//        eventPicture = binding.imageEvent

        return binding.root
    }

    private fun setupTagRecyclerView() {
        val listOfTag = binding.listTags
        val adapter = EditEventTagAdapter(viewModel, EditEventTagAdapter.OnClickListener {
        })
        listOfTag.adapter = adapter
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            PHOTO_FROM_GALLERY -> {
//                when (resultCode) {
//                    Activity.RESULT_OK -> {
//                        val uri = data!!.data
//                        if (eventPicture.visibility == View.GONE) {
//                            eventPicture.visibility = View.VISIBLE
//                        }
//                        eventPicture.setImageURI(uri)
//                    }
//                    Activity.RESULT_CANCELED -> {
//                        Log.wtf("getImageResult", resultCode.toString())
//                    }
//                }
//            }
//
//            PHOTO_FROM_CAMERA -> {
//                when (resultCode) {
//                    Activity.RESULT_OK -> {
//                        if (eventPicture.visibility == View.GONE) {
//                            eventPicture.visibility = View.VISIBLE
//                        }
//                        Glide.with(this).load(saveUri).into(eventPicture)
//                    }
//                    Activity.RESULT_CANCELED -> {
//                        Log.wtf("getImageResult", resultCode.toString())
//                    }
//                }
//
//            }
//        }
//    }

//    fun startCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        val tmpFile = File(
//            JustPetApplication.appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//            System.currentTimeMillis().toString() + ".png"
//        )
//        val uriForCamera = FileProvider.getUriForFile(
//            JustPetApplication.appContext,
//            "com.taiwan.justvet.justpet.provider",
//            tmpFile
//        )
//        saveUri = uriForCamera
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForCamera)
//
//        startActivityForResult(intent, PHOTO_FROM_CAMERA)
//    }
//
//    fun startGallery() {
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        startActivityForResult(intent, PHOTO_FROM_GALLERY)
//    }

    fun setupSeekBar() {
        val seekBarSpirit = binding.seekBarSpirit
        val seekBarAppetite = binding.seekBarAppetite

        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            seekBarSpirit.correctOffsetWhenContainerOnScrolling()
            seekBarAppetite.correctOffsetWhenContainerOnScrolling()
        }

        seekBarSpirit.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListenerAdapter() {
            override fun getProgressOnFinally(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                viewModel.setSpiritScore(progressFloat)
            }
        }

        seekBarAppetite.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListenerAdapter() {
            override fun getProgressOnFinally(
                bubbleSeekBar: BubbleSeekBar?,
                progress: Int,
                progressFloat: Float,
                fromUser: Boolean
            ) {
                viewModel.setAppetiteScore(progressFloat)
            }
        }

        currentEvent.spirit?.let { seekBarSpirit.setProgress(it.toFloat()) }
        currentEvent.appetite?.let { seekBarAppetite.setProgress(it.toFloat()) }

    }
}
