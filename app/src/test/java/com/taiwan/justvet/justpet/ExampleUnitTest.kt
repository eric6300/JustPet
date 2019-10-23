package com.taiwan.justvet.justpet

import com.taiwan.justvet.justpet.data.PetEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import com.taiwan.justvet.justpet.util.Util

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun testGetDateOfEvent() {
        val eventA = PetEvent(
            year = 2019,
            month = 2,
            dayOfMonth = 1,
            time = "03:22",
            timestamp = 1551369600L
        )
        val resultA = eventA.getDateOfEvent()

        val eventB = PetEvent(
            year = 2018,
            month = 8,
            dayOfMonth = 27,
            time = "05:22",
            timestamp = 1537996920L
        )
        val resultB = eventB.getDateOfEvent()

        assertEquals(Date(1551369600L * 1000), resultA)
        assertEquals(Date(1537996920L * 1000), resultB)
    }


    @Test
    fun testCalculateSyndromeDataSize() {
        val threeMonthsAgoTimestamp = 1564588800L  //  2019-08-01 0:0:0
        val sixMonthsAgoTimestamp = 1556640000L    //  2019-05-01 0:0:0
        val oneYearAgoTimestamp = 1541001600L      //  2018-11-01 0:0:0

        val eventList = mutableListOf<PetEvent>().apply {
            add(PetEvent(timestamp = 1571083454))  //  2019-10-15 4:4:14
            add(PetEvent(timestamp = 1562721421))  //  2019-07-10 9:17:1
            add(PetEvent(timestamp = 1554804266))  //  2019-04-09 18:4:26
        }

        val resultList = Util.calculateSyndromeDataSize(
            eventList,
            threeMonthsAgoTimestamp,
            sixMonthsAgoTimestamp,
            oneYearAgoTimestamp
        )

        assertEquals(1, resultList[0])
        assertEquals(2, resultList[1])
        assertEquals(3, resultList[2])

    }
}
