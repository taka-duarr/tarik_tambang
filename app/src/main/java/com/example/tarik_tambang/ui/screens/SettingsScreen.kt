package com.example.tarik_tambang.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.UserPrefs
import com.example.tarik_tambang.audio.AudioManager
import com.example.tarik_tambang.ui.components.BackButton

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var musicVolume by remember { mutableStateOf(UserPrefs.getMusicVolume(context)) }
    var sfxVolume by remember { mutableStateOf(UserPrefs.getSfxVolume(context)) }

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
                        text = "SETTINGS",
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

                    // Red accent line
                    Box(
                        modifier = Modifier
                            .width(lineWidth)
                            .height(6.dp)
                            .background(Color(0xFFE60012))
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Settings content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Music Volume
                PersonaVolumeControl(
                    label = "MUSIC VOLUME",
                    icon = "♪",
                    value = musicVolume,
                    onValueChange = {
                        musicVolume = it
                        AudioManager.setMusicVolume(context, it)
                    }
                )

                // SFX Volume
                PersonaVolumeControl(
                    label = "SFX VOLUME",
                    icon = "🔊",
                    value = sfxVolume,
                    onValueChange = { 
                        sfxVolume = it
                        AudioManager.setSfxVolume(context, it)
                    }
                )
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
private fun PersonaVolumeControl(
    label: String,
    icon: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
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

        // Slider container with Persona 5 style
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF2A2A2A)
                        )
                    )
                )
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color(0xFFE60012))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slider
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFE60012),
                        activeTrackColor = Color(0xFFE60012),
                        inactiveTrackColor = Color(0xFF4A4A4A)
                    )
                )

                Spacer(Modifier.width(16.dp))

                // Percentage display
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(40.dp)
                        .background(Color(0xFFE60012)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(value * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(onBack = {})
}