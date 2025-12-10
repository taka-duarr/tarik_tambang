package com.example.tarik_tambang.ui.screens

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.R
import com.example.tarik_tambang.audio.AudioManager
import com.example.tarik_tambang.UserPrefs

@Composable
fun MainMenuScreen(
    onPlay: () -> Unit,
    onLeaderboard: () -> Unit,
    onSettings: () -> Unit,
    onProfile: () -> Unit
) {
    val context = LocalContext.current

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
        // Diagonal red stripes background (Persona 5 style)
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
                    .fillMaxWidth()
                    .height(80.dp)
                    .offset(y = (index * 200f - offset).dp)
                    .rotate(-45f)
                    .background(Color.Red.copy(alpha = 0.1f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(40.dp))

            // Persona 5 style title with skew effect
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val maxWidthDp = maxWidth
                val titleFontSize = (maxWidthDp.value * 0.18f).coerceIn(36f, 72f).sp
                val subtitleFontSize = (maxWidthDp.value * 0.04f).coerceIn(12f, 16f).sp
                val lineWidth = maxWidthDp * 0.8f


                Column(
                    horizontalAlignment = Alignment.Start
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

                    Spacer(Modifier.height(4.dp))

                    // Red accent line
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

            Spacer(Modifier.weight(1f))

            // Persona 5 style menu
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PersonaMenuButton(
                    text = "PLAY",
                    onClick = onPlay
                )

                PersonaMenuButton(
                    text = "LEADERBOARD",
                    onClick = onLeaderboard
                )

                PersonaMenuButton(
                    text = "SETTINGS",
                    onClick = onSettings
                )

                PersonaMenuButton(
                    text = "PROFILE",
                    onClick = {
                        onProfile()
                    }
                )



                PersonaMenuButton(
                    text = "QUIT",
                    onClick = {
                        AudioManager.playSfx(R.raw.quit_sfx)
                        (context as? Activity)?.finish()
                    }
                )
            }

            Spacer(Modifier.height(40.dp))
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

@Composable
private fun PersonaMenuButton(
    text: String,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val offsetX by animateFloatAsState(
        targetValue = if (isHovered) -8f else 0f,
        animationSpec = tween(200),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(56.dp)
            .scale(scale)
            .offset(x = offsetX.dp)
    ) {
        // Background skewed box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 28.dp,
                        bottomEnd = 0.dp,
                        bottomStart = 28.dp
                    )
                )
                .background(
                    if (isHovered) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE60012),
                                Color(0xFFFF0000)
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
                .clickable { 
                    isPressed = true
                    onClick()
                }
                .then(
                    if (!isPressed) Modifier
                    else Modifier.also {
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(100)
                            isPressed = false
                        }
                    }
                )
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(
                        if (isHovered) Color.White else Color(0xFFE60012)
                    )
            )

            // Text
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        color = if (isHovered) Color.White else Color(0xFFCCCCCC),
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )

                // Arrow indicator
                Text(
                    text = "▶",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        color = if (isHovered) Color.White else Color(0xFFE60012)
                    )
                )
            }
        }

        // Hover state listener
        LaunchedEffect(isPressed) {
            if (!isPressed) {
                isHovered = false
                kotlinx.coroutines.delay(50)
                isHovered = true
                kotlinx.coroutines.delay(300)
                isHovered = false
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    MainMenuScreen(
        onPlay = {},
        onLeaderboard = {},
        onSettings = {},
        onProfile = {}
    )
}