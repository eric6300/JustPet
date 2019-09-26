package com.taiwan.justvet.justpet.calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.databinding.ItemChipTagBinding
import com.taiwan.justvet.justpet.tag.TagListAdapter

class CalendarTagListAdapter(val viewModel: CalendarViewModel) :
    ListAdapter<EventTag, CalendarTagListAdapter.ViewHolder>(TagListAdapter.TagDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eventTag = getItem(position)
        holder.bind(eventTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return CalendarTagListAdapter.ViewHolder(
            ItemChipTagBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel
        )
    }

    override fun onViewAttachedToWindow(holder: CalendarTagListAdapter.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: CalendarTagListAdapter.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    class ViewHolder(val binding: ItemChipTagBinding, val viewModel: CalendarViewModel) :
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

        fun bind(eventTag: EventTag) {
            binding.lifecycleOwner = this
            binding.calendarViewModel = viewModel
            binding.eventTag = eventTag
            binding.executePendingBindings()
        }

    }
}

