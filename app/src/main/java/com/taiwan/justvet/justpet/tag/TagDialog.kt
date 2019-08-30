package com.taiwan.justvet.justpet.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.databinding.DialogTagBinding

class TagDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTagBinding

    private val viewModel: TagViewModel by lazy {
        ViewModelProviders.of(this).get(TagViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DialogTagBinding.inflate( inflater, container, false )

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

        setupTag()

        return binding.root
    }

    private fun setupTag() {
        val tagAdapter = TagListAdapter(viewModel, TagListAdapter.OnClickListener {

        })
        binding.listTags.adapter = tagAdapter
        mockData(tagAdapter)
    }

    fun mockData(tagAdapter: TagListAdapter) {
        val listTag = mutableListOf<EventTag>()

        listTag.add(EventTag(1, "吃飯"))
        listTag.add(EventTag(2, "洗澡"))
        listTag.add(EventTag(3, "散步"))
        listTag.add(EventTag(4, "剪指甲"))
        listTag.add(EventTag(5, "剃毛"))
        listTag.add(EventTag(6, "量體重"))

        tagAdapter.submitList(listTag)
    }
}