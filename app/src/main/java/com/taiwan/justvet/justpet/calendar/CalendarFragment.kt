package com.taiwan.justvet.justpet.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentCalendarBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_calendar, container, false
        )

        return binding.root
    }
}