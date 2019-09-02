package com.taiwan.justvet.justpet.calendar

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentCalendarBinding
import com.taiwan.justvet.justpet.decorators.EventDecorator
import com.taiwan.justvet.justpet.home.TAG
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

class CalendarFragment : Fragment(), OnDateSelectedListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentCalendarBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_calendar, container, false
        )

        val instance = LocalDate.now()

        val list = ArrayList<CalendarDay>()
        list.add(CalendarDay.from(2019,9,27))

        val calendarView = binding.calendarView
        calendarView.apply {
            this.setOnDateChangedListener(this@CalendarFragment)
            this.showOtherDates = MaterialCalendarView.SHOW_ALL
            this.setSelectedDate(instance)
            this.addDecorator(EventDecorator(Color.RED, list))
        }


        return binding.root
    }

    override fun onDateSelected(
        calendarView: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        Log.d(TAG, "selected date : $date")
    }
}