package com.spcore.models

interface Nowable {
    /**
     * Determines whether the event is currently on-going
     */
    fun isNow() : Boolean
}