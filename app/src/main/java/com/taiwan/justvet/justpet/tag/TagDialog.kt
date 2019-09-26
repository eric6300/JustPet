package com.taiwan.justvet.justpet.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogTagBinding
import kotlinx.android.synthetic.main.activity_main.*

class TagDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTagBinding
    //    private lateinit var datePickerDialog: DatePickerDialog
//    private lateinit var timePickerDialog: TimePickerDialog
//    private lateinit var calendar: Calendar
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
            if (it) {
                findNavController().popBackStack()
            }
        })

        viewModel.navigateToEditEvent.observe(this, Observer {
            if (it) {
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

        viewModel.navigateToCalendar.observe(this, Observer {
            if (it == true) {
                dismiss()
                (activity as MainActivity).nav_bottom_view.selectedItemId = R.id.nav_bottom_calendar
                viewModel.navigateToCalendarCompleted()
            }
        })

        setupPetProfile()
        setupListOfTags()

        setupSegmentedButtonGroup()

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
                    viewModel.getProfileByPosition(newPosition)
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

    private fun setupSegmentedButtonGroup() {
        binding.tagCategoryButtonGroup.setOnPositionChangedListener {
            when (it) {
                0 -> {
                    viewModel.showDiaryTag()
                }
                1 -> {
                    viewModel.showSyndromeTag()
                }
                2 -> {
                    viewModel.showTreatmentTag()
                }
            }
        }
    }

}