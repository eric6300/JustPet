package com.taiwan.justvet.justpet.breath

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.UserManager
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.HEART_RATE_TYPE
import com.taiwan.justvet.justpet.event.EventViewModel.Companion.RESPIRATORY_RATE_TYPE
import com.taiwan.justvet.justpet.util.Util

class BreathViewModel : ViewModel() {
    private val _navigateToTagDialog = MutableLiveData<PetEvent>()
    val navigateToTagDialog: LiveData<PetEvent>
        get() = _navigateToTagDialog

    private val _averageTapRate = MutableLiveData<Long>()
    val averageTapRate: LiveData<Long>
        get() = _averageTapRate

    private var tapCount = 0L
    private var startTime = 0L
    private var endTime = 0L
    private var lastEndTime = 0L
    private var totalInterval = 0L
    private var lastTapInterval = 0L

    private val vibrator =
        JustPetApplication.appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    var tapRateType = MutableLiveData<Int>()

    init {
        resetTapRateCount()
        tapRateType.value = RESPIRATORY_RATE_TYPE
    }

    fun setTapRateType(type: Int) {
        tapRateType.value = type
    }

    fun countTapRate() {
        tapCount++

        vibration()

        when (startTime) {
            0L -> {
                startTime = System.currentTimeMillis()
                endTime = System.currentTimeMillis()
            }
            else -> {
                lastEndTime = endTime
                endTime = System.currentTimeMillis()

                displayAverageTapRate()
            }
        }
    }

    private fun vibration() {
        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun displayAverageTapRate() {
        _averageTapRate.value = tapCount * 60 * 1000 / endTime.minus(startTime)
    }

    fun resetTapRateCount() {
        tapCount = 0L
        startTime = 0L
        endTime = 0L
        lastEndTime = 0L
        totalInterval = 0L
        lastTapInterval = 0L
        _averageTapRate.value = 0L
    }

    fun navigateToTagDialog() {
        when (UserManager.userProfile.value?.pets?.size) {
            0 -> {
                Toast.makeText(
                    JustPetApplication.appContext,
                    Util.getString(R.string.text_no_pet_no_new_event),
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                averageTapRate.value?.let { tapRate ->
                    when (tapRate) {
                        0L -> {
                            Toast.makeText(
                                JustPetApplication.appContext,
                                Util.getString(R.string.text_rate_empty_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            tapRateType.value?.let {
                                when (it) {
                                    RESPIRATORY_RATE_TYPE -> {
                                        _navigateToTagDialog.value =
                                            PetEvent(respiratoryRate = tapRate)
                                    }
                                    HEART_RATE_TYPE -> {
                                        _navigateToTagDialog.value =
                                            PetEvent(heartRate = tapRate)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun navigateToTagCompleted() {
        _navigateToTagDialog.value = null
    }
}