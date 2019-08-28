package com.taiwan.justvet.justpet.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.PagerSnapHelper
import com.taiwan.justvet.justpet.Converter
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

        val petProfileAdapter = PetProfileAdapter(viewModel, PetProfileAdapter.OnClickListener{

        })

        val listProfilePet = binding.listProfilePet
        listProfilePet.adapter = petProfileAdapter
        PagerSnapHelper().attachToRecyclerView(listProfilePet)

        binding.buttonAchievement.setOnClickListener {
            findNavController().navigate(R.id.navigate_to_achievementDialog)
        }

        val list = mutableListOf<PetProfile>()
        list.add(PetProfile("Meimei", 0, 0,"900123256344452"))
        list.add(PetProfile("多多", 1, 0,"900001255677536"))
        list.add(PetProfile("Lucky", 1, 1,"900135996333216"))

        petProfileAdapter.submitList(list)

        return binding.root
    }
}