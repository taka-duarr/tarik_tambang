package com.example.tarik_tambang.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.R
import com.example.tarik_tambang.audio.AudioManager
import com.example.tarik_tambang.ui.components.BackButton
import com.example.tarik_tambang.ui.components.PlayerCard
import com.google.firebase.database.*
import kotlinx.coroutines.delay

// --- Helper Functions ---

fun simpleStringListener(onValue: (String?) -> Unit) = object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) = onValue(snapshot.getValue(String::class.java))
    override fun onCancelled(error: DatabaseError) = onValue(null)
}

fun simpleIntListener(onValue: (Int?) -> Unit) = object : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) = onValue(snapshot.getValue(Int::class.java))
    override fun onCancelled(error: DatabaseError) = onValue(null)
}

fun generateNewQuestion(roomRef: DatabaseReference) {
    val num1 = (1..20).random()
    val num2 = (1..20).random()
    roomRef.updateChildren(mapOf(
        "currentQuestion" to "$num1 + $num2 = ?",
        "currentAnswer" to num1 + num2
    ))
}

// --- Composables ---

@SuppressLint("Range")
@Composable
fun ActiveGameScreen(
    roomCode: String,
    myRole: String,
    myName: String,
    onLeaveRoom: () -> Unit
) {
    val roomRef = remember { FirebaseDatabase.getInstance().getReference("rooms").child(roomCode) }

    var playerAName by remember { mutableStateOf("") }
    var playerAScore by remember { mutableStateOf(0) }
    val playerAReady = remember { mutableStateOf(false) }

    var playerBName by remember { mutableStateOf("") }
    var playerBScore by remember { mutableStateOf(0) }
    val playerBReady = remember { mutableStateOf(false) }

    var status by remember { mutableStateOf("waiting") }
    var winner by remember { mutableStateOf("") }
    var questionText by remember { mutableStateOf("Menunggu soal...") }
    var userAnswer by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val offset by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 100f, animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "")
    val pulseScale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.05f, animationSpec = infiniteRepeatable(animation = tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "")

    val ropePosition by animateFloatAsState(targetValue = ((playerBScore - playerAScore) * 15).toFloat().coerceIn(-130f, 130f), animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "")

    // Firebase Listeners
    DisposableEffect(roomCode) {
        val nameListener = roomRef.child("$myRole/name").addValueEventListener(simpleStringListener { if(it.isNullOrBlank()) roomRef.child("$myRole/name").setValue(myName) })
        onDispose { roomRef.child("$myRole/name").removeEventListener(nameListener) }
    }
    DisposableEffect(Unit) {
        onDispose {
            roomRef.child("status").onDisconnect().setValue("waiting")
        }
    }
    LaunchedEffect(roomRef) {
        roomRef.child("playerA/name").addValueEventListener(simpleStringListener { playerAName = it ?: "" })
        roomRef.child("playerA/score").addValueEventListener(simpleIntListener { playerAScore = it ?: 0 })
        roomRef.child("playerA/ready").addValueEventListener(simpleIntListener { playerAReady.value = it == 1 })
        roomRef.child("playerB/name").addValueEventListener(simpleStringListener { playerBName = it ?: "" })
        roomRef.child("playerB/score").addValueEventListener(simpleIntListener { playerBScore = it ?: 0 })
        roomRef.child("playerB/ready").addValueEventListener(simpleIntListener { playerBReady.value = it == 1 })
        roomRef.child("status").addValueEventListener(simpleStringListener { status = it ?: "waiting" })
        roomRef.child("winner").addValueEventListener(simpleStringListener { winner = it ?: "" })
        roomRef.child("currentQuestion").addValueEventListener(simpleStringListener { questionText = it ?: "Menunggu soal..." })
    }

    // UI
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = listOf(Color(0xFFCC0000), Color(0xFF990000), Color.Black), start = Offset(0f, 0f), end = Offset(1000f, 1000f))).alpha(0.7f))
        repeat(5) { index -> Box(modifier = Modifier.fillMaxWidth(3f).height(80.dp).offset(y = (index * 200f - offset).dp).rotate(-45f).background(Color.Red.copy(alpha = 0.1f))) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Header(roomCode = roomCode)
            Spacer(Modifier.height(16.dp))
            PlayerSection(playerAName, playerBName, playerAScore, playerBScore, playerAReady.value, playerBReady.value, myRole, ropePosition)
            Spacer(Modifier.height(16.dp))
            GameContent(
                modifier = Modifier.weight(1f),
                roomRef = roomRef, status = status, winner = winner, pulseScale = pulseScale,
                myName = myName, myRole = myRole, playerAName = playerAName, playerBName = playerBName,
                questionText = questionText, userAnswer = userAnswer, message = message,
                playerAReady = playerAReady.value, playerBReady = playerBReady.value,
                onUserAnswerChange = { userAnswer = it },
                onMessageChange = { message = it }
            )
        }

        GameBackButton(roomRef, myRole, onLeaveRoom)
    }
}

