package com.taiwan.justvet.justpet.util

import android.content.Context
import com.taiwan.justvet.justpet.R

class Test(val context: Context) {

    fun getHelloMockitoString(): String {
        return context.getString(R.string.text_hello_mockito)
    }

}