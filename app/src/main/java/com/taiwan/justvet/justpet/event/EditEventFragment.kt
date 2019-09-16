package com.taiwan.justvet.justpet.event

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.icu.util.Calendar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.FragmentEditEventBinding
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_main.*
import android.graphics.BitmapFactory
import android.graphics.Outline
import androidx.room.util.CursorUtil.getColumnIndex
import android.provider.MediaStore
import android.view.ViewOutlineProvider
import android.widget.ImageView
import com.bumptech.glide.Glide


class EditEventFragment : Fragment() {

    private lateinit var binding: FragmentEditEventBinding
    private lateinit var viewModel: EditEventViewModel
    private lateinit var currentEvent: PetEvent
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var calendar: Calendar
//    lateinit var saveUri: Uri
    lateinit var eventPicture: ImageView

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

        Log.d(ERIC, "event Id : ${currentEvent.eventId}")

        viewModel.navigateToCalendar.observe(this, Observer {
            if (it == true) {
                (activity as MainActivity).nav_bottom_view.selectedItemId = R.id.nav_bottom_calendar
                viewModel.navigateToCalendarCompleted()
            }
        })

        setupSeekBar()

        binding.layoutMedia.setOnClickListener {
            startGallery()
        }

//        binding.buttonGallery.setOnClickListener {
//            startGallery()
//        }

        calendar = viewModel.calendar
        setupDatePickerDialog()
        setupTimePickerDialog()

        setupTagRecyclerView()

        eventPicture = binding.imageMedia
        eventPicture.clipToOutline = true
        eventPicture.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.setRoundRect(0, 0, view.width, view.height, 12F)
            }
        }

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
                        data?.let { data ->
                            data.data?.let {
                                if (eventPicture.visibility == View.GONE) {
                                    eventPicture.visibility = View.VISIBLE
                                }
                                Glide.with(this).load(it).into(eventPicture)
                                Log.d(ERIC, it.path)
                            }
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResult", resultCode.toString())
                    }
                }
            }

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
        }
    }

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
    fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }

    fun setupSeekBar() {
        val seekBarSpirit = binding.seekBarSpirit
        val seekBarAppetite = binding.seekBarAppetite

        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            seekBarSpirit.correctOffsetWhenContainerOnScrolling()
            seekBarAppetite.correctOffsetWhenContainerOnScrolling()
        }

        seekBarSpirit.onProgressChangedListener =
            object : BubbleSeekBar.OnProgressChangedListenerAdapter() {
                override fun getProgressOnFinally(
                    bubbleSeekBar: BubbleSeekBar?,
                    progress: Int,
                    progressFloat: Float,
                    fromUser: Boolean
                ) {
                    viewModel.setSpiritScore(progressFloat)
                }
            }

        seekBarAppetite.onProgressChangedListener =
            object : BubbleSeekBar.OnProgressChangedListenerAdapter() {
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

    private fun setupDatePickerDialog() {
        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                viewModel.showDateDialogCompleted()
                calendar.set(year, month, dayOfMonth)
                viewModel.showTimePickerDialog()
            }
        datePickerDialog = DatePickerDialog(
            this.context!!,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        viewModel.showDatePickerDialog.observe(this, Observer {
            if (it == true) {
                datePickerDialog.show()
                viewModel.showDateDialogCompleted()
            }
        })
    }

    private fun setupTimePickerDialog() {
        val timeListener =
            TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
                viewModel.showTimeDialogCompleted()
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                viewModel.updateDateAndTime()
            }
        timePickerDialog = TimePickerDialog(
            this.context,
            timeListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        viewModel.showTimePickerDialog.observe(this, Observer {
            if (it == true) {
                timePickerDialog.show()
                viewModel.showTimeDialogCompleted()
            }
        })
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        // Result code is RESULT_OK only if the user selects an Image
//        if (resultCode == Activity.RESULT_OK)
//            when (requestCode) {
//                GALLERY_REQUEST_CODE -> {
//                    //data.getData return the content URI for the selected Image
//                    val selectedImage = data!!.data
//                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
//                    // Get the cursor
//                    val cursor =
//                        getContentResolver().query(selectedImage, filePathColumn, null, null, null)
//                    // Move to first row
//                    cursor.moveToFirst()
//                    //Get the column index of MediaStore.Images.Media.DATA
//                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
//                    //Gets the String value in the column
//                    val imgDecodableString = cursor.getString(columnIndex)
//                    cursor.close()
//                    // Set the Image in ImageView after decoding the String
//                    imageView.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString))
//                }
//            }
//    }
}
