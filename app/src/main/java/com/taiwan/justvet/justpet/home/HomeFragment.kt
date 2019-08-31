package com.taiwan.justvet.justpet.home

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.NavGraphDirections
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.databinding.FragmentHomeBinding

const val TAG = "testEric"
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var profileAdapter: PetProfileAdapter
    private lateinit var eventAdapter: PetEventAdapter
    private lateinit var colorDrawableBackground: ColorDrawable
    private lateinit var swipeIcon: Drawable
    private lateinit var eventList: MutableList<PetEvent>

    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setupPetProfile()
        setupPetEvent()
        mockupData()

        viewModel.birthdayChange.observe(this, Observer {
            if (it) {
                profileAdapter.notifyDataSetChanged()
                viewModel.birthdayChangeCompleted()
            }
        })

        viewModel.navigateToAchievement.observe(this, Observer {
            if (it == true) {
                findNavController().navigate(NavGraphDirections.navigateToAchievementDialog())
                viewModel.navigateToAchievementCompleted()
            }
        })



        return binding.root
    }

    private fun setupPetProfile() {
        profileAdapter = PetProfileAdapter(viewModel, PetProfileAdapter.OnClickListener {
        })

        val layoutManager = CustomLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        val listProfilePet = binding.listProfilePet
        listProfilePet.apply {
            this.layoutManager = layoutManager
            this.adapter = profileAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        // disable scroll function when editing pet profile
        viewModel.isModified.observe(this, Observer {
            if (it == true) {
                (listProfilePet.layoutManager as CustomLayoutManager).setScrollEnabled(flag = false)
                profileAdapter.notifyDataSetChanged()
            } else {
                (listProfilePet.layoutManager as CustomLayoutManager).setScrollEnabled(flag = true)
                profileAdapter.notifyDataSetChanged()
            }
        })
    }

    private fun setupPetEvent() {
        eventAdapter = PetEventAdapter(viewModel, PetEventAdapter.OnClickListener {

        })

        val listEventPet = binding.listEventPet
        listEventPet.apply {
            this.adapter = eventAdapter
            PagerSnapHelper().attachToRecyclerView(this)
            enableSwipe(this)
        }
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
                    if (swipeDirection == ItemTouchHelper.LEFT) {
                        Log.d(TAG, "完成並詢問要不要設定推播")
                        // TODO 推播詢問及設定
                    }
                    eventList.removeAt(viewHolder.adapterPosition)
                    eventAdapter.submitList(eventList)
                    eventAdapter.notifyDataSetChanged()
                    // TODO Connect to Firebase
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
                            JustPetApplication.appContext.getColor(R.color.colorDiaryDark)

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
//                    c.save()
//                    if (dX > 0)
//                        c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
//                    else
//                        c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
//                    c.restore()

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

    fun mockupData() {
        val list = mutableListOf<PetProfile>()
        list.add(PetProfile("Meimei", 0, 0, "900123256344452"))
        list.add(PetProfile("多多", 1, 0, "900001255677536"))
        list.add(PetProfile("Lucky", 1, 1, ""))
        profileAdapter.submitList(list)

        eventList = mutableListOf<PetEvent>()
        eventList.add(PetEvent(date = "1", type = 0, tag = 0, note = "年度健康檢查還剩 15 天"))
        eventList.add(PetEvent(date = "2", type = 1, tag = 1, note = "除蚤滴劑要記得點喔!"))
        eventList.add(PetEvent(date = "3", type = 2, tag = 2, note = "這四週內已經吐了三次喔!"))
        eventAdapter.submitList(eventList)

    }
}