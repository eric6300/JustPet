package com.taiwan.justvet.justpet

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class CustomLayoutManager(context: Context?) : LinearLayoutManager(context) {
    private var isScrollEnabled = true

    fun setScrollEnabled(flag: Boolean) {
        this.isScrollEnabled = flag
    }

    override fun canScrollHorizontally(): Boolean {
        //Similarly you can customize "canScrollHorizontally()" for managing horizontal scroll
        return isScrollEnabled && super.canScrollHorizontally()
    }
}