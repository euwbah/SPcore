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

    /**
     * @param authorization Should be "Bearer<space><jwt token>"
     */
    @FormUrlEncoded
    @PUT("/api/dev/auth/updateUser")
    @Headers("Accept: */*")
    fun updateUser(
            @Header("Authorization") authorization: String,
            @Field("username") username: String,
            @Field("displayName") displayName: String?) : Call<StringResponse>
}