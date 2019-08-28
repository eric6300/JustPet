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
            viewModel.modifyPetProfile()
        })

        viewModel.isModified.observe(this, Observer {
            Log.d(TAG, "$it")
        })

        val listProfilePet = binding.listProfilePet
        listProfilePet.adapter = petProfileAdapter
        PagerSnapHelper().attachToRecyclerView(listProfilePet)

        binding.buttonAchievement.setOnClickListener {
            findNavController().navigate(R.id.navigate_to_achievementDialog)
        }

        val list = mutableListOf<PetProfile>()
        list.add(PetProfile("Meimei"))
        list.add(PetProfile("多多"))
        list.add(PetProfile("Lucky"))

        petProfileAdapter.submitList(list)

        return binding.root
    }
}