package com.taiwan.justvet.justpet.breath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.taiwan.justvet.justpet.NavGraphDirections
import com.taiwan.justvet.justpet.R
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

        binding.rateTypeButtonGroup.setOnPositionChangedListener {
            viewModel.setTapRateType(it)
        }

        viewModel.navigateToTag.observe(this, Observer {
            it?.let {
                findNavController().navigate(NavGraphDirections.navigateToTagDialog(it))
                viewModel.navigateToTagCompleted()
            }
        })

        return binding.root
    }
}