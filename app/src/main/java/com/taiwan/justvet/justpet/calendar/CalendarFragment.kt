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
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.databinding.FragmentCalendarBinding
import com.taiwan.justvet.justpet.decorator.EventDecorator
import org.threeten.bp.LocalDate

class CalendarFragment : Fragment(), OnDateSelectedListener {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var userProfile: UserProfile
    private lateinit var localDate: LocalDate
    private lateinit var adapter: CalendarEventAdapter
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

        UserManager.userProfile.value?.let {
            userProfile = it
        }

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        localDate = viewModel.localDate

        setupCalendarView()
        setupEventRecyclerView()

        monthChangedListener()
        decorationObserver()

//        showThisMonthEvents()

        viewModel.navigateToDetail.observe(this, Observer {
            it?.let {
                findNavController().navigate(
                    CalendarFragmentDirections.actionCalendarFragmentToEventDetailFragment(
                        it
                    )
                )
                viewModel.navigateToDetailCompleted()
            }
        })

        viewModel.refreshEventData.observe(this, Observer {
            calendarView.selectedDate?.apply {
                if (it) {
                    calendarView.removeDecorators()
                    viewModel.default()
                    viewModel.getMonthEventsData(
                        userProfile,
                        this.year.toLong(),
                        this.month.toLong()
                    )
                    showSelectedDayEvents(this)
                    viewModel.refreshEventDataCompleted()
                }
            }
        })

        viewModel.showDeleteDialog.observe(this, Observer {
            it?.let {
                deleteDialog(it)
                viewModel.showDeleteDialogCompleted()
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        showThisMonthEvents()
    }

    private fun setupCalendarView() {
        calendarView = binding.calendarView
        calendarView.apply {
            this.setOnDateChangedListener(this@CalendarFragment)
            this.setSelectedDate(localDate)
            this.isDynamicHeightEnabled = true
        }
    }

    private fun setupEventRecyclerView() {
        val listOfEvents = binding.calendarListEvent
        adapter = CalendarEventAdapter(viewModel, CalendarEventAdapter.OnClickListener {
        })
        listOfEvents.adapter = adapter
    }

    private fun monthChangedListener() {
        calendarView.setOnMonthChangedListener { widget, date ->
            viewModel.getMonthEventsData(
                userProfile,
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
                showSelectedDayEvents(it)
            }
        })
    }

    private fun showSelectedDayEvents(calendarDay: CalendarDay) {
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

    fun deleteDialog(petEvent: PetEvent) {
        val builder = AlertDialog.Builder(this.context!!, R.style.Theme_AppCompat_Dialog)

        builder.setTitle("確定要刪除此筆資料嗎？")
        builder.setPositiveButton("確定") { _, _ ->
            viewModel.getEventTagsToDelete(petEvent)
        }.setNegativeButton("取消") { _, _ ->
        }.show()
    }

}