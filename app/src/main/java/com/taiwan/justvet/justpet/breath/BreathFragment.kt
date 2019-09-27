package com.taiwan.justvet.justpet.breath

import android.os.Binder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.calendar.CalendarViewModel
import com.taiwan.justvet.justpet.databinding.FragmentBreathBinding
import com.taiwan.justvet.justpet.util.Converter

class BreathFragment : Fragment() {

    private lateinit var binding: FragmentBreathBinding
    private val viewModel: BreathViewModel by lazy {
        ViewModelProviders.of(this).get(BreathViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_breath, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.converter = Converter


        return binding.root
    }
}