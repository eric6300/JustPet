package com.taiwan.justvet.justpet.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
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
    private lateinit var deleteIcon: Drawable

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

        binding.buttonAchievement.setOnClickListener {
            findNavController().navigate(R.id.navigate_to_achievementDialog)
        }

        return binding.root
    }

    private fun setupPetProfile() {
        profileAdapter = PetProfileAdapter(viewModel, PetProfileAdapter.OnClickListener {

        })

        val layoutManager = CustomGLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        val listProfilePet = binding.listProfilePet
        listProfilePet.apply {
            this.layoutManager = layoutManager
            this.adapter = profileAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        viewModel.isModified.observe(this, Observer {
            if (it == true) {
                (listProfilePet.layoutManager as CustomGLayoutManager).setScrollEnabled(flag = false)
                profileAdapter.notifyDataSetChanged()
            } else {
                (listProfilePet.layoutManager as CustomGLayoutManager).setScrollEnabled(flag = true)
                profileAdapter.notifyDataSetChanged()
            }
        })
    }

    fun setupPetEvent() {
        eventAdapter = PetEventAdapter(viewModel, PetEventAdapter.OnClickListener {

        })

        val listEventPet = binding.listEventPet
        listEventPet.apply {
            this.adapter = eventAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }
    }

    fun mockupData() {
        val list = mutableListOf<PetProfile>()
        list.add(PetProfile("Meimei", 0, 0, "900123256344452"))
        list.add(PetProfile("多多", 1, 0, "900001255677536"))
        list.add(PetProfile("Lucky", 1, 1, ""))
        profileAdapter.submitList(list)

        val eventList = mutableListOf<PetEvent>()
        eventList.add(PetEvent(type = 0, tag = 0, note = "hello"))
        eventList.add(PetEvent(type = 1, tag = 1, note = "hey"))
        eventList.add(PetEvent(type = 2, tag = 2, note = "yo"))
        eventAdapter.submitList(eventList)

    }
}

class CustomGLayoutManager(context: Context?) : LinearLayoutManager(context) {
    private var isScrollEnabled = true

    fun setScrollEnabled(flag: Boolean) {
        this.isScrollEnabled = flag
    }

    override fun canScrollHorizontally(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollHorizontally()
    }
}