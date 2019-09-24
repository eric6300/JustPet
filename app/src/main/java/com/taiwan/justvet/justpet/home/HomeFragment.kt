package com.taiwan.justvet.justpet.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest
import com.taiwan.justvet.justpet.*
import com.taiwan.justvet.justpet.data.Invite
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var profileAdapter: PetProfileAdapter
    private lateinit var notificationAdapter: EventNotificationAdapter
    private lateinit var colorDrawableBackground: ColorDrawable
    private lateinit var swipeIcon: Drawable

    private val quickPermissionsOption = QuickPermissionsOptions(
        handleRationale = false,
        permanentDeniedMethod = { permissionsPermanentlyDenied(it) }
    )

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
        setupEventNotification()

        UserManager.refreshUserProfileCompleted.observe(this, Observer {
            if (it == true) {
                UserManager.userProfile.value?.let { userProfile ->
                    viewModel.getPetProfileData(userProfile)
                    viewModel.checkInvite()
                    UserManager.refreshUserProfileCompleted()
                }
            }
        })

        viewModel.birthdayChange.observe(this, Observer {
            if (it == true) {
                profileAdapter.notifyDataSetChanged()
                viewModel.birthdayChangeCompleted()
            }
        })

        viewModel.petSpecies.observe(this, Observer {
            it?.let {
                profileAdapter.notifyDataSetChanged()
            }
        })

        viewModel.petGender.observe(this, Observer {
            it?.let {
                profileAdapter.notifyDataSetChanged()
            }
        })

        viewModel.navigateToAchievement.observe(this, Observer {
            it?.let {
                findNavController().navigate(NavGraphDirections.navigateToFamilyDialog(it))
                viewModel.navigateToAchievementCompleted()
            }
        })

        viewModel.petList.observe(this, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    profileAdapter.submitList(it)
                    profileAdapter.notifyDataSetChanged()
                } else {
                    binding.cardAddNewPet.visibility = View.VISIBLE
                }
            }
        })

        viewModel.selectedPet.observe(this, Observer {
            it?.let {
                viewModel.showPetProfile(it)
                viewModel.getPetEvents(it)
                profileAdapter.notifyDataSetChanged()
            }
        })

        viewModel.eventsList.observe(this, Observer {
            it?.let {
                viewModel.filterForNotification(it)
            }
        })

        viewModel.startGallery.observe(this, Observer {
            if (it) {
                startGallery()
                viewModel.startGalleryCompleted()
            }
        })

        viewModel.inviteList.observe(this, Observer { inviteList ->
            inviteList?.let {
                val invite = it[0]

                val dialog = this.context?.let { context ->
                    AlertDialog.Builder(context)
                        .setTitle("邀請通知")
                        .setMessage("${invite.inviterName} ( ${invite.inviterEmail} ) \n邀請你一起紀錄 ${invite.petName} 的生活")
                        .setPositiveButton("接受") { _, _ ->

                            viewModel.confirmInvite(invite)

                            val newList = mutableListOf<Invite>()
                            newList.addAll(inviteList)
                            newList.removeAt(0)
                            viewModel.showInvite(newList)
                        }
                        .setNeutralButton("再想想") { _, _ ->
                            val newList = mutableListOf<Invite>()
                            newList.addAll(inviteList)
                            newList.removeAt(0)
                            viewModel.showInvite(newList)
                        }.create()

                }

                dialog?.show()
            }
        })

        viewModel.navigateToNewPet.observe(this, Observer {
            if (it) {
                findNavController().navigate(NavGraphDirections.navigateToPetProfileDialogFragment())
                viewModel.navigateToNewPetCompleted()
            }
        })

        return binding.root
    }

    private fun setupPetProfile() {
        // set adapter
        profileAdapter = PetProfileAdapter(viewModel, PetProfileAdapter.OnClickListener {
        })

        // set layoutManager
        val layoutManager = CustomLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        // set recyclerView with adapter and layoutManager
        val listProfilePet = binding.homeListProfilePet
        listProfilePet.apply {
            this.layoutManager = layoutManager
            this.adapter = profileAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }

        // set indicator of recyclerView
        val recyclerIndicator = binding.indicatorProfilePet
        recyclerIndicator.apply {
            this.attachToRecyclerView(listProfilePet)
        }

        // monitor position after scrolling
        var lastPosition = -1
        listProfilePet.setOnScrollChangeListener { _, _, _, _, _ ->
            val newPosition =
                (listProfilePet.layoutManager as CustomLayoutManager).findFirstVisibleItemPosition()

            if (lastPosition != newPosition) {
                viewModel.selectPetProfile(newPosition)
                lastPosition = newPosition
                Log.d(ERIC, "position changed")
            }
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

    private fun setupEventNotification() {
        notificationAdapter =
            EventNotificationAdapter(viewModel, EventNotificationAdapter.OnClickListener {
                if (it.type != 2) {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEventDetailFragment(
                        PetEvent(
                            petProfile = it.petProfile,
                            petName = it.petProfile.name,
                            petId = it.petProfile.profileId,
                            petSpecies = it.petProfile.species,
                            eventTags = it.eventTags,
                            eventTagsIndex = it.eventTagsIndex
                        )
                    ))
                }
            })

        val listEventNotification = binding.homeListEventNotification
        listEventNotification.apply {
            this.adapter = notificationAdapter
            PagerSnapHelper().attachToRecyclerView(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PHOTO_FROM_GALLERY -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let { data ->
                            data.data?.let {
                                viewModel.petImage.value = it.toString()
                                profileAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.wtf("getImageResult", resultCode.toString())
                    }
                }
            }
        }
    }

    private fun startGallery() = runWithPermissions(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        options = quickPermissionsOption
    ) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PHOTO_FROM_GALLERY)
    }

    private fun permissionsPermanentlyDenied(req: QuickPermissionsRequest) {
        // this will be called when some/all permissions required by the method are permanently
        // denied. Handle it your way.
        this.context?.let {
            AlertDialog.Builder(it)
                .setMessage("開啟「設定」，點選「權限」，並開啟「儲存」")
                .setPositiveButton("開啟「設定」") { _, _ -> req.openAppSettings() }
                .setNegativeButton("取消") { _, _ -> req.cancel() }
                .setCancelable(true)
                .show()
        }
    }
}