package com.spcore.apis

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal const val BACKEND_URL: String = "http://188.166.217.80:8080"

object Backend {

    const val WRONG_SPICE_CRENDENTIALS = 4156
    const val LOCKED_OUT_BY_SP = 1237
    const val MISSING_JWT = 8456
    /**
     * When username taken
     */
    const val DUPLICATE_FOUND = 5640
    const val ALREADY_FRIENDS = 8974
    const val DATABASE_ERROR = 7456
    const val BAD_REQUEST = 7651
    const val NOT_FRIENDS = 4568
    const val CANNOT_ADD_SELF_AS_FRIEND = 8791
    const val OTHER_PARTY_ALREADY_SENT_REQ = 7777
    const val CAP_REACHED = 4786
    const val NOT_EVENT_HOST = 7980

    private var backendCalls: BackendInterface

    init {
        backendCalls = Retrofit.Builder()
                .baseUrl(BACKEND_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(BackendInterface::class.java)
    }

    fun performLogin(adminNo: String, password: String) : Call<LoginResponse> {
        return backendCalls.performLogin(
                adminNo.let {
                    if (it.startsWith("p"))
                        it
                    else
                        "p$it"
                },
                password)
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
