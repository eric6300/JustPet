package com.taiwan.justvet.justpet.breath

import android.os.Binder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.taiwan.justvet.justpet.ERIC
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.NavGraphDirections
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

        binding.rateTypeButtonGroup.setOnPositionChangedListener {
            viewModel.setTypeOfRate(it)
        }

//        viewModel.averageRate.observe(this, Observer {
//            it?.let {
//                if (it == 0L) {
//                    binding.buttonAddEvent.setBackgroundColor(JustPetApplication.appContext.getColor(R.color.colorPrimaryLight))
//                    Log.d(ERIC, "unClickable")
//                } else {
//                    binding.buttonAddEvent.setBackgroundColor(JustPetApplication.appContext.getColor(R.color.colorPrimary))
//                    Log.d(ERIC, "clickable")
//                }
//            }
//        })

        viewModel.navigateToTag.observe(this, Observer {
            it?.let {
                findNavController().navigate(NavGraphDirections.navigateToTagDialog(it))
                viewModel.navigateToTagCompleted()
            }
        })

        return binding.root
    }
}