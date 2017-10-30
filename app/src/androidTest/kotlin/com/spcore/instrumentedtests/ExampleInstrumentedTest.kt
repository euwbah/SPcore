package com.spcore.instrumentedtests

import android.content.Context.MODE_PRIVATE
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.spcore.R
import com.spcore.spmobileapi.SPMobileAPI

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()

        assertEquals("com.spcore", appContext.packageName)

        val sharedPrefs =
                appContext.getSharedPreferences(appContext.getString(R.string.cookie_storage_shared_preference_id), MODE_PRIVATE)

        SPMobileAPI.inititialize(sharedPrefs)


    }
    @Test
    fun testRandomStuff() {
        println("hi")
    }
}
