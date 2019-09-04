package com.taiwan.justvet.justpet.tag

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.databinding.ItemIconTagBinding
import com.taiwan.justvet.justpet.databinding.ItemTagDialogPetBinding
import com.taiwan.justvet.justpet.home.PetProfileAdapter

class PetAvatarAdapter(val viewModel: TagViewModel, val onClickListener: OnClickListener) :
    ListAdapter<PetProfile, PetAvatarAdapter.ViewHolder>(PetProfileAdapter.ProfileDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = getItem(position)
        holder.bind(profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return PetAvatarAdapter.ViewHolder(
            ItemTagDialogPetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel
        )
    }

    override fun onViewAttachedToWindow(holder: PetAvatarAdapter.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: PetAvatarAdapter.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    class ViewHolder(val binding: ItemTagDialogPetBinding, val viewModel: TagViewModel) :
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

        fun bind(profile: PetProfile) {
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            binding.profile = profile
            binding.executePendingBindings()
        }

    }

    class OnClickListener(val clickListener: (profile: PetProfile) -> Unit) {
        fun onClick(profile: PetProfile) = clickListener(profile)
    }
}