package com.taiwan.justvet.justpet

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testAddNewPetFlow() {
        Thread.sleep(3000)
        //  open option menu and click add new pet button
        openActionBarOverflowOrOptionsMenu(getInstrumentation().targetContext)
        onView(withText(R.string.text_new_pet_profile)).perform(click())
        Thread.sleep(1500)

        //  choose dog
        onView(withId(R.id.icon_dog)).perform(click())

        //  choose male
        onView(withId(R.id.icon_male)).perform(click())

        //  type pet name
        onView(withId(R.id.edit_text_name)).perform(typeText("Kitty"), closeSoftKeyboard())

        //  choose pet birthday
        onView(withId(R.id.edit_text_birthday)).check(matches(isEnabled()))
        onView(withId(R.id.edit_text_birthday)).perform(click())
        onView(withClassName(Matchers.equalTo(DatePicker::class.java.name))).perform(
            PickerActions.setDate(
                2019,
                2,
                23
            )
        )
        onView(withText("確定")).perform(click())

        onView(withId(R.id.edit_text_name)).check(matches(withText("Kitty")))
        onView(withId(R.id.edit_text_birthday)).check(matches(withText("2019/2/23")))

        //  click confirm
        onView(withId(R.id.button_confirm)).perform(click())
        Thread.sleep(5000)
    }

}