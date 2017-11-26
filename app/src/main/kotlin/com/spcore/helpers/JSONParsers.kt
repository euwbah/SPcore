package com.spcore.helpers

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi

val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()!!
val backendErrorAdapter = moshi.adapter(BackendErrorResult::class.java)!!

class BackendErrorResult(val message: String, val code: Int)