package com.taiwan.justvet.justpet.tag

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TimePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.databinding.DialogTagBinding

class TagDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTagBinding
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var calendar: Calendar
    private lateinit var avatarAdapter: PetAvatarAdapter
    private val viewModel: TagViewModel by lazy {
        ViewModelProviders.of(this).get(TagViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogTagBinding.inflate(inflater, container, false)

        dialog?.setOnShowListener {

            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED

        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.leaveTagDialog.observe(this, Observer {
            if (it == true) {
                findNavController().popBackStack()
            }
        })

        viewModel.navigateToEditEvent.observe(this, Observer {
            if (it == true) {
                viewModel.currentEvent.value?.apply {
                    findNavController().navigate(
                        TagDialogDirections.actionTagDialogToEventDetailFragment(
                            this
                        )
                    )
                    viewModel.navigateToEditEventCompleted()
                }
            }
        })

        viewModel.showDatePickerDialog.observe(this, Observer {
            if (it == true) {
                datePickerDialog.show()
                viewModel.showDateDialogCompleted()
            }
        })

        viewModel.showTimePickerDialog.observe(this, Observer {
            if (it == true) {
                timePickerDialog.show()
                viewModel.showTimeDialogCompleted()
            }
        })

        setupPetProfile()
        setupListOfTags()

        calendar = viewModel.calendar
        setupDatePickerDialog()
        setupTimePickerDialog()

        return binding.root
    }

    private fun setupPetProfile() {
        var lastPosition: Int? = -1

        val listOfProfile = binding.listOfProfile
        avatarAdapter = PetAvatarAdapter(viewModel)

        listOfProfile.apply {
            PagerSnapHelper().attachToRecyclerView(this)

            this.adapter = avatarAdapter

            this.setOnScrollChangeListener { _, _, _, _, _ ->
                val newPosition = (listOfProfile.layoutManager as LinearLayoutManager)
                    .findFirstVisibleItemPosition()

                if (lastPosition != newPosition) {
                    viewModel.getProfilePosition(newPosition)
                    lastPosition = newPosition
                }
            }
        }

        // set indicator of recyclerView
        val recyclerIndicator = binding.indicatorProfilePetDialogTag
        recyclerIndicator.apply {
            this.attachToRecyclerView(listOfProfile)
        }
    }

    private fun setupListOfTags() {
        val listOfTags = binding.listOfTags
        val tagAdapter = TagListAdapter(viewModel, TagListAdapter.OnClickListener {

        })
        listOfTags.adapter = tagAdapter
    }

    private fun setupDatePickerDialog() {
        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                viewModel.updateDate()
            }
        datePickerDialog = DatePickerDialog(
            this.context!!,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun setupTimePickerDialog() {
        val timeListener =
            TimePickerDialog.OnTimeSetListener { _: TimePicker, hourOfDay: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                viewModel.updateTime()
            }
        timePickerDialog = TimePickerDialog(
            this.context,
            timeListener,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

}