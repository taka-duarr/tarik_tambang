package com.example.tarik_tambang.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

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
                .alpha(0.8f)
        )

        // Animated diagonal lines
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(3f)
                    .height(80.dp)
                    .offset(y = (index * 200 - offset).dp)
                    .rotate(-45f)
                    .background(Color.Red.copy(alpha = 0.1f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val maxWidthDp = maxWidth
                val titleFontSize = (maxWidthDp.value * 0.18f).coerceIn(36f, 64f).sp
                val subtitleFontSize = (maxWidthDp.value * 0.04f).coerceIn(12f, 16f).sp
                val lineWidth = maxWidthDp * 0.8f

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TARIK",
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
                        text = "TAMBANG",
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

                    Box(
                        modifier = Modifier
                            .width(lineWidth)
                            .height(6.dp)
                            .background(Color(0xFFE60012))
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "THE KUIS GAME",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = subtitleFontSize,
                            letterSpacing = 4.sp,
                            color = Color(0xFFE60012)
                        )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Login Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A1A1A),
                                Color(0xFF0A0A0A)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = Color(0xFFE60012),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // Left accent bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(Color(0xFFE60012))
                )

                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LOGIN",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            letterSpacing = 2.sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(3f, 3f),
                                blurRadius = 6f
                            )
                        )
                    )

                    Spacer(Modifier.height(32.dp))

                    // Username input
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.trim() },
                        label = { Text("USERNAME", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE60012),
                            unfocusedBorderColor = Color(0xFF4A4A4A),
                            focusedLabelColor = Color(0xFFE60012),
                            unfocusedLabelColor = Color(0xFF999999),
                            cursorColor = Color(0xFFE60012),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(24.dp))

                    // Login button
                    Button(
                        onClick = {
                            if (username.isBlank()) {
                                message = "Username tidak boleh kosong"
                            } else {
                                onLogin(username)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE60012)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "MASUK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 1.sp,
                                color = Color.White,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }

                    // Error message
                    if (message.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFE60012).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    Color(0xFFE60012),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = message,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

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