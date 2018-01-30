package com.spcore.apis

import retrofit2.Call
import retrofit2.http.*

interface BackendInterface {
    @FormUrlEncoded
    @POST("/api/dev/auth/login")
    @Headers("Accept: application/json")
    fun performLogin(
            @Field("adminNo") adminNo: String,
            @Field("password") password: String,
            @Field("firebaseRegistrationToken") firebaseRegistrationToken: String) : Call<LoginResponse>

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

    /**
     * @param authorization Should be "Bearer<space><jwt token>"
     */
    @GET("/api/dev/event/lesson")
    @Headers("Accept: */*")
    fun getLessons(
            @Header("Authorization") authorization: String,
            @Query("start") startYYYYMM: String,
            @Query("end") endYYYYMM: String? = null,
            @Query("refresh") forceRefresh: Boolean) : Call<List<LessonResponse>>
}