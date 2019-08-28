package com.taiwan.justvet.justpet.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentToolBinding

class ToolFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentToolBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_tool, container, false
        )

        return binding.root
    }
}