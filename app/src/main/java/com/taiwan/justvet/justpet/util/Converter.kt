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

    @InverseMethod("doubleToString")
    fun stringToDouble(string: String): Double {
        return try {
            string.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    fun doubleToString(double: Double): String {
        return double.toString()
    }

}