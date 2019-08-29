package com.taiwan.justvet.justpet.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogEventBinding

class EventDialog : BottomSheetDialogFragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: DialogEventBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_event, container, false
        )

        return binding.root
    }
}