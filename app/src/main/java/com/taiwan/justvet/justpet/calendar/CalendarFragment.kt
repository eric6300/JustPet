package com.taiwan.justvet.justpet.calendar

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.databinding.FragmentCalendarBinding
import com.taiwan.justvet.justpet.decorators.EventDecorator
import com.taiwan.justvet.justpet.home.TAG
import org.threeten.bp.LocalDate

class CalendarFragment : Fragment(), OnDateSelectedListener {

    lateinit var binding: FragmentCalendarBinding
    lateinit var calendarView: MaterialCalendarView
    lateinit var localDate: LocalDate
    private val viewModel: CalendarViewModel by lazy {
        ViewModelProviders.of(this).get(CalendarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_calendar, container, false
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupCalendarView()
        setupEventRecyclerView()

        setupMonthChangedListener()
        showThisMonthEvent(localDate)
        setupDecoration()


        return binding.root
    }

    private fun setupCalendarView() {
        localDate = LocalDate.now()
        calendarView = binding.calendarView
        calendarView.apply {
            this.setOnDateChangedListener(this@CalendarFragment)
            this.showOtherDates = MaterialCalendarView.SHOW_OTHER_MONTHS
            this.setSelectedDate(localDate)
        }
    }

    private fun setupEventRecyclerView() {
        val listOfEvents = binding.calendarListEvent
        val adapter = CalendarEvnetAdapter(viewModel, CalendarEvnetAdapter.OnClickListener {
        })
        listOfEvents.adapter = adapter
    }

    private fun setupMonthChangedListener() {
        calendarView.setOnMonthChangedListener { widget, date ->
            viewModel.eventFilter(date.year, date.month, null)
        }
    }

    private fun showThisMonthEvent(localDate: LocalDate) {
        viewModel.data.observe(this, Observer {
            val year = localDate.year
            val month = localDate.monthValue
            val dayOfMonth = localDate.dayOfMonth
            // filter for events of this month and set decoration
            viewModel.eventFilter(year = year, month = month, dayOfMonth = null)
            // filter for events of today and show in the recyclerView
            viewModel.eventFilter(year = year, month = month, dayOfMonth = dayOfMonth)
        })
    }

    private fun setupDecoration() {
        viewModel.decorateListOfEvents.observe(this, Observer {
            for (event in it) {
                event.dayOfMonth?.let {
                    val year = event.year
                    val month = event.month
                    val dayOfMonth = event.dayOfMonth
                    val list = ArrayList<CalendarDay>()
                    list.add(CalendarDay.from(year, month, dayOfMonth))
                    calendarView.addDecorator(EventDecorator(Color.RED, list))
                }
            }
        })
    }

    override fun onDateSelected(
        calendarView: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        viewModel.eventFilter(date.year, date.month, date.day)
    }
}