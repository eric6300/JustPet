package com.taiwan.justvet.justpet.util

import android.graphics.drawable.Drawable
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R

object Util {

    fun getString(resourceId: Int): String {
        return JustPetApplication.appContext.getString(resourceId)
    }

    fun getString(resourceId: Int, string: String?): String {
        return JustPetApplication.appContext.getString(resourceId, string)
    }

    fun getDrawable(resourceId: Int): Drawable? {
        return JustPetApplication.appContext.getDrawable(resourceId)
    }

    fun getIconDrawable(index: Long): Drawable? {
        return when (index) {
            0L -> getDrawable(R.drawable.ic_food)
            1L -> getDrawable(R.drawable.ic_shower)
            2L -> getDrawable(R.drawable.ic_walking)
            3L -> getDrawable(R.drawable.ic_nail_trimming)
            4L -> getDrawable(R.drawable.ic_grooming)
            5L -> getDrawable(R.drawable.ic_weighting)
            200L -> getDrawable(R.drawable.ic_tick)
            201L -> getDrawable(R.drawable.ic_medicine)
            202L -> getDrawable(R.drawable.ic_heart)
            203L -> getDrawable(R.drawable.ic_synrige)
            204L -> getDrawable(R.drawable.ic_blood_test)
            205L -> getDrawable(R.drawable.ic_medicine)
            206L -> getDrawable(R.drawable.ic_ointment)
            207L -> getDrawable(R.drawable.ic_eye_drops)
            208L -> getDrawable(R.drawable.ic_synrige)
            209L -> getDrawable(R.drawable.ic_blood_test)
            else -> getDrawable(R.drawable.ic_others)
        }
    }
}