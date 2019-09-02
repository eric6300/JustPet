package com.taiwan.justvet.justpet.tag

import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.home.TAG
import com.taiwan.justvet.justpet.util.TagType
import com.taiwan.justvet.justpet.util.Util
import com.taiwan.justvet.justpet.util.Util.getDrawable
import com.taiwan.justvet.justpet.util.timestampToDateString
import com.taiwan.justvet.justpet.util.timestampToTimeString
import java.text.SimpleDateFormat
import java.util.*

class TagViewModel : ViewModel() {

    private val _navigateToEditEvent = MutableLiveData<Boolean>()
    val navigateToEditEvent: LiveData<Boolean>
        get() = _navigateToEditEvent

    private val _leaveTagDialog = MutableLiveData<Boolean>()
    val leaveTagDialog: LiveData<Boolean>
        get() = _leaveTagDialog

    private val _showDatePickerDialog = MutableLiveData<Boolean>()
    val showDatePickerDialog: LiveData<Boolean>
        get() = _showDatePickerDialog

    private val _showTimePickerDialog = MutableLiveData<Boolean>()
    val showTimePickerDialog: LiveData<Boolean>
        get() = _showTimePickerDialog

    private val _listOfTags = MutableLiveData<List<EventTag>>()
    val listOfTags: LiveData<List<EventTag>>
        get() = _listOfTags

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String>
        get() = _currentDate

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String>
        get() = _currentTime

    val selectedList = mutableListOf<EventTag>()

    private val listTagDiary = mutableListOf<EventTag>()
    private val listTagSyndrome = mutableListOf<EventTag>()
    private val listTagTreatment = mutableListOf<EventTag>()


    init {
        setupDiaryTagList()
        setupSyndromeTagList()
        setupTreatmentTagList()

        showDiaryTag()

        showCurrentTime()
    }

    private fun showCurrentTime() {
        val timeStamp = System.currentTimeMillis()
        timeStamp.let {
            _currentDate.value = it.timestampToDateString()
            _currentTime.value = it.timestampToTimeString()
        }
    }

    fun showDiaryTag() {
        _listOfTags.value = listTagDiary
    }

    fun showSyndromeTag() {
        _listOfTags.value = listTagSyndrome
    }

    fun showTreatmentTag() {
        _listOfTags.value = listTagTreatment
    }

    private fun setupDiaryTagList() {
        listTagDiary.let {
            it.add(EventTag(TagType.DIARY,0, "吃飯"))
            it.add(EventTag(TagType.DIARY,1, "洗澡"))
            it.add(EventTag(TagType.DIARY,2, "散步"))
            it.add(EventTag(TagType.DIARY,3, "剪指甲"))
            it.add(EventTag(TagType.DIARY,4, "剃毛"))
            it.add(EventTag(TagType.DIARY,5, "量體重"))
            it.add(EventTag(TagType.DIARY,6, "其他"))
        }
    }

    private fun setupSyndromeTagList() {
        listTagSyndrome.let {
            it.add(EventTag(TagType.SYNDROME,100, "嘔吐"))
            it.add(EventTag(TagType.SYNDROME,101, "下痢"))
            it.add(EventTag(TagType.SYNDROME,102, "咳嗽"))
            it.add(EventTag(TagType.SYNDROME,103, "打噴嚏"))
            it.add(EventTag(TagType.SYNDROME,104, "搔癢"))
            it.add(EventTag(TagType.SYNDROME,105, "癲癇"))
            it.add(EventTag(TagType.SYNDROME,106, "昏倒"))
            it.add(EventTag(TagType.SYNDROME,107, "排尿異常"))
            it.add(EventTag(TagType.SYNDROME,108, "其他"))
        }
    }

    private fun setupTreatmentTagList() {
        listTagTreatment.let {
            it.add(EventTag(TagType.TREATMENT,200, "除蚤"))
            it.add(EventTag(TagType.TREATMENT,201, "驅蟲"))
            it.add(EventTag(TagType.TREATMENT,202, "心絲蟲"))
            it.add(EventTag(TagType.TREATMENT,203, "皮下注射"))
            it.add(EventTag(TagType.TREATMENT,204, "血糖紀錄"))
            it.add(EventTag(TagType.TREATMENT,205, "口服藥"))
            it.add(EventTag(TagType.TREATMENT,206, "外用藥"))
            it.add(EventTag(TagType.TREATMENT,207, "眼藥/耳藥"))
            it.add(EventTag(TagType.TREATMENT,208, "其他"))
        }
    }

    fun showDatePickerDialog() {
        _showDatePickerDialog.value = true
    }

    fun showDateDialogCompleted() {
        _showDatePickerDialog.value = false
    }

    fun showTimePickerDialog() {
        _showTimePickerDialog.value = true
    }

    fun showTimeDialogCompleted() {
        _showTimePickerDialog.value = false
    }

    fun navigateToEditEvent() {
        _navigateToEditEvent.value = true
        for (tag in selectedList) {
            Log.d(TAG, "Tag index: ${tag.index} , Tag title: ${tag.title}")
        }
    }

    fun navigateToEditEventCompleted() {
        _navigateToEditEvent.value = null
    }

    fun leaveTagDialog() {
        _leaveTagDialog.value = true
    }

    fun getIconDrawable(index: Int): Drawable? {
        return when (index) {
            0 -> getDrawable(R.drawable.ic_food)
            1 -> getDrawable(R.drawable.ic_shower)
            2 -> getDrawable(R.drawable.ic_walking)
            3 -> getDrawable(R.drawable.ic_nail_trimming)
            4 -> getDrawable(R.drawable.ic_grooming)
            5 -> getDrawable(R.drawable.ic_weighting)
            else -> getDrawable(R.drawable.ic_food)
        }
    }

    fun updateDate(calendar: Calendar) {
        val time = SimpleDateFormat(JustPetApplication.appContext.getString(R.string.date_format), Locale.TAIWAN)
        _currentDate.value = time.format(calendar.time)
    }

    fun updateTime(calendar: Calendar) {
        val time = SimpleDateFormat(JustPetApplication.appContext.getString(R.string.time_format), Locale.TAIWAN)
        _currentTime.value = time.format(calendar.time)
    }

    fun addToSelectedList() {
        
    }

}