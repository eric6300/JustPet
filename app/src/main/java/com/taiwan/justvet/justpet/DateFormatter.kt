package com.taiwan.justvet.justpet

import android.content.Context
import com.jjoe64.graphview.DefaultLabelFormatter
import com.taiwan.justvet.justpet.util.Util
import com.taiwan.justvet.justpet.util.Util.getString
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class to use date objects as x-values.
 * This will use your own Date Format or by default
 * the Android default date format to convert
 * the x-values (that has to be millis from
 * 01-01-1970) into a formatted date string.
 *
 * See the DateAsXAxis example in the GraphView-Demos project
 * to see a working example.
 *
 * @author jjoe64
 */
class DateFormatter : DefaultLabelFormatter {
    /**
     * the date format that will convert
     * the unix timestamp to string
     */
    private val mDateFormat: DateFormat

    /**
     * calendar to avoid creating new date objects
     */
    private val mCalendar: Calendar

    /**
     * create the formatter with the Android default date format to convert
     * the x-values.
     *
     * @param context the application context
     */
    constructor(context: Context) {
        mDateFormat = SimpleDateFormat(
            getString(R.string.chart_year_month_format),
            Locale.TAIWAN
        )
        mCalendar = Calendar.getInstance()
    }


    /**
     * create the formatter with your own custom
     * date format to convert the x-values.
     *
     * @param context the application context
     * @param dateFormat custom date format
     */
    constructor(context: Context, dateFormat: DateFormat) {
        mDateFormat = dateFormat
        mCalendar = Calendar.getInstance()
    }

    /**
     * formats the x-values as date string.
     *
     * @param value raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return value converted to string
     */
    override fun formatLabel(value: Double, isValueX: Boolean): String {
        if (isValueX) {
            // format as date
            mCalendar.timeInMillis = value.toLong()
            return mDateFormat.format(mCalendar.timeInMillis)
        } else {
            return super.formatLabel(value, isValueX)
        }
    }
}
