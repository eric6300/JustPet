package com.taiwan.justvet.justpet.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.UserManager.userProfile
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.databinding.FragmentCalendarBinding

class CalendarFragment : Fragment(), OnDateSelectedListener {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarView: MaterialCalendarView

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
        setupDecorationOfEvents()

        showEventsOfThisMonth()

        viewModel.navigateToEventFragment.observe(this, Observer {
            it?.let {
                findNavController().navigate(
                    CalendarFragmentDirections.actionCalendarFragmentToEventFragment(
                        it
                    )
                )
                viewModel.navigateToEventFragmentCompleted()
            }
        })

        viewModel.refreshEventData.observe(this, Observer {
            UserManager.userProfile.value?.let { userProfile ->
                calendarView.selectedDate?.apply {
                    if (it) {
                        calendarView.removeDecorators()
                        viewModel.default()
                        viewModel.getMonthEventsData(
                            userProfile,
                            this.year.toLong(),
                            this.month.toLong()
                        )
                        showEventsOfSelectedDay(this)
                        viewModel.refreshEventDataCompleted()
                    }
                }
            }
        })

        viewModel.showDeleteDialog.observe(this, Observer {
            it?.let {
                showDeleteEventDialog(it)
                viewModel.showDeleteDialogCompleted()
            }
        })

        return binding.root
    }

    private fun setupCalendarView() {
        calendarView = binding.calendarView
        calendarView.apply {
            this.setOnDateChangedListener(this@CalendarFragment)
            this.setSelectedDate(viewModel.localDate)
            this.isDynamicHeightEnabled = true
        }
    }

    private fun setupEventRecyclerView() {
        binding.calendarListEvent.adapter = CalendarEventAdapter(viewModel)
    }

    private fun setupMonthChangedListener() {
        calendarView.setOnMonthChangedListener { _, date ->
            UserManager.userProfile.value?.let {
                viewModel.getMonthEventsData(
                    it,
                    date.year.toLong(),
                    date.month.toLong()
                )
            }
        }
    }

    private fun setupDecorationOfEvents() {
        viewModel.decorateListOfEvents.observe(this, Observer {
            val list = ArrayList<CalendarDay>()
            for (event in it) {
                val year = event.year
                val month = event.month
                val dayOfMonth = event.dayOfMonth
                list.add(CalendarDay.from(year.toInt(), month.toInt(), dayOfMonth.toInt()))
            }
            calendarView.addDecorator(
                CalendarEventDecorator(
                    Color.RED,
                    list
                )
            )
        })
    }

    private fun showEventsOfThisMonth() {
        viewModel.monthEventsData.observe(this, Observer { list ->
            calendarView.selectedDate?.let {
                viewModel.getDecorationEvents(list)
                showEventsOfSelectedDay(it)
            }
        })
    }

    private fun showEventsOfSelectedDay(calendarDay: CalendarDay) {
        viewModel.dayEventsFilter(
            year = calendarDay.year.toLong(),
            month = calendarDay.month.toLong(),
            dayOfMonth = calendarDay.day.toLong(),
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

    private fun showDeleteEventDialog(petEvent: PetEvent) {
        val builder = AlertDialog.Builder(this.context!!, R.style.Theme_AppCompat_Dialog)

        builder.setTitle(getString(R.string.text_delete_event_message))
        builder.setPositiveButton(getString(R.string.text_confirm)) { _, _ ->
            viewModel.getEventTagsToDelete(petEvent)
        }.setNegativeButton(getString(R.string.text_cancel)) { _, _ ->
        }.show()
    }

}