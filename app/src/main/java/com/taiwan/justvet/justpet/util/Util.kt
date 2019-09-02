package com.taiwan.justvet.justpet.util

import android.graphics.drawable.Drawable
import com.taiwan.justvet.justpet.JustPetApplication

object Util {

    fun getString(resourceId: Int): String {
        return JustPetApplication.appContext.getString(resourceId)
    }

    fun getDrawable(resourceId: Int): Drawable? {
        return JustPetApplication.appContext.getDrawable(resourceId)
    }
}