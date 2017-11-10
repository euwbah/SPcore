package com.spcore.backend

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import kotlin.concurrent.thread


object Backend {

    private var backendCalls: BackendInterface

    init {
        backendCalls = Retrofit.Builder()
                .baseUrl("http://localhost:3000")
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(BackendInterface::class.java)
    }

    fun performLogin(adminNo: String, password: String) : Call<LoginResponse> {
        return backendCalls.performLogin(adminNo, password)
    }

    fun performLoginAsync(adminNo: String, password: String): onResponseBuilder<LoginResponse> {
        return onResponseBuilder(backendCalls.performLogin(adminNo, password))
    }

    class onResponseBuilder<T>(val call: Call<T>) {
        fun onResponse(onRespCallback: (Call<T>, Response<T>) -> Unit): onFailureBuilder<T> {
            return onFailureBuilder(call, onRespCallback)
        }
    }

    class onFailureBuilder<T>(
            val call:           Call<T>,
            val onRespCallback: (Call<T>, Response<T>) -> Unit) {
        fun onFailure(onFailureCallback: (Call<T>, Throwable?) -> Unit) {
            call.enqueue(object : Callback<T> {
                override fun onFailure(_call: Call<T>, _t: Throwable?) {
                    onFailureCallback(_call, _t)
                }

                override fun onResponse(_call: Call<T>, _response: Response<T>) {
                    onRespCallback(_call, _response)
                }

            })
        }
    }
}
