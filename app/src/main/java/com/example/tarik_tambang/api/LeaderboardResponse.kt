package com.example.tarik_tambang.api

data class LeaderboardResponse(
    val success: Boolean,
    val leaderboard: List<LeaderboardPlayer>
)

data class LeaderboardPlayer(
    val username: String,
    val wins: Int
)
