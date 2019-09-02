package com.taiwan.justvet.justpet.tag.adapter

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.taiwan.justvet.justpet.tag.TagListAdapter

class MyLookup(private val recyclerView: RecyclerView)
    : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent)
            : ItemDetails<Long>? {

        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if(view != null) {
            return (recyclerView.getChildViewHolder(view) as TagListAdapter.ViewHolder)
                .getItemDetails()
        }
        return null

    }
}