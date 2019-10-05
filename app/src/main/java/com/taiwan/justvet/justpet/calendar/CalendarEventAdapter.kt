package com.taiwan.justvet.justpet.calendar

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
import com.taiwan.justvet.justpet.databinding.ItemCalendarEventBinding

class CalendarEventAdapter(val viewModel: CalendarViewModel) :
    ListAdapter<PetEvent, CalendarEventAdapter.ViewHolder>(CalendarEventAdapter.EventDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val petEvent = getItem(position)
        holder.itemView.setOnClickListener {
            viewModel.navigateToEventFragment(petEvent)
        }

        holder.binding.icDelete.setOnClickListener {
            viewModel.showDeleteDialog(petEvent)
        }

        holder.binding.listOfTags.let {
            val adapter = CalendarTagListAdapter(viewModel)
            it.adapter = adapter
            adapter.submitList(petEvent.eventTags)
        }

        holder.bind(petEvent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return CalendarEventAdapter.ViewHolder(
            ItemCalendarEventBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel
        )
    }

    override fun onViewAttachedToWindow(holder: CalendarEventAdapter.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: CalendarEventAdapter.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    class ViewHolder(val binding: ItemCalendarEventBinding, val viewModel: CalendarViewModel) :
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

        fun bind(petEvent: PetEvent) {
            binding.lifecycleOwner = this
            binding.viewModel = viewModel
            binding.event = petEvent
            binding.executePendingBindings()
        }

    }

    class EventDiffCallback : DiffUtil.ItemCallback<PetEvent>() {
        override fun areItemsTheSame(oldItem: PetEvent, newItem: PetEvent): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PetEvent, newItem: PetEvent): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }
    }
}