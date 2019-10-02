package com.taiwan.justvet.justpet.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.util.LoadApiStatus
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogTagBinding
import kotlinx.android.synthetic.main.activity_main.*

class TagDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTagBinding
    private lateinit var viewModelFactory: TagViewModelFactory
    private lateinit var avatarAdapterTag: TagPetAvatarAdapter
    private val viewModel: TagViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(TagViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }

        viewModelFactory = TagViewModelFactory(TagDialogArgs.fromBundle(arguments!!).petEvent)

        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_tag, container, false
        )

        binding.let {
            it.lifecycleOwner = this
            it.viewModel = viewModel
        }

        setupPetProfile()
        setupListOfTags()
        setupSegmentedButtonGroup()

        viewModel.leaveTagDialog.observe(this, Observer {
            if (it) {
                dismiss()
                viewModel.leaveTagDialogCompleted()
            }
        })

        viewModel.loadStatus.observe(this, Observer {
            it?.let {
                binding.listOfTags.isLayoutFrozen = it == LoadApiStatus.LOADING
            }
        })

        viewModel.navigateToEvent.observe(this, Observer {
            it?.let {
                findNavController().navigate(
                    TagDialogDirections.actionTagDialogToEventDetailFragment(it)
                )
                viewModel.navigateToEventCompleted()
            }
        })

        viewModel.navigateToCalendar.observe(this, Observer {
            if (it) {
                dismiss()
                (activity as MainActivity).nav_bottom_view.selectedItemId = R.id.nav_bottom_calendar
                viewModel.navigateToCalendarCompleted()
            }
        })

        return binding.root
    }

    private fun setupPetProfile() {
        var lastPosition = -1

        val listOfProfile = binding.listOfProfile
        listOfProfile.apply {
            this.adapter = TagPetAvatarAdapter(viewModel)

            PagerSnapHelper().attachToRecyclerView(this)

            // set indicator of recyclerView
            binding.indicatorProfilePetDialogTag.attachToRecyclerView(this)

            this.setOnScrollChangeListener { _, _, _, _, _ ->
                val newPosition = (this.layoutManager as LinearLayoutManager)
                    .findFirstVisibleItemPosition()

                if (lastPosition != newPosition) {
                    viewModel.getProfileByPosition(newPosition)
                    lastPosition = newPosition
                }
            }
        }
    }

    private fun setupListOfTags() {
        binding.listOfTags.adapter = TagListAdapter(viewModel)
    }

    private fun setupSegmentedButtonGroup() {
        binding.tagCategoryButtonGroup.setOnPositionChangedListener {
            when (it) {
                TagType.DIARY.index -> {
                    viewModel.showDiaryTags()
                }
                TagType.SYNDROME.index -> {
                    viewModel.showSyndromeTags()
                }
                TagType.TREATMENT.index -> {
                    viewModel.showTreatmentTags()
                }
            }
        }
    }

}