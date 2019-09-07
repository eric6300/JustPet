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
import com.taiwan.justvet.justpet.MainActivity
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.TAG
import com.taiwan.justvet.justpet.UserManager
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
        localDate = viewModel.localDate

        setupCalendarView()
        setupEventRecyclerView()

        monthChangedListener()
        decorationObserver()

        showThisMonthEvents()

        viewModel.refreshEventData.observe(this, Observer {
            calendarView.selectedDate?.apply {
                if (it) {
                    calendarView.removeDecorators()
                    viewModel.default()
                    viewModel.getMonthEventsData(
                        viewModel.mockUser(),
                        this.year.toLong(),
                        this.month.toLong()
                    )
                    viewModel.refreshEventDataCompleted()
                }
            }
        })

        return binding.root
    }

    private fun setupCalendarView() {
        calendarView = binding.calendarView
        calendarView.apply {
            this.setOnDateChangedListener(this@CalendarFragment)
            this.showOtherDates = MaterialCalendarView.SHOW_DEFAULTS
            this.setSelectedDate(localDate)
            this.isDynamicHeightEnabled = true
        }
    }

    private fun setupEventRecyclerView() {
        val listOfEvents = binding.calendarListEvent
        val adapter = CalendarEventAdapter(viewModel, CalendarEventAdapter.OnClickListener {
        })
        listOfEvents.adapter = adapter
    }

    private fun monthChangedListener() {
        calendarView.setOnMonthChangedListener { widget, date ->
            viewModel.getMonthEventsData(
                viewModel.mockUser(),
                date.year.toLong(),
                date.month.toLong()
            )
        }
    }

    private fun decorationObserver() {
        viewModel.decorateListOfEvents.observe(this, Observer {
            val list = ArrayList<CalendarDay>()
            for (event in it) {
                val year = event.year
                val month = event.month
                val dayOfMonth = event.dayOfMonth
                list.add(CalendarDay.from(year.toInt(), month.toInt(), dayOfMonth.toInt()))
            }
            calendarView.addDecorator(EventDecorator(Color.RED, list))
        })
    }

    private fun showThisMonthEvents() {
        viewModel.monthEventsData.observe(this, Observer { list ->
            calendarView.selectedDate?.let {
                viewModel.getDecorationEvents(list)

                if (it.year == localDate.year && it.month == localDate.monthValue
                    && it.day == localDate.dayOfMonth
                ) {
                    showTodayEvents()
                }
            }
        })
    }

    private fun showTodayEvents() {
        viewModel.dayEventsFilter(
            year = localDate.year.toLong(),
            month = localDate.monthValue.toLong(),
            dayOfMonth = localDate.dayOfMonth.toLong(),
            events = viewModel.monthEventsData.value
        )
    }

    override fun onDateSelected(
        calendarView: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        viewModel.dayEventsFilter(
            date.year.toLong(),
            date.month.toLong(),
            date.day.toLong(),
            viewModel.monthEventsData.value
        )
    }

}