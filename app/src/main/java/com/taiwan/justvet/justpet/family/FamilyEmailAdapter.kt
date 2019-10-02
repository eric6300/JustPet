package com.taiwan.justvet.justpet.family

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.databinding.ItemFamilyEmailBinding

class FamilyEmailAdapter(
    val viewModel: FamilyViewModel
) : ListAdapter<String, FamilyEmailAdapter.ViewHolder>(DiffCallback()) {

    private lateinit var context: Context

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val email = getItem(position)
        holder.bind(email)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemFamilyEmailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), viewModel
        )
    }

    class ViewHolder(
        val binding: ItemFamilyEmailBinding,
        val viewModel: FamilyViewModel
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(email: String) {
            binding.email = email
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

}