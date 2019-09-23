package com.taiwan.justvet.justpet.util

import com.taiwan.justvet.justpet.R

enum class CurrentFragmentType(val value: String) {
    HOME(Util.getString(R.string.home)),
    CALENDAR(Util.getString(R.string.calendar)),
    CHART(Util.getString(R.string.chart)),
    TOOL(Util.getString(R.string.tool)),
    EVENT(Util.getString(R.string.event)),
}