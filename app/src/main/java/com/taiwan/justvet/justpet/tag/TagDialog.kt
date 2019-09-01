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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.databinding.DialogTagBinding

class TagDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTagBinding
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog
    private lateinit var calendar: Calendar
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
                findNavController().navigate(TagDialogDirections.actionTagDialogToEventDetailFragment())
                viewModel.navigateToEditEventCompleted()
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

        setupListOfTags()

        calendar = Calendar.getInstance()
        setupDatePickerDialog()
        setupTimePickerDialog()

        return binding.root
    }

    private fun setupListOfTags() {
        val tagAdapter = TagListAdapter(viewModel, TagListAdapter.OnClickListener {

        })
        binding.listOfTags.adapter = tagAdapter

    }

    private fun setupDatePickerDialog() {
        val dateListener =
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                viewModel.updateDate(calendar)
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
            TimePickerDialog.OnTimeSetListener { view: TimePicker, hourOfDay: Int, minute: Int ->
                calendar.set(hourOfDay, minute)
                viewModel.updateTime(calendar)
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