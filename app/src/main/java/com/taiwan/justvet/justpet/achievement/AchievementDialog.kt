package com.taiwan.justvet.justpet.achievement

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.DialogAchievementBinding

class AchievementDialog : AppCompatDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: DialogAchievementBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_achievement, container, false
        )

        return binding.root
    }
}