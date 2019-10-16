package com.taiwan.justvet.justpet

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.taiwan.justvet.justpet.chart.ChartFragment
import com.taiwan.justvet.justpet.chart.ChartViewModel
import com.taiwan.justvet.justpet.chart.ChartViewModelFactory
import com.taiwan.justvet.justpet.data.JustPetRepository
import com.taiwan.justvet.justpet.data.PetEvent
import com.taiwan.justvet.justpet.event.EventViewModel
import com.taiwan.justvet.justpet.event.EventViewModelFactory
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MockitoUnitTest {

//  TODO: DI to resolve mocking viewModel issue

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var fragment: ChartFragment


    private lateinit var repository: JustPetRepository
    private lateinit var viewModel: ChartViewModel
    private val observer: Observer<Int> = mock()

    @Before
    fun before() {
        fragment = ChartFragment()
        repository = JustPetRepository
        viewModel = ViewModelProviders.of(fragment, ChartViewModelFactory(JustPetRepository))
            .get(ChartViewModel::class.java)
        viewModel.syndrome3MonthsDataSize.observeForever(observer)
    }

    @Test
    fun testCalculateSyndromeDataSize() {
        val mockList = listOf(
            PetEvent(timestamp = 1569878160L),
            PetEvent(timestamp = 1552341900L),
            PetEvent(timestamp = 1541444941L)
        )

        val expectedSize = 1

        val captor = ArgumentCaptor.forClass(Int::class.java)

        viewModel.calculateSyndromeDataSize(mockList)

        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertEquals(expectedSize, value)
        }
    }
}