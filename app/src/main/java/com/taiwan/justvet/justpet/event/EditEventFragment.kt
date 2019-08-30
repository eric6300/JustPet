package com.taiwan.justvet.justpet.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentEditEventBinding
import com.taiwan.justvet.justpet.tag.TagViewModel
import kotlinx.android.synthetic.main.activity_main.*


class EditEventFragment : Fragment() {

    private val viewModel: EditEventViewModel by lazy {
        ViewModelProviders.of(this).get(EditEventViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentEditEventBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.navigateToCalendar.observe(this, Observer {
            if (it == true) {
                (activity as MainActivity).nav_bottom_view.selectedItemId = R.id.nav_bottom_calendar
                viewModel.navigateToCalendarCompleted()
            }
        })

        binding.scrollView.viewTreeObserver.addOnScrollChangedListener {
            binding.seekBarAppetite.correctOffsetWhenContainerOnScrolling()
            binding.seekBarSpirit.correctOffsetWhenContainerOnScrolling()
        }

        return binding.root
    }
}
