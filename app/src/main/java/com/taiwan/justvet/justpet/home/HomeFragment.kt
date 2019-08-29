package com.taiwan.justvet.justpet.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.databinding.FragmentHomeBinding


const val TAG = "testEric"

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
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

        val petProfileAdapter = PetProfileAdapter(viewModel, PetProfileAdapter.OnClickListener {

        })

        val layoutManager = CustomGLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        val listProfilePet = binding.listProfilePet
        listProfilePet.apply {
            this.layoutManager = layoutManager
            this.adapter = petProfileAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        viewModel.isModified.observe(this, Observer {
            if (it == true) {
                (listProfilePet.layoutManager as CustomGLayoutManager).setScrollEnabled(flag = false)
                petProfileAdapter.notifyDataSetChanged()
            } else {
                (listProfilePet.layoutManager as CustomGLayoutManager).setScrollEnabled(flag = true)
                petProfileAdapter.notifyDataSetChanged()
            }
        })

        val list = mutableListOf<PetProfile>()
        list.add(PetProfile("Meimei", 0, 0, "900123256344452"))
        list.add(PetProfile("多多", 1, 0, "900001255677536"))
        list.add(PetProfile("Lucky", 1, 1, ""))

        petProfileAdapter.submitList(list)

        binding.buttonAchievement.setOnClickListener {
            findNavController().navigate(R.id.navigate_to_achievementDialog)
        }

        return binding.root
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