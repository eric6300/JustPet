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
            FOOD -> getDrawable(R.drawable.ic_food)
            SHOWER -> getDrawable(R.drawable.ic_shower)
            WALKING -> getDrawable(R.drawable.ic_walking)
            NAIL_TRIMMING -> getDrawable(R.drawable.ic_nail_trimming)
            HAIR_TRIMMING -> getDrawable(R.drawable.ic_hair_trimming)
            WEIGHTING -> getDrawable(R.drawable.ic_weighting)
            ECTO_PREVENT -> getDrawable(R.drawable.ic_tick)
            ENDO_PREVENT -> getDrawable(R.drawable.ic_medicine)
            HEART_WORM -> getDrawable(R.drawable.ic_heart)
            SC_INJECTION -> getDrawable(R.drawable.ic_synrige)
            GLUCOSE_TEST -> getDrawable(R.drawable.ic_blood_test)
            ORAL_MEDICINE -> getDrawable(R.drawable.ic_medicine)
            OINTMENT -> getDrawable(R.drawable.ic_ointment)
            DROPS -> getDrawable(R.drawable.ic_eye_drops)
            VACCINE -> getDrawable(R.drawable.ic_synrige)
            BLOOD_TEST -> getDrawable(R.drawable.ic_blood_test)
            else -> getDrawable(R.drawable.ic_others)
        }
    }

    const val FOOD          = 0L
    const val SHOWER        = 1L
    const val WALKING       = 2L
    const val NAIL_TRIMMING = 3L
    const val HAIR_TRIMMING = 4L
    const val WEIGHTING     = 5L
    const val ECTO_PREVENT  = 200L
    const val ENDO_PREVENT  = 201L
    const val HEART_WORM    = 202L
    const val SC_INJECTION  = 203L
    const val GLUCOSE_TEST  = 204L
    const val ORAL_MEDICINE = 205L
    const val OINTMENT      = 206L
    const val DROPS         = 207L
    const val VACCINE       = 208L
    const val BLOOD_TEST    = 209L
}