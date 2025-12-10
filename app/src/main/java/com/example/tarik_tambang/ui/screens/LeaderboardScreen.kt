package com.example.tarik_tambang.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.ui.components.BackButton
import com.example.tarik_tambang.api.ApiClient
import com.example.tarik_tambang.api.LeaderboardResponse
import com.example.tarik_tambang.api.LeaderboardPlayer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    var leaderboard by remember { mutableStateOf<List<LeaderboardPlayer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Animasi diagonal stripes
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    // Ambil data dari mysql
    LaunchedEffect(Unit) {
        ApiClient.instance.getLeaderboard()
            .enqueue(object : Callback<LeaderboardResponse> {
                override fun onResponse(
                    call: Call<LeaderboardResponse>,
                    response: Response<LeaderboardResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.success) {
                        leaderboard = body.leaderboard
                    } else {
                        errorMessage = "Failed to load leaderboard. Please try again later."
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<LeaderboardResponse>, t: Throwable) {
                    errorMessage = "Failed to connect to the server. Please check your internet connection."
                    isLoading = false
                }
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Diagonal red stripes background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFCC0000),
                            Color(0xFF990000),
                            Color.Black
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .alpha(0.7f)
        )

        // Animated diagonal lines
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(3f)
                    .height(80.dp)
                    .offset(y = (index * 200f - offset).dp)
                    .rotate(-45f)
                    .background(Color.Red.copy(alpha = 0.1f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            // Persona 5 style title
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                val maxWidthDp = maxWidth
                val titleFontSize = (maxWidthDp.value * 0.15f).coerceIn(32f, 56f).sp
                val lineWidth = maxWidthDp * 0.7f

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "TOP",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = titleFontSize,
                            letterSpacing = (-2).sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(4f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )

                    Text(
                        text = "PLAYER",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = titleFontSize,
                            letterSpacing = (-2).sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(4f, 4f),
                                blurRadius = 8f
                            )
                        ),
                        modifier = Modifier.offset(x = (maxWidthDp.value * 0.05f).dp)
                    )

                    // Red accent line
                    Box(
                        modifier = Modifier
                            .width(lineWidth)
                            .height(6.dp)
                            .background(Color(0xFFE60012))
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color(0xFFE60012),
                                strokeWidth = 4.dp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "LOADING...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 3.sp
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 3.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        itemsIndexed(leaderboard) { index, player ->
                            PersonaLeaderboardItem(
                                rank = index + 1,
                                name = player.username,
                                score = player.wins
                            )
                        }
                    }
                }
            }
        }

        BackButton(onClick = onBack)

        // Bottom right accent
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(150.dp)
                .offset(x = 50.dp, y = 50.dp)
                .rotate(45f)
                .background(Color(0xFFE60012).copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun PersonaLeaderboardItem(rank: Int, name: String, score: Int) {
    val isTopThree = rank <= 3

    // Medal emoji untuk top 3
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isTopThree) 80.dp else 70.dp)
    ) {
        // Background box dengan Persona 5 style
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = if (isTopThree) 35.dp else 35.dp,
                        bottomEnd = 0.dp,
                        bottomStart = if (isTopThree) 35.dp else 35.dp
                    )
                )
                .background(
                    if (isTopThree) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE60012),
                                Color(0xFFFF3333)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A1A1A),
                                Color(0xFF2A2A2A)
                            )
                        )
                    }
                )
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(if (isTopThree) 6.dp else 4.dp)
                    .background(
                        if (isTopThree) Color.White else Color(0xFFE60012)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Rank + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Rank dengan background
                    Box(
                        modifier = Modifier
                            .size(if (isTopThree) 50.dp else 45.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isTopThree) {
                                    Color.White.copy(alpha = 0.2f)
                                } else {
                                    Color(0xFFE60012).copy(alpha = 0.3f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (medal.isNotEmpty()) medal else "#$rank",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = if (isTopThree) 24.sp else 20.sp,
                            style = MaterialTheme.typography.titleLarge.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Name
                    Column {
                        Text(
                            text = name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isTopThree) 22.sp else 18.sp,
                            letterSpacing = 0.5.sp,
                            style = MaterialTheme.typography.titleMedium.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )

                        if (isTopThree) {
                            Text(
                                text = when (rank) {
                                    1 -> "CHAMPION"
                                    2 -> "RUNNER UP"
                                    3 -> "3RD PLACE"
                                    else -> ""
                                },
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                // Right side: Score
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = score.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = if (isTopThree) 28.sp else 24.sp,
                        letterSpacing = 1.sp,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = "WINS",
                        color = if (isTopThree) {
                            Color.White.copy(alpha = 0.8f)
                        } else {
                            Color(0xFFE60012)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}