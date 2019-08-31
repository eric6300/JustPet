package com.taiwan.justvet.justpet.tag

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.databinding.DialogTagBinding
import com.taiwan.justvet.justpet.home.TAG

class TagDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTagBinding
    private lateinit var datePickerDialog: DatePickerDialog
    private val viewModel: TagViewModel by lazy {
        ViewModelProviders.of(this).get(TagViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogTagBinding.inflate( inflater, container, false )

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

        setupListOfTags()
        setupDatePickerDialog()

        return binding.root
    }

    private fun setupListOfTags() {
        val tagAdapter = TagListAdapter(viewModel, TagListAdapter.OnClickListener {

        })
        binding.listOfTags.adapter = tagAdapter

    }

    fun setupDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val dateListener =
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                viewModel.updateDate(year, month.plus(1), dayOfMonth)
            }
        datePickerDialog = DatePickerDialog(
            this.context!!,
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

}