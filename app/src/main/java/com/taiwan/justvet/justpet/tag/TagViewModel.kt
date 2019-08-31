package com.taiwan.justvet.justpet.tag

import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taiwan.justvet.justpet.JustPetApplication
import com.taiwan.justvet.justpet.R
import com.taiwan.justvet.justpet.data.EventTag
import com.taiwan.justvet.justpet.util.timestampToDateString
import com.taiwan.justvet.justpet.util.timestampToTimeString

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

    private val _listOfTags = MutableLiveData<List<EventTag>>()
    val listOfTags: LiveData<List<EventTag>>
        get() = _listOfTags

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String>
        get() = _currentDate

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String>
        get() = _currentTime

    val tagMap = mutableMapOf<Int, Drawable>()

    private val appContext = JustPetApplication.appContext

    private val listTagDiary = mutableListOf<EventTag>()
    private val listTagSyndrome = mutableListOf<EventTag>()
    private val listTagTreatment = mutableListOf<EventTag>()


    init {
        setupDiaryTagList()
        setupSyndromeTagList()
        setupTreatmentTagList()

        showDiaryTagList()

        showCurrentTime()
    }

    private fun showCurrentTime() {
        val timeStamp = System.currentTimeMillis()
        timeStamp.let {
            _currentDate.value = it.timestampToDateString()
            _currentTime.value = it.timestampToTimeString()
        }
    }

    fun showDiaryTagList() {
        _listOfTags.value = listTagDiary
    }

    fun showSyndromeTagList() {
        _listOfTags.value = listTagSyndrome
    }

    fun showTreatmentTagList() {
        _listOfTags.value = listTagTreatment
    }

    private fun setupDiaryTagList() {
        listTagDiary.add(EventTag(1, "吃飯"))
        tagMap[1] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagDiary.add(EventTag(2, "洗澡"))
        tagMap[2] = appContext.getDrawable(R.drawable.ic_shower)!!

        listTagDiary.add(EventTag(3, "散步"))
        tagMap[3] = appContext.getDrawable(R.drawable.ic_walking)!!

        listTagDiary.add(EventTag(4, "剪指甲"))
        tagMap[4] = appContext.getDrawable(R.drawable.ic_nail_trimming)!!

        listTagDiary.add(EventTag(5, "剃毛"))
        tagMap[5] = appContext.getDrawable(R.drawable.ic_grooming)!!

        listTagDiary.add(EventTag(6, "量體重"))
        tagMap[6] = appContext.getDrawable(R.drawable.ic_weighting)!!

        listTagDiary.add(EventTag(7, "其他"))
    }

    private fun setupSyndromeTagList() {
        listTagSyndrome.add(EventTag(101, "嘔吐"))
        tagMap[101] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(102, "下痢"))
        tagMap[102] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(103, "咳嗽"))
        tagMap[103] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(104, "打噴嚏"))
        tagMap[104] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(105, "搔癢"))
        tagMap[105] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(106, "癲癇"))
        tagMap[106] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(107, "昏倒"))
        tagMap[107] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(108, "排尿異常"))
        tagMap[108] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagSyndrome.add(EventTag(109, "其他"))
        tagMap[109] = appContext.getDrawable(R.drawable.ic_food)!!
    }

    private fun setupTreatmentTagList() {
        listTagTreatment.add(EventTag(201, "除蚤"))
        tagMap[201] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(202, "驅蟲"))
        tagMap[202] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(203, "心絲蟲"))
        tagMap[203] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(204, "皮下注射"))
        tagMap[204] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(205, "血糖紀錄"))
        tagMap[205] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(206, "口服藥"))
        tagMap[206] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(207, "外用藥"))
        tagMap[207] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(208, "眼藥/耳藥"))
        tagMap[208] = appContext.getDrawable(R.drawable.ic_food)!!

        listTagTreatment.add(EventTag(209, "其他"))
        tagMap[209] = appContext.getDrawable(R.drawable.ic_food)!!
    }

    fun showDatePickerDialog() {
        _showDatePickerDialog.value = true
    }

    fun showDateDialogCompleted() {
        _showDatePickerDialog.value = false
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

    fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        _currentDate.value = "${year}年${month}月${dayOfMonth}日"
    }

}