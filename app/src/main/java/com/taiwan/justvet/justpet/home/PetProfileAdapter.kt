package com.taiwan.justvet.justpet.home

import android.content.Context
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.util.Converter
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.databinding.ItemHomePetProfileBinding

class PetProfileAdapter(val viewModel: HomeViewModel, val onClickListener: OnClickListener) :
    ListAdapter<PetProfile, PetProfileAdapter.ViewHolder>(ProfileDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petProfile = getItem(position)

        val petImage = holder.binding.imagePet
        val filter = holder.binding.filterImage

        petImage.clipToOutline = true
        petImage.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                viewModel.isModified.value.let {
                    if (it == true) {
                        outline?.setRoundRect(0, 0, view.width, view.height + 36, 36F)
                    } else {
                        outline?.setRoundRect(0, 0, view.width, view.height, 36F)
                    }
                }
            }
        }


        filter.clipToOutline = true
        filter.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                viewModel.isModified.value.let {
                    if (it == true) {
                        outline?.setRoundRect(0, 0, view.width, view.height + 36, 36F)
                    } else {
                        outline?.setRoundRect(0, 0, view.width, view.height, 36F)
                    }
                }
            }
        }

        viewModel.petSpecies.value?.let {
            if (it == 0L) {
                holder.binding.iconCat.alpha = 1.0F
                holder.binding.iconDog.alpha = 0.2F
            } else if (it == 1L) {
                holder.binding.iconCat.alpha = 0.2F
                holder.binding.iconDog.alpha = 1.0F
            }
        }

        viewModel.petGender.value?.let {
            if (it == 0L) {
                holder.binding.iconMale.alpha = 0.2F
                holder.binding.iconFemale.alpha = 1.0F
            } else if (it == 1L) {
                holder.binding.iconMale.alpha = 1.0F
                holder.binding.iconFemale.alpha = 0.2F
            }
        }

        viewModel.errorMessage.observe(holder, Observer {
            it?.let {
                holder.binding.editTextName.error = it
            }
        })

        holder.bind(petProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemHomePetProfileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel
        )
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    class ViewHolder(val binding: ItemHomePetProfileBinding, val viewModel: HomeViewModel) :
        RecyclerView.ViewHolder(binding.root), LifecycleOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }

        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }

        fun onAttach() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }

        fun onDetach() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }

        fun bind(petProfile: PetProfile) {
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            binding.converter = Converter
            binding.petProfile = petProfile
            binding.executePendingBindings()
        }
    }

    class ProfileDiffCallback : DiffUtil.ItemCallback<PetProfile>() {
        override fun areItemsTheSame(oldItem: PetProfile, newItem: PetProfile): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PetProfile, newItem: PetProfile): Boolean {
            return (oldItem.name == newItem.name) && (oldItem.owner == newItem.owner) && (oldItem.birthday == newItem.birthday) && (oldItem.idNumber == newItem.idNumber)
        }
    }

    class OnClickListener(val clickListener: (profile: PetProfile) -> Unit) {
        fun onClick(profile: PetProfile) = clickListener(profile)
    }
}