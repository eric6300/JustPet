package com.taiwan.justvet.justpet

import android.content.Context
import android.icu.util.Calendar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.common.base.CharMatcher.`is`
import com.google.firebase.FirebaseApp
import com.taiwan.justvet.justpet.chart.ChartViewModel
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.util.toDateFormat
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun testGetDateOfEvent() {
        val eventA = PetEvent(year = 2019, month = 2, dayOfMonth = 1, time = "03:22", timestamp = 1551369600L)
        val resultA = eventA.getDateOfEvent()

        val eventB = PetEvent(year = 2018, month = 8, dayOfMonth = 27, time = "05:22", timestamp = 1537996920L)
        val resultB = eventB.getDateOfEvent()

        assertEquals(Date(1551369600L * 1000), resultA)
        assertEquals(Date(1537996920L * 1000), resultB)
    }
}
