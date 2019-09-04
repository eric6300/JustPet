package com.taiwan.justvet.justpet.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.databinding.ItemHomePetEventBinding

class PetNotificationAdapter(val viewModel: HomeViewModel, val onClickListener: OnClickListener) :
    ListAdapter<PetEvent, PetNotificationAdapter.ViewHolder>(EventDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)
        holder.binding.layoutPetEvent.setOnClickListener {
            onClickListener.onClick(event)
        }
        holder.bind(event)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemHomePetEventBinding.inflate(
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

    class ViewHolder(val binding: ItemHomePetEventBinding, val viewModel: HomeViewModel) :
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

        fun bind(event: PetEvent) {
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            binding.event = event
            binding.executePendingBindings()
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<PetEvent>() {
        override fun areItemsTheSame(oldItem: PetEvent, newItem: PetEvent): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PetEvent, newItem: PetEvent): Boolean {
            return oldItem.timeStamp == newItem.timeStamp
        }
    }

    class OnClickListener(val clickListener: (event: PetEvent) -> Unit) {
        fun onClick(event: PetEvent) = clickListener(event)
    }
}