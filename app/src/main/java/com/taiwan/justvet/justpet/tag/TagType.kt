package com.taiwan.justvet.justpet.tag

import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.util.Util.getString

enum class TagType(val value: String, val index: Int) {
    DIARY(getString(R.string.text_diary), 0),
    SYNDROME(getString(R.string.text_syndrome), 1),
    TREATMENT(getString(R.string.text_treatment), 2)
}