package com.taiwan.justvet.justpet.tag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogTagBinding

class TagDialog : BottomSheetDialogFragment() {

    private val viewModel: TagViewModel by lazy {
        ViewModelProviders.of(this).get(TagViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: DialogTagBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_tag, container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.navigateToEditEvent.observe(this, Observer {
            if (it == true) {
                findNavController().navigate(TagDialogDirections.actionTagDialogToEventDetailFragment())
                viewModel.navigateToEditEventCompleted()
            }
        })

        return binding.root
    }
}