package com.example.tarik_tambang.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.R
import com.example.tarik_tambang.ui.components.BackButton
import com.example.tarik_tambang.util.generateRoomCode
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

@Composable
fun LobbyScreen(
    fixedName: String,
    onJoinRoom: (code: String, role: String) -> Unit,
    onBack: () -> Unit,
    onProfileClick: () -> Unit
) {
    val roomsRef = remember { FirebaseDatabase.getInstance().getReference("rooms") }

    var roomCodeInput by remember { mutableStateOf("") }
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
                .alpha(0.7f)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val maxWidthDp = maxWidth
                val titleFontSize = (maxWidthDp.value * 0.12f).coerceIn(28f, 48f).sp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LOBBY",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = titleFontSize,
                            letterSpacing = 2.sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                offset = Offset(4f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )

                    Box(
                        modifier = Modifier
                            .width(maxWidthDp * 0.4f)
                            .height(4.dp)
                            .background(Color(0xFFE60012))
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Welcome text
                Text(
                    text = "Welcome, $fixedName!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                        color = Color(0xFFE60012)
                    )
                )

                Spacer(Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFE60012), CircleShape)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile",
                        tint = Color(0xFFE60012),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Main content card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Input section
                    Text(
                        text = "JOIN EXISTING ROOM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Room code input
                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { roomCodeInput = it.uppercase() },
                        label = { Text("ROOM CODE", fontWeight = FontWeight.Bold) },
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

                    Spacer(Modifier.height(16.dp))

                    // Join button
                    PersonaButton(
                        text = "JOIN ROOM",
                        onClick = {
                            if (roomCodeInput.isBlank()) {
                                message = "Room code cannot be empty"
                                return@PersonaButton
                            }

                            roomsRef.child(roomCodeInput).get().addOnSuccessListener { snapshot ->
                                if (!snapshot.exists()) {
                                    message = "Room '$roomCodeInput' not found"
                                    return@addOnSuccessListener
                                }

                                val aName = snapshot.child("playerA/name").getValue(String::class.java)
                                val bName = snapshot.child("playerB/name").getValue(String::class.java)

                                when {
                                    aName.isNullOrBlank() || aName == fixedName ->
                                        onJoinRoom(roomCodeInput, "playerA")

                                    bName.isNullOrBlank() || bName == fixedName ->
                                        onJoinRoom(roomCodeInput, "playerB")

                                    else ->
                                        message = "Room is full!"
                                }

                            }.addOnFailureListener {
                                message = "Error: ${it.message}"
                            }
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFF4A4A4A))
                        )
                        Text(
                            text = " OR ",
                            color = Color(0xFF999999),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(Color(0xFF4A4A4A))
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Create room section
                    PersonaButton(
                        text = "CREATE NEW ROOM",
                        isPrimary = true,
                        onClick = {
                            val newRoomCode = generateRoomCode()
                            val roomData = mapOf(
                                "status" to "waiting",
                                "createdAt" to ServerValue.TIMESTAMP
                            )

                            roomsRef.child(newRoomCode).setValue(roomData)
                                .addOnSuccessListener {
                                    onJoinRoom(newRoomCode, "playerA")
                                }
                                .addOnFailureListener {
                                    message = "Failed to create room: ${it.message}"
                                }
                        }
                    )

                    // Error message
                    if (message.isNotBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = message,
                            color = Color(0xFFE60012),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        BackButton(onClick = onBack)
    }
}

@Composable
private fun PersonaButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isPrimary) {
                    Brush.horizontalGradient(colors = listOf(Color(0xFFE60012), Color(0xFFFF3333)))
                } else {
                    Brush.horizontalGradient(colors = listOf(Color(0xFF3A3A3A), Color(0xFF2A2A2A)))
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            letterSpacing = 2.sp,
            style = MaterialTheme.typography.titleLarge.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyScreenPreview() {
    LobbyScreen(
        fixedName = "AndroidDev",
        onJoinRoom = { _, _ -> },
        onBack = {},
        onProfileClick = {}
    )
}
