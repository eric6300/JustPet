package com.taiwan.justvet.justpet.event

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Outline
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.FragmentEventBinding
import com.taiwan.justvet.justpet.family.EMPTY_STRING
import com.taiwan.justvet.justpet.util.Converter
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_main.*

class EventFragment : Fragment() {

    private lateinit var binding: FragmentEventBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var currentEvent: PetEvent
    private lateinit var calendar: Calendar

    private val quickPermissionsOption = QuickPermissionsOptions(
        handleRationale = false,
        permanentDeniedMethod = { permissionsPermanentlyDenied(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        currentEvent = EventFragmentArgs.fromBundle(arguments!!).petEvent
        viewModel =
            ViewModelProviders.of(this, EventViewModelFactory(currentEvent))
                .get(EventViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_event, container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.converter = Converter
        binding.petProfile = currentEvent.petProfile

        viewModel.navigateToCalendarFragment.observe(this, Observer {
            it?.let {
                if (it) {
                    (activity as MainActivity).nav_bottom_view.selectedItemId =
                        R.id.nav_bottom_calendar
                    viewModel.navigateToCalendarCompleted()
                }
            }
        })

        viewModel.showGallery.observe(this, Observer {
            it?.let {
                if (it) {
                    showGallery()
                    viewModel.showGalleryCompleted()
                }
            }
        })

        setupSeekBarOfEvent()

        calendar = viewModel.calendar
        setupDatePickerDialog()
        setupTimePickerDialog()

        setupTagRecyclerView()

        binding.imageMedia.clipToOutline = true
        binding.imageMedia.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.setRoundRect(0, 0, view.width, view.height, 12F)
            }
        }

        //  Handle empty string can't transform to double issue
        viewModel.eventWeight.observe(this, Observer {
            it?.let {
                if (it == EMPTY_STRING) {
                    viewModel.defaultWeight()
                }
            }
        })

        //  Handle empty string can't transform to double issue
        viewModel.eventTemper.observe(this, Observer {
            it?.let {
                if (it == EMPTY_STRING) {
                    viewModel.defaultTemper()
                }
            }
        })

        //  Handle empty string can't transform to double issue
        viewModel.eventRr.observe(this, Observer {
            it?.let {
                if (it == EMPTY_STRING) {
                    viewModel.defaultRr()
                }
            }
        })

        //  Handle empty string can't transform to double issue
        viewModel.eventHr.observe(this, Observer {
            it?.let {
                if (it == EMPTY_STRING) {
                    viewModel.defaultHr()
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

    private fun setupTagRecyclerView() {
        binding.listTags.adapter = EventTagAdapter(viewModel)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PHOTO_FROM_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let { data ->
                            data.data?.let {
                                if (binding.imageMedia.visibility == View.GONE) {
                                    binding.imageMedia.visibility = View.VISIBLE
                                }
                                viewModel.eventImage.value = it.toString()
                                Glide.with(this).load(it).into(binding.imageMedia)
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

    fun showGallery() = runWithPermissions(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        options = quickPermissionsOption
    ) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }

    fun setupSeekBarOfEvent() {
        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            binding.seekBarSpirit.correctOffsetWhenContainerOnScrolling()
            binding.seekBarAppetite.correctOffsetWhenContainerOnScrolling()
        }

        binding.seekBarSpirit.onProgressChangedListener =
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

        binding.seekBarAppetite.onProgressChangedListener =
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

        currentEvent.spirit?.let { binding.seekBarSpirit.setProgress(it.toFloat()) }
        currentEvent.appetite?.let { binding.seekBarAppetite.setProgress(it.toFloat()) }

    }

    private fun setupDatePickerDialog() {
        val datePickerListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                viewModel.showDateDialogCompleted()
                viewModel.showTimePickerDialog()
            }

        val datePickerDialog = DatePickerDialog(
            this.context!!,
            datePickerListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        viewModel.showDatePickerDialog.observe(this, Observer {
            it?.let {
                if (it) {
                    datePickerDialog.show()
                    viewModel.showDateDialogCompleted()
                }
            }
        })
    }

    private fun setupTimePickerDialog() {
        val timePickerListener =
            TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                viewModel.showTimeDialogCompleted()
                viewModel.updateDateAndTimeOfEvent()
            }

        val timePickerDialog = TimePickerDialog(
            this.context,
            timePickerListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )

        viewModel.showTimePickerDialog.observe(this, Observer {
            it?.let {
                if (it) {
                    timePickerDialog.show()
                    viewModel.showTimeDialogCompleted()
                }
            }
        })
    }
}
