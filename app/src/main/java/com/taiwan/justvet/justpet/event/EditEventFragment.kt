package com.taiwan.justvet.justpet.event

import android.Manifest
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
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest


class EditEventFragment : Fragment() {

    private lateinit var binding: FragmentEditEventBinding
    private lateinit var viewModel: EditEventViewModel
    private lateinit var currentEvent: PetEvent
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var calendar: Calendar
    lateinit var eventPicture: ImageView

    private val quickPermissionsOption = QuickPermissionsOptions(
        handleRationale = false,
        permanentDeniedMethod = { permissionsPermanentlyDenied(it) }
    )

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

        viewModel.startGallery.observe(this, Observer {
            it?.let{
                if (it) {
                    startGallery()
                    viewModel.startGalleryCompleted()
                }
            }
        })

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

    private fun permissionsPermanentlyDenied(req: QuickPermissionsRequest) {
        // this will be called when some/all permissions required by the method are permanently
        // denied. Handle it your way.
        this.context?.let {
            AlertDialog.Builder(it)
//                .setTitle("Permissions Denied")
                .setMessage("開啟「設定」，點選「權限」，並開啟「儲存」")
                .setPositiveButton("開啟「設定」") { _, _ -> req.openAppSettings() }
                .setNegativeButton("取消") { _, _ -> req.cancel() }
                .setCancelable(true)
                .show()
        }
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
                                viewModel.eventImage.value = it.toString()
                                Glide.with(this).load(it).into(eventPicture)
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

    fun startGallery() = runWithPermissions(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        options = quickPermissionsOption
    ) {
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
}
