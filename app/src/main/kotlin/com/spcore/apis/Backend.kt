package com.spcore.apis

import com.spcore.helpers.Auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal const val BACKEND_URL: String = "http://128.199.181.203:8080"

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

    fun performLogin(adminNo: String, password: String, firebaseRegistrationToken: String?) : Call<LoginResponse> {
        return backendCalls.performLogin(
                adminNo.let {
                    if (it.startsWith("p"))
                        it
                    else
                        "p$it"
                },
                password,
                firebaseRegistrationToken ?: "NULL, somehow")
    }

    fun initializeOrUpdateUserDetails(username: String, displayName: String?) : Call<StringResponse> {
        return backendCalls.updateUser("Bearer ${Auth.getJwtToken()}", username, displayName)
    }

    fun getLessons(startYYYYMM: String, endYYYYMM: String? = null) : Call<List<LessonResponse>> {
        return backendCalls.getLessons("Bearer ${Auth.getJwtToken()}", startYYYYMM, endYYYYMM)
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
