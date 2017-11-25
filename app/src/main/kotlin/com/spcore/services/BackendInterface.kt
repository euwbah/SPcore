package com.spcore.services

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface BackendInterface {
    @FormUrlEncoded
    @POST("/api/test/auth/login")
    fun performLogin(
            @Field("adminNo") adminNo: String,
            @Field("password") password: String) : Call<LoginResponse>
}