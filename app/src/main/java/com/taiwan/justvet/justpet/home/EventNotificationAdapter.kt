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
import com.taiwan.justvet.justpet.data.EventNotification
import com.taiwan.justvet.justpet.databinding.ItemHomeNotificationBinding

class EventNotificationAdapter(val viewModel: HomeViewModel, val onClickListener: OnClickListener) :
    ListAdapter<EventNotification, EventNotificationAdapter.ViewHolder>(EventDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position)
        holder.binding.layoutPetEvent.setOnClickListener {
            onClickListener.onClick(notification)
        }
        holder.bind(notification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemHomeNotificationBinding.inflate(
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

    class ViewHolder(val binding: ItemHomeNotificationBinding, val viewModel: HomeViewModel) :
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

        fun bind(notification: EventNotification) {
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            binding.notification = notification
            binding.executePendingBindings()
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<EventNotification>() {
        override fun areItemsTheSame(oldItem: EventNotification, newItem: EventNotification): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: EventNotification, newItem: EventNotification): Boolean {
            return oldItem.timeStamp == newItem.timeStamp
        }
    }

    class OnClickListener(val clickListener: (notification: EventNotification) -> Unit) {
        fun onClick(notification: EventNotification) = clickListener(notification)
    }
}