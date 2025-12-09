package com.example.tarik_tambang.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("add-win")
    fun addWin(
        @Field("username") username: String
    ): Call<AddWinResponse>

    @GET("leaderboard")
    fun getLeaderboard(): Call<LeaderboardResponse>


}