@Composable
private fun Header(roomCode: String) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(colors = listOf(Color(0xFF1A1A1A), Color(0xFF2A2A2A))),
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("ROOM CODE", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF999999), fontWeight = FontWeight.Bold, letterSpacing = 2.sp))
                Text(roomCode, style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp))
            }
            IconButton(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Ayo main Tarik Tambang Kuis! GABUNG di room-ku dengan kode: $roomCode")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Kode Room"))
                },
                modifier = Modifier.size(48.dp).background(Color(0xFFE60012), CircleShape)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share Room Code", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun PlayerSection(playerAName: String, playerBName: String, playerAScore: Int, playerBScore: Int, playerAReady: Boolean, playerBReady: Boolean, myRole: String, ropePosition: Float) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            PlayerCard(playerName = playerAName.ifEmpty { "Waiting..." }, score = playerAScore, isReady = playerAReady, color = Color(0xFFFF6B6B), isActive = myRole == "playerA")
            PlayerCard(playerName = playerBName.ifEmpty { "Waiting..." }, score = playerBScore, isReady = playerBReady, color = Color(0xFF4ECDC4), isActive = myRole == "playerB")
        }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth(0.9f).height(60.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFFFF6B6B), Color.White, Color(0xFF4ECDC4))), shape = RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.size(40.dp).offset(x = ropePosition.dp).background(Color(0xFFFFD93D), CircleShape).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = "🏁", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun GameContent(
    modifier: Modifier = Modifier,
    roomRef: DatabaseReference, status: String, winner: String, pulseScale: Float,
    myName: String, myRole: String, playerAName: String, playerBName: String,
    questionText: String, userAnswer: String, message: String,
    playerAReady: Boolean, playerBReady: Boolean,
    onUserAnswerChange: (String) -> Unit, onMessageChange: (String) -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth().background(Brush.verticalGradient(colors = listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))), shape = RoundedCornerShape(16.dp)).border(1.dp, Color.Black, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "YOU: $myName", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp, letterSpacing = 1.sp)
                Box(modifier = Modifier.background(if (status == "playing") Color(0xFF4CAF50) else Color(0xFFFF9800), shape = RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(text = status.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                }
            }

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (winner.isNotBlank()) {
                    WinnerContent(winner, pulseScale) { 
                        roomRef.updateChildren(mapOf("playerA/score" to 0, "playerB/score" to 0, "playerA/ready" to 0, "playerB/ready" to 0, "winner" to "", "status" to "waiting", "currentQuestion" to "Menunggu...", "currentAnswer" to 0))
                            .addOnSuccessListener { onMessageChange("Game direset, tunggu pemain lain") }
                    }
                } else if (status == "playing") {
                    PlayingContent(questionText, userAnswer, onUserAnswerChange) { 
                        val answerInt = userAnswer.toIntOrNull()
                        if (answerInt == null) { onMessageChange("Masukkan jawaban berupa angka"); return@PlayingContent }
                        roomRef.child("currentAnswer").get().addOnSuccessListener { answerSnapshot ->
                            val correctAnswer = answerSnapshot.getValue(Int::class.java)
                            if (answerInt == correctAnswer) {
                                onMessageChange("BENAR! Menambah skor...")
                                roomRef.child("$myRole/score").runTransaction(object : Transaction.Handler {
                                    override fun doTransaction(currentData: MutableData): Transaction.Result { currentData.value = (currentData.getValue(Int::class.java) ?: 0) + 1; return Transaction.success(currentData) }
                                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                        if (error != null || !committed) { onMessageChange("Gagal update skor: ${error?.message}"); return }
                                        roomRef.get().addOnSuccessListener { snap ->
                                            val aScore = snap.child("playerA/score").getValue(Int::class.java) ?: 0
                                            val bScore = snap.child("playerB/score").getValue(Int::class.java) ?: 0
                                            if (aScore >= 10) roomRef.updateChildren(mapOf("winner" to playerAName.ifBlank { "Player A" }, "status" to "finished"))
                                            else if (bScore >= 10) roomRef.updateChildren(mapOf("winner" to playerBName.ifBlank { "Player B" }, "status" to "finished"))
                                            else generateNewQuestion(roomRef)
                                        }
                                    }
                                })
                                onUserAnswerChange("")
                            } else {
                                onMessageChange("SALAH! Coba lagi.")
                            }
                        }.addOnFailureListener { onMessageChange("Gagal mengecek jawaban: ${it.message}") }
                    }
                } else {
                    val myReadyState = if (myRole == "playerA") playerAReady else playerBReady
                    WaitingContent(myReadyState, pulseScale) { 
                        AudioManager.playSfx(R.raw.ready_sfx)
                        roomRef.child("$myRole/ready").setValue(1).addOnSuccessListener {
                            val otherReady = if (myRole == "playerA") playerBReady else playerAReady
                            if (otherReady) {
                                roomRef.child("status").setValue("playing")
                                generateNewQuestion(roomRef)
                                onMessageChange("Game Dimulai!")
                            } else {
                                onMessageChange("Kamu sudah siap, menunggu lawan.")
                            }
                        }
                    }
                }
            }

            if (message.isNotBlank() && winner.isBlank()) {
                Box(modifier = Modifier.fillMaxWidth().background(when { message.contains("BENAR") -> Color(0xFF4CAF50); message.contains("SALAH") -> Color(0xFFE60012); else -> Color(0xFF2196F3) }.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)).border(1.dp, when { message.contains("BENAR") -> Color(0xFF4CAF50); message.contains("SALAH") -> Color(0xFFE60012); else -> Color(0xFF2196F3) }, shape = RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Text(text = message, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun WinnerContent(winner: String, pulseScale: Float, onPlayAgain: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.scale(pulseScale)) {
        Text("🏆", fontSize = 80.sp)
        Text("WINNER!", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, fontSize = 48.sp, color = Color(0xFFFFD93D), shadow = Shadow(color = Color.Black.copy(alpha = 0.8f), offset = Offset(4f, 4f), blurRadius = 8f)))
        Text(winner, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 28.sp))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth(0.8f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)), shape = RoundedCornerShape(8.dp)) {
            Text("PLAY AGAIN", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun PlayingContent(questionText: String, userAnswer: String, onUserAnswerChange: (String) -> Unit, onSubmit: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFE60012), shape = RoundedCornerShape(12.dp)).padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("❓ SOLVE THIS!", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                Text(text = questionText, style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.Black, fontSize = 42.sp, shadow = Shadow(color = Color.Black.copy(alpha = 0.5f), offset = Offset(3f, 3f), blurRadius = 6f)))
            }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = userAnswer, onValueChange = onUserAnswerChange, label = { Text("YOUR ANSWER", fontWeight = FontWeight.Bold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFE60012), unfocusedBorderColor = Color(0xFF4A4A4A), focusedLabelColor = Color(0xFFE60012), unfocusedLabelColor = Color(0xFF999999), cursorColor = Color(0xFFE60012), focusedTextColor = Color.White, unfocusedTextColor = Color.White), textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)), shape = RoundedCornerShape(8.dp)) {
            Text("✓ SUBMIT ANSWER", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun WaitingContent(myReadyState: Boolean, pulseScale: Float, onReadyClick: () -> Unit) {
    if (!myReadyState) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("⚡", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Ready to Battle?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    shadow = Shadow(color = Color.Black, offset = Offset(2f, 2f), blurRadius = 4f)
                )
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onReadyClick,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(64.dp)
                    .scale(pulseScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE60012),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text("I'M READY!", fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            verticalArrangement = Arrangement.Center
        ) {
            PulsingDots()
            Spacer(Modifier.height(24.dp))
            Text(
                text = "WAITING FOR OPPONENT...",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    shadow = Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 2f)
                )
            )
        }
    }
}

@Composable
fun PulsingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val dotScales = (1..3).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1200
                    0.5f at 0
                    1f at 300
                    0.5f at 600
                    0.5f at 1200
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(index * 200)
            ), label = ""
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        dotScales.forEach {
            Box(modifier = Modifier.size(20.dp).scale(it.value).background(Color(0xFFE60012), CircleShape))
        }
    }
}

@Composable
private fun GameBackButton(roomRef: DatabaseReference, myRole: String, onLeaveRoom: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackButton(onClick = {
            val otherRole = if (myRole == "playerA") "playerB" else "playerA"
            roomRef.child(myRole).removeValue().addOnCompleteListener {
                roomRef.child(otherRole).child("name").get().addOnSuccessListener { snap ->
                    if (snap.getValue(String::class.java).isNullOrBlank()) roomRef.removeValue()
                    onLeaveRoom()
                }.addOnFailureListener { onLeaveRoom() }
            }
        })
    }
}