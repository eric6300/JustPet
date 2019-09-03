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
import androidx.lifecycle.Transformations
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
    lateinit var list: Array<CalendarDay>
    lateinit var calendarView: MaterialCalendarView
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
        setup()

        return binding.root
    }

    private fun setupCalendarView() {
        val instance = LocalDate.now()
//        val list = ArrayList<CalendarDay>()
//        list.add(CalendarDay.from(2019,9,27))
        calendarView = binding.calendarView
        calendarView.apply {
            this.setOnDateChangedListener(this@CalendarFragment)
            this.showOtherDates = MaterialCalendarView.SHOW_ALL
            this.setSelectedDate(instance)
//            this.addDecorator(EventDecorator(Color.RED, list))
        }
    }

    private fun setupEventRecyclerView() {
        val listOfEvents = binding.calendarListEvent
        val adapter = CalendarEvnetAdapter(viewModel, CalendarEvnetAdapter.OnClickListener{
        })
        listOfEvents.adapter = adapter
    }

    fun setup() {
        viewModel.listOfEvents.observe(this, Observer {
            for (event in it) {
                event.dateString?.let {
                    val splitString = it.split("/")
                    val year = splitString[0].toInt()
                    val month = splitString[1].toInt()
                    val dayOfMonth = splitString[2].toInt()
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
        Log.d(TAG, "selected timeStamp : $date")
    }
}