package com.taiwan.justvet.justpet.calendar

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.UserProfile
import com.taiwan.justvet.justpet.databinding.FragmentCalendarBinding
import com.taiwan.justvet.justpet.decorators.EventDecorator
import org.threeten.bp.LocalDate

class CalendarFragment : Fragment(), OnDateSelectedListener {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var userProfile: UserProfile
    private lateinit var localDate: LocalDate
    private lateinit var adapter: CalendarEventAdapter
    private lateinit var colorDrawableBackground: ColorDrawable
    private lateinit var swipeIcon: Drawable
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

        showThisMonthEvents()



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
        adapter = CalendarEventAdapter(viewModel, CalendarEventAdapter.OnClickListener {
        })
        listOfEvents.adapter = adapter
        enableSwipe(listOfEvents)
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

    private fun enableSwipe(recyclerView: RecyclerView) {
        val itemTouchHelperCallback =
            object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDirection: Int) {

                    val oldlist = mutableListOf<PetEvent>()
                    viewModel.dayEventsData.value?.let { oldlist.addAll(it) }

                    val petEvent = adapter.getPetEvent(viewHolder.adapterPosition)
                    petEvent?.let {
                        viewModel.getEventTagsToDelete(it)
                        Log.d(TAG, "deleted event Id: ${it.petId}")
                    }
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    if (dX > 0) {
                        swipeIcon = ContextCompat.getDrawable(
                            JustPetApplication.appContext,
                            R.drawable.ic_delete
                        )!!

                        colorDrawableBackground = ColorDrawable()
                        colorDrawableBackground.color =
                            JustPetApplication.appContext.getColor(R.color.colorSyndromeDark)

                        val itemView = viewHolder.itemView
                        val iconMarginVertical =
                            (viewHolder.itemView.height - swipeIcon.intrinsicHeight) / 2

                        colorDrawableBackground.setBounds(
                            itemView.left,
                            itemView.top,
                            dX.toInt(),
                            itemView.bottom
                        )

                        swipeIcon.setBounds(
                            itemView.left + iconMarginVertical,
                            itemView.top + iconMarginVertical,
                            itemView.left + iconMarginVertical + swipeIcon.intrinsicWidth,
                            itemView.bottom - iconMarginVertical
                        )

                        c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)

                    } else {
                        swipeIcon = ContextCompat.getDrawable(
                            JustPetApplication.appContext,
                            R.drawable.ic_edit_white
                        )!!

                        colorDrawableBackground = ColorDrawable()
                        colorDrawableBackground.color =
                            JustPetApplication.appContext.getColor(R.color.colorSyndromeDark)

                        val itemView = viewHolder.itemView
                        val iconMarginVertical =
                            (viewHolder.itemView.height - swipeIcon.intrinsicHeight) / 2

                        colorDrawableBackground.setBounds(
                            itemView.right + dX.toInt(),
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )

                        swipeIcon.setBounds(
                            itemView.right - iconMarginVertical - swipeIcon.intrinsicWidth,
                            itemView.top + iconMarginVertical,
                            itemView.right - iconMarginVertical,
                            itemView.bottom - iconMarginVertical
                        )

                        c.clipRect(
                            itemView.right + dX.toInt(),
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )
                    }

                    colorDrawableBackground.draw(c)
                    swipeIcon.draw(c)

                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

}