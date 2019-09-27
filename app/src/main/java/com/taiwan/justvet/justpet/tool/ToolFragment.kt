package com.taiwan.justvet.justpet.tool

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
import com.taiwan.justvet.justpet.breath.BreathViewModel
import com.taiwan.justvet.justpet.databinding.FragmentToolBinding

class ToolFragment : Fragment() {

    private lateinit var binding: FragmentToolBinding
    private val viewModel: ToolViewModel by lazy {
        ViewModelProviders.of(this).get(ToolViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_tool, container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.navigateToBreath.observe(this, Observer {
            it?.let {
                if (it) {
                    findNavController().navigate(NavGraphDirections.navigateToBreathFragment())
                    viewModel.navigateToBreathCompleted()
                }
            }
        })

        return binding.root
    }
}