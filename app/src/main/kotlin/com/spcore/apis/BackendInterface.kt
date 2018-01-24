package com.spcore.apis

import retrofit2.Call
import retrofit2.http.*

interface BackendInterface {
    @FormUrlEncoded
    @POST("/api/dev/auth/login")
    @Headers("Accept: application/json")
    fun performLogin(
            @Field("adminNo") adminNo: String,
            @Field("password") password: String) : Call<LoginResponse>
}