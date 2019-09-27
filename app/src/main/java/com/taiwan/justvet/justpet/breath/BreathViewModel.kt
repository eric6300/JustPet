package com.taiwan.justvet.justpet.breath

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.ERIC

class BreathViewModel : ViewModel() {

    private var count: Int = 0
    private var startTime = 0L
    var endTime = 0L
    var lastEndTime = 0L
    var totalInterval = 0L
    var lastInterval = 0L
    val instantRate = MutableLiveData<Long>()
    val averageRate = MutableLiveData<Long>()

    fun click() {
        count++
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
            endTime = System.currentTimeMillis()
            Log.d(ERIC, "startTime : $startTime")
        } else {
            lastEndTime = endTime
            endTime = System.currentTimeMillis()

            lastInterval = endTime.minus(lastEndTime)
            instantRate.value = 60 * 1000 / lastInterval

            totalInterval = endTime.minus(startTime)
            averageRate.value = count * 60 * 1000 / totalInterval

            Log.d(ERIC, "==========================")
            Log.d(ERIC, "instantRate : ${instantRate.value}")
            Log.d(ERIC, "averageRate : ${averageRate.value}")
        }
    }

    fun reset() {
        count = 0
        startTime = 0L
        endTime = 0L
        lastEndTime = 0L
        totalInterval = 0L
        lastInterval = 0L
        instantRate.value = 0
        averageRate.value = 0
    }


}