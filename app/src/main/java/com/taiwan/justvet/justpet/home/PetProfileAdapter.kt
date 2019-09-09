package com.taiwan.justvet.justpet.home

import android.content.Context
import android.graphics.Outline
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.util.Converter
import com.taiwan.justvet.justpet.data.PetProfile
import com.taiwan.justvet.justpet.databinding.ItemHomePetProfileBinding

class PetProfileAdapter(val viewModel: HomeViewModel, val onClickListener: OnClickListener) :
    ListAdapter<PetProfile, PetProfileAdapter.ViewHolder>(ProfileDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petProfile = getItem(position)

        val petImage = holder.binding.imagePet
        petImage.clipToOutline = true
        petImage.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.setRoundRect(0, 0, view.width, view.height, 24F)
            }
        }

        val filter = holder.binding.filterImage
        filter.clipToOutline = true
        filter.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.setRoundRect(0, 0, view.width, view.height, 24F)
            }
        }

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
            return (oldItem.name == newItem.name) && (oldItem.owner == newItem.owner) && (oldItem.birthDay == newItem.birthDay)
        }
    }

    class OnClickListener(val clickListener: (profile: PetProfile) -> Unit) {
        fun onClick(profile: PetProfile) = clickListener(profile)
    }
}