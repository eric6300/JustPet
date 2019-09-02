package com.taiwan.justvet.justpet.util

import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.util.Util.getString

enum class TagType(val value: String) {
    DIARY(getString(R.string.text_diary)),
    SYNDROME(getString(R.string.text_syndrome)),
    TREATMENT(getString(R.string.text_treatment))
}