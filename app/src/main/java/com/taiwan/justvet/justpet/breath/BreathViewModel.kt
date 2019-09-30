package com.taiwan.justvet.justpet.breath

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.ERIC
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.UserManager
import com.taiwan.justvet.justpet.data.PetEvent

class BreathViewModel : ViewModel() {

    private val _navigateToTag = MutableLiveData<PetEvent>()
    val navigateToTag: LiveData<PetEvent>
        get() = _navigateToTag

    var rateType = MutableLiveData<Int>()

    private var count: Int = 0
    private var startTime = 0L
    var endTime = 0L
    var lastEndTime = 0L
    var totalInterval = 0L
    var lastInterval = 0L
    val averageRate = MutableLiveData<Long>()

    val vibrator = JustPetApplication.appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    init {
        averageRate.value = 0L
        rateType.value = 0
    }

    fun setTypeOfRate(type: Int) {
        rateType.value = type
    }

    fun click() {
        count++
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
            endTime = System.currentTimeMillis()
            Log.d(ERIC, "startTime : $startTime")
        } else {
            lastEndTime = endTime
            endTime = System.currentTimeMillis()

            totalInterval = endTime.minus(startTime)
            averageRate.value = count * 60 * 1000 / totalInterval
        }

        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun reset() {
        count = 0
        startTime = 0L
        endTime = 0L
        lastEndTime = 0L
        totalInterval = 0L
        lastInterval = 0L
        averageRate.value = 0
    }

    fun saveRecord() {
        if (UserManager.userProfile.value?.pets?.size != 0) {
            if (averageRate.value != 0L) {
                rateType.value?.let {
                    if (it == 0) {
                        _navigateToTag.value = PetEvent(respiratoryRate = averageRate.value.toString())
                    } else if (it == 1) {
                        _navigateToTag.value = PetEvent(heartRate = averageRate.value.toString())
                    }
                }
            }
        } else {
            Toast.makeText(JustPetApplication.appContext, "目前沒有寵物資料，無法新增紀錄", Toast.LENGTH_LONG).show()
        }
    }

    fun navigateToTagCompleted() {
        _navigateToTag.value = null
    }


}