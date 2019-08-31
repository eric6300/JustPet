package com.taiwan.justvet.justpet.tag

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.databinding.ItemHomePetEventBinding
import com.taiwan.justvet.justpet.databinding.ItemIconTagBinding
import com.taiwan.justvet.justpet.home.HomeViewModel
import com.taiwan.justvet.justpet.home.PetEventAdapter
import com.taiwan.justvet.justpet.home.TAG

class TagListAdapter(val viewModel: TagViewModel, val onClickListener: OnClickListener) :
    ListAdapter<EventTag, TagListAdapter.ViewHolder>(TagDiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val eventTag = getItem(position)
        holder.itemView.setOnClickListener {
            Log.d(TAG, eventTag.title)
        }
        holder.bind(eventTag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return TagListAdapter.ViewHolder(
            ItemIconTagBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel
        )
    }

    override fun onViewAttachedToWindow(holder: TagListAdapter.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: TagListAdapter.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    class ViewHolder(val binding: ItemIconTagBinding, val viewModel: TagViewModel) :
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