package com.taiwan.justvet.justpet.calendar

import android.graphics.Color
import android.os.Bundle
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
        showThisMonthEvent()
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
        val adapter = CalendarEventAdapter(viewModel, CalendarEventAdapter.OnClickListener {
        })
        listOfEvents.adapter = adapter
    }

    private fun setupMonthChangedListener() {
        calendarView.setOnMonthChangedListener { widget, date ->
            viewModel.eventFilter(date.year.toLong(), date.month.toLong(), null)
        }
    }

    private fun showThisMonthEvent() {
        viewModel.eventsData.observe(this, Observer {
            val year = localDate.year
            val month = localDate.monthValue
            val dayOfMonth = localDate.dayOfMonth
            // filter for events of this month and set decoration
            viewModel.eventFilter(year = year.toLong(), month = month.toLong(), dayOfMonth = null)
            // filter for events of today and show in the recyclerView
            viewModel.eventFilter(year = year.toLong(), month = month.toLong(), dayOfMonth = dayOfMonth.toLong())
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
                    list.add(CalendarDay.from(year.toInt(), month.toInt(), dayOfMonth.toInt()))
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
        viewModel.eventFilter(date.year.toLong(), date.month.toLong(), date.day.toLong())
    }
}