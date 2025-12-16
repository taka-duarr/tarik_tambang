package com.example.tarik_tambang.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @POST("add-win")
    fun addWin(): Call<AddWinResponse>


    @GET("leaderboard")
    fun getLeaderboard(): Call<LeaderboardResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("update-profile")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Field("new_username") newUsername: String,
        @Field("new_password") newPassword: String?
    ): Call<UpdateProfileResponse>


    @POST("delete-account")
    fun deleteAccount(
        @Header("Authorization") token: String
    ): Call<UpdateProfileResponse>




}
