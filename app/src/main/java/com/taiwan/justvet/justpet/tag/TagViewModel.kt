package com.taiwan.justvet.justpet.tag

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import kotlinx.android.synthetic.main.dialog_tag.view.*

class TagViewModel : ViewModel() {

    private val _navigateToEditEvent = MutableLiveData<Boolean>()
    val navigateToEditEvent: LiveData<Boolean>
        get() = _navigateToEditEvent

    private val _leaveTagDialog = MutableLiveData<Boolean>()
    val leaveTagDialog: LiveData<Boolean>
        get() = _leaveTagDialog

    val tagMap = mutableMapOf<Int, Drawable>()

    val appContext = JustPetApplication.appContext

    init {
        tagMap[1] = appContext.getDrawable(R.drawable.ic_food)!!
        tagMap[2] = appContext.getDrawable(R.drawable.ic_shower)!!
        tagMap[3] = appContext.getDrawable(R.drawable.ic_walking)!!
        tagMap[4] = appContext.getDrawable(R.drawable.ic_nail_trimming)!!
        tagMap[5] = appContext.getDrawable(R.drawable.ic_grooming)!!
        tagMap[5] = appContext.getDrawable(R.drawable.ic_weighting)!!
    }

    fun navigateToEditEvent() {
        _navigateToEditEvent.value = true
    }

    fun navigateToEditEventCompleted() {
        _navigateToEditEvent.value = null
    }

    fun leaveTagDialog() {
        _leaveTagDialog.value = true
    }

    fun getIconDrawable(index: Int): Drawable? {
        return tagMap[index]
    }

}