package com.spcore.services

import com.spcore.helpers.BACKEND_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object Backend {

    private var backendCalls: BackendInterface

    init {
        backendCalls = Retrofit.Builder()
                .baseUrl(BACKEND_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(BackendInterface::class.java)
    }

    fun performLogin(adminNo: String, password: String) : Call<LoginResponse> {
        return backendCalls.performLogin(adminNo, password)
    }

    /**
     * Performs an async login request to Budi's backend
     *
     * This returns a [onResponseBuilder] which consumes a callback, [onResponseBuilder.onResponse],
     * upon successful response,
     * which in turn returns [onFailureBuilder]
     */
    fun performLoginAsync(adminNo: String, password: String): onResponseBuilder<LoginResponse> {
        return onResponseBuilder(backendCalls.performLogin(adminNo, password))
    }

    /**
     * Method-chaining interface to provide a callback upon successful response
     *
     * @see onResponse
     */
    class onResponseBuilder<T>(val call: Call<T>) {
        /**
         * @param onRespCallback The callback upon successful response
         * @return Returns the [onFailureBuilder] method-chaining interface
         */
        fun onResponse(onRespCallback: (Call<T>, Response<T>) -> Unit): onFailureBuilder<T> {
            return onFailureBuilder(call, onRespCallback)
        }
    }

    /**
     * Method-chaining interface to provide callback upon a non-successful response
     *
     * @see onFailure
     */
    class onFailureBuilder<T>(
            val call:           Call<T>,
            val onRespCallback: (Call<T>, Response<T>) -> Unit) {
        /**
         * @param onFailureCallback The callback upon successful response
         * @return Returns the [onFailure] method-chaining interface
         */
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
