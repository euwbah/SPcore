package com.spcore.exceptions

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.wtf

class WTF(message: String = "") : Exception(message), AnkoLogger {
    init {
        wtf(message)
    }
}