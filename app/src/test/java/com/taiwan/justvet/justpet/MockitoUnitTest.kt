package com.taiwan.justvet.justpet

import android.content.Context
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

private const val FAKE_STRING = "HELLO WORLD"

@RunWith(MockitoJUnitRunner::class)
class MockitoUnitTest {

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun readStringFromContext_LocalizedString() {
        // Given a mocked Context injected into the object under test...
        `when`(mockContext.getString(R.string.text_hello_mockito))
            .thenReturn(FAKE_STRING)
        val myObjectUnderTest = com.taiwan.justvet.justpet.util.Test(mockContext)

        // ...when the string is returned from the object under test...
        val result: String = myObjectUnderTest.getHelloMockitoString()

        // ...then the result should be the expected one.
        MatcherAssert.assertThat(result, CoreMatchers.`is`(FAKE_STRING))
    }

}