package com.taiwan.justvet.justpet.util

import androidx.databinding.InverseMethod

object Converter {
    @InverseMethod("longToString")
    fun stringToLong(string: String): Long {
        return try {
            string.toLong()
        } catch (e: Exception) {
            0
        }
    }

    fun longToString(long: Long): String {
        return long.toString()
    }

}