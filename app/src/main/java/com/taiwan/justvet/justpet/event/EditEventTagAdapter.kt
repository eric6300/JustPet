package com.taiwan.justvet.justpet.event

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.databinding.ItemChipTagBinding
import com.taiwan.justvet.justpet.databinding.ItemIconTagBinding
import com.taiwan.justvet.justpet.tag.TagListAdapter
import com.taiwan.justvet.justpet.tag.TagViewModel

class EditEventTagAdapter(val viewModel: EditEventViewModel, val onClickListener: OnClickListener) :
    ListAdapter<EventTag, EditEventTagAdapter.ViewHolder>(TagDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eventTag = getItem(position)
        holder.bind(eventTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return EditEventTagAdapter.ViewHolder(
            ItemChipTagBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel
        )
    }

    override fun onViewAttachedToWindow(holder: EditEventTagAdapter.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: EditEventTagAdapter.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    class ViewHolder(val binding: ItemChipTagBinding, val viewModel: EditEventViewModel) :
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
            binding.viewModel = viewModel
            binding.eventTag = eventTag
            binding.executePendingBindings()
        }

    }

    class TagDiffCallback : DiffUtil.ItemCallback<EventTag>() {
        override fun areItemsTheSame(oldItem: EventTag, newItem: EventTag): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: EventTag, newItem: EventTag): Boolean {
            return oldItem.index == newItem.index
        }
    }

    class OnClickListener(val clickListener: (eventTag: EventTag) -> Unit) {
        fun onClick(eventTag: EventTag) = clickListener(eventTag)
    }
}