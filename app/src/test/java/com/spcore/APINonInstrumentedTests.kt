package com.spcore

import org.junit.Test

import org.junit.Assert.*
import com.spcore.spmobileapi.*

class APINonInstrumentedTests {
    @Test
    fun main() {
        println("Running API Non-instrumented tests")

        println(SPMobileAPI.getTimetableDay("271017", 1626498))
    }
}