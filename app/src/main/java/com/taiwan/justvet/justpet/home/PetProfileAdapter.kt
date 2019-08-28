package com.taiwan.justvet.justpet.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.Converter
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.databinding.ItemCardPetProfileBinding

class PetProfileAdapter(val viewModel: HomeViewModel, val onClickListener: OnClickListener) :
    ListAdapter<PetProfile,
            PetProfileAdapter.ViewHolder>(ProfileDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petProfile = getItem(position)
        holder.binding.layoutPet.setOnClickListener {
            onClickListener.onClick(petProfile)
        }
        holder.bind(petProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemCardPetProfileBinding.inflate(
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

    class ViewHolder(val binding: ItemCardPetProfileBinding, val viewModel: HomeViewModel) :
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
            return oldItem.name == newItem.name
        }
    }

    class OnClickListener(val clickListener: (profile: PetProfile) -> Unit) {
        fun onClick(profile: PetProfile) = clickListener(profile)
    }
}