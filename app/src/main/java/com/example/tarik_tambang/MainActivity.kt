package com.example.tarik_tambang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import android.content.Intent
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GameNavigation()
            }
        }
    }
}

@Composable
fun GameNavigation() {
    var activeRoomCode by remember { mutableStateOf<String?>(null) }
    var activeRole by remember { mutableStateOf<String?>(null) }
    var myName by remember { mutableStateOf<String?>(null) }

    if (activeRole == null) {

        LobbyScreen(
            onJoinRoom = { code, role, name ->
                activeRoomCode = code
                activeRole = role
                myName = name
            }
        )
    } else {
        // Tampilkan Game jika sudah ada role
        ActiveGameScreen(
            roomCode = activeRoomCode!!,
            myRole = activeRole!!,
            myName = myName!!,
            onLeaveRoom = {
                activeRoomCode = null
                activeRole = null
                myName = null
            }
        )
    }
}


@Composable
fun LobbyScreen(onJoinRoom: (code: String, role: String, name: String) -> Unit) {
    val roomsRef = remember { FirebaseDatabase.getInstance().getReference("rooms") }

    var enteredName by remember { mutableStateOf("") }
    var roomCodeInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF283593),
                        Color(0xFF3949AB)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "🎮 TARIK TAMBANG 🎮",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A237E)
                    )
                )
                Text(
                    "Math Battle Arena",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF3949AB),
                        fontWeight = FontWeight.Light
                    )
                )
                Spacer(Modifier.height(32.dp))

                // Kolom Nama
                OutlinedTextField(
                    value = enteredName,
                    onValueChange = { enteredName = it },
                    label = { Text("Masukkan Nama Anda") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3949AB),
                        focusedLabelColor = Color(0xFF3949AB)
                    )
                )
                Spacer(Modifier.height(32.dp))

                // Gabung Room
                OutlinedTextField(
                    value = roomCodeInput,
                    onValueChange = { roomCodeInput = it.uppercase() },
                    label = { Text("Kode Room") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3949AB),
                        focusedLabelColor = Color(0xFF3949AB)
                    )
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (enteredName.isBlank()) {
                            message = "Nama tidak boleh kosong"
                            return@Button
                        }
                        if (roomCodeInput.isBlank()) {
                            message = "Kode room tidak boleh kosong"
                            return@Button
                        }

                        roomsRef.child(roomCodeInput).get().addOnSuccessListener { snapshot ->
                            if (!snapshot.exists()) {
                                message = "Room '$roomCodeInput' tidak ditemukan"
                                return@addOnSuccessListener
                            }

                            val aName = snapshot.child("playerA/name").getValue(String::class.java)
                            val bName = snapshot.child("playerB/name").getValue(String::class.java)

                            if (aName.isNullOrBlank() || aName == enteredName) {
                                onJoinRoom(roomCodeInput, "playerA", enteredName)
                            } else if (bName.isNullOrBlank() || bName == enteredName) {
                                onJoinRoom(roomCodeInput, "playerB", enteredName)
                            } else {
                                message = "Room '$roomCodeInput' sudah penuh!"
                            }

                        }.addOnFailureListener {
                            message = "Error: ${it.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🤝 GABUNG ROOM", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(32.dp))
                Text("ATAU")
                Spacer(Modifier.height(32.dp))

                // Buat Room
                Button(
                    onClick = {
                        if (enteredName.isBlank()) {
                            message = "Nama tidak boleh kosong"
                            return@Button
                        }

                        val newRoomCode = generateRoomCode()
                        val roomData = mapOf(
                            "status" to "waiting",
                            "createdAt" to ServerValue.TIMESTAMP
                        )

                        roomsRef.child(newRoomCode).setValue(roomData)
                            .addOnSuccessListener {
                                onJoinRoom(newRoomCode, "playerA", enteredName)
                            }
                            .addOnFailureListener {
                                message = "Gagal membuat room: ${it.message}"
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("✨ BUAT ROOM BARU", fontWeight = FontWeight.Bold)
                }

                if (message.isNotBlank()) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = message,
                        color = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}



@Composable
fun ActiveGameScreen(
    roomCode: String,
    myRole: String,
    myName: String,
    onLeaveRoom: () -> Unit
) {
    val roomRef = remember(roomCode) {
        FirebaseDatabase.getInstance().getReference("rooms/$roomCode")
    }

    // State Lokal
    var playerAName by remember { mutableStateOf("") }
    var playerBName by remember { mutableStateOf("") }
    var playerAScore by remember { mutableStateOf(0) }
    var playerBScore by remember { mutableStateOf(0) }
    var status by remember { mutableStateOf("waiting") }
    var winner by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val playerAReady = remember { mutableStateOf(false) }
    val playerBReady = remember { mutableStateOf(false) }
    var questionText by remember { mutableStateOf("Menunggu soal...") }
    var userAnswer by remember { mutableStateOf("") }

    // Animasi
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val ropePosition by animateFloatAsState(
        targetValue = when {
            playerAScore > playerBScore -> -20f * (playerAScore - playerBScore).coerceAtMost(5)
            playerBScore > playerAScore -> 20f * (playerBScore - playerAScore).coerceAtMost(5)
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rope"
    )


    LaunchedEffect(roomRef, myRole, myName) {
        val playerRef = roomRef.child(myRole)
        val updates = mapOf(
            "name" to myName,
            "score" to 0,
            "ready" to 0
        )
        playerRef.updateChildren(updates)

        roomRef.child("status").get().addOnSuccessListener {
            if(it.getValue(String::class.java) != "playing") {
                roomRef.child("status").setValue("waiting")
            }
        }
    }

    // Listener Firebase
    LaunchedEffect(roomRef) {
        roomRef.child("playerA/name").addValueEventListener(simpleStringListener { playerAName = it ?: "" })
        roomRef.child("playerA/score").addValueEventListener(simpleIntListener { playerAScore = it ?: 0 })
        roomRef.child("playerA/ready").addValueEventListener(simpleIntListener { playerAReady.value = it == 1 })

        roomRef.child("playerB/name").addValueEventListener(simpleStringListener { playerBName = it ?: "" })
        roomRef.child("playerB/score").addValueEventListener(simpleIntListener { playerBScore = it ?: 0 })
        roomRef.child("playerB/ready").addValueEventListener(simpleIntListener { playerBReady.value = it == 1 })

        roomRef.child("status").addValueEventListener(simpleStringListener { status = it ?: "waiting" })
        roomRef.child("winner").addValueEventListener(simpleStringListener { winner = it ?: "" })

        roomRef.child("currentQuestion").addValueEventListener(simpleStringListener {
            questionText = it ?: "Menunggu soal..."
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF283593),
                        Color(0xFF3949AB)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Header (Dengan tombol Share)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎮 TARIK TAMBANG 🎮",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 28.sp
                        )
                    )

                    Spacer(Modifier.height(8.dp))
                    val context = LocalContext.current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Room Code: $roomCode",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Ayo main Tarik Tambang Kuis! GABUNG di room-ku dengan kode: $roomCode")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Bagikan Kode Room")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Room Code",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Player Cards & Rope
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PlayerCard(
                        playerName = playerAName.ifEmpty { "Waiting..." },
                        score = playerAScore,
                        isReady = playerAReady.value,
                        playerLabel = "PLAYER A",
                        color = Color(0xFFFF6B6B),
                        isActive = myRole == "playerA"
                    )

                    PlayerCard(
                        playerName = playerBName.ifEmpty { "Waiting..." },
                        score = playerBScore,
                        isReady = playerBReady.value,
                        playerLabel = "PLAYER B",
                        color = Color(0xFF4ECDC4),
                        isActive = myRole == "playerB"
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Rope Visualization
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(60.dp)
                        .offset(x = ropePosition.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFF6B6B),
                                        Color.White,
                                        Color(0xFF4ECDC4)
                                    )
                                ),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFD93D), CircleShape)
                            .border(4.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏁",
                            fontSize = 20.sp
                        )
                    }
                }
            }

            // Main Content Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Game Screen (UI utama)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Player Info
                        Surface(
                            color = Color(0xFFF1F3F4),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "You: $myName",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A237E)
                                )
                                Text(
                                    text = status.uppercase(),
                                    color = when(status) {
                                        "playing" -> Color(0xFF4CAF50)
                                        else -> Color(0xFFFF9800)
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (winner.isNotBlank()) {
                            // Winner Screen
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.scale(pulseScale)
                            ) {
                                Text("🏆", fontSize = 80.sp)
                                Text(
                                    text = "WINNER!",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFFD93D)
                                    )
                                )
                                Text(
                                    text = winner,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1A237E)
                                    )
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        roomRef.child("playerA/score").setValue(0)
                                        roomRef.child("playerB/score").setValue(0)
                                        roomRef.child("playerA/ready").setValue(0)
                                        roomRef.child("playerB/ready").setValue(0)
                                        roomRef.child("winner").setValue("")
                                        roomRef.child("status").setValue("waiting")
                                        roomRef.child("currentQuestion").setValue("Menunggu...")
                                        roomRef.child("currentAnswer").setValue(0)
                                        message = "Game direset, tunggu pemain lain"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("🔄 Play Again", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            if (status == "playing") {
                                // Question Screen
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        color = Color(0xFF1A237E),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "❓ SOLVE THIS!",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                text = questionText,
                                                style = MaterialTheme.typography.headlineLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 36.sp
                                                )
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(24.dp))

                                    OutlinedTextField(
                                        value = userAnswer,
                                        onValueChange = { userAnswer = it },
                                        label = { Text("Your Answer") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            val answerInt = userAnswer.toIntOrNull()
                                            if (answerInt == null) {
                                                message = "Masukkan jawaban berupa angka"
                                                return@Button
                                            }
                                            roomRef.child("currentAnswer").get().addOnSuccessListener { answerSnapshot ->
                                                val correctAnswer = answerSnapshot.getValue(Int::class.java)

                                                if (answerInt == correctAnswer) {
                                                    message = "✅ BENAR! Menambah skor..."
                                                    val scoreRef = roomRef.child("$myRole/score")

                                                    scoreRef.runTransaction(object : Transaction.Handler {
                                                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                                                            val cur = currentData.getValue(Int::class.java) ?: 0
                                                            currentData.value = cur + 1
                                                            return Transaction.success(currentData)
                                                        }

                                                        override fun onComplete(
                                                            error: DatabaseError?,
                                                            committed: Boolean,
                                                            currentData: DataSnapshot?
                                                        ) {
                                                            if (error != null || !committed) {
                                                                message = "Gagal update skor: ${error?.message}"
                                                                return
                                                            }

                                                            roomRef.get().addOnSuccessListener { snap ->
                                                                val aScore = snap.child("playerA/score").getValue(Int::class.java) ?: 0
                                                                val bScore = snap.child("playerB/score").getValue(Int::class.java) ?: 0
                                                                val threshold = 10

                                                                var winnerFound = false
                                                                if (aScore >= threshold) {
                                                                    roomRef.child("winner").setValue(playerAName.ifBlank { "Player A" })
                                                                    roomRef.child("status").setValue("finished")
                                                                    winnerFound = true
                                                                } else if (bScore >= threshold) {
                                                                    roomRef.child("winner").setValue(playerBName.ifBlank { "Player B" })
                                                                    roomRef.child("status").setValue("finished")
                                                                    winnerFound = true
                                                                }

                                                                if (!winnerFound) {
                                                                    generateNewQuestion(roomRef)
                                                                }
                                                            }
                                                        }
                                                    })
                                                    userAnswer = ""

                                                } else {
                                                    message = "❌ SALAH! Coba lagi."
                                                }
                                            }.addOnFailureListener {
                                                message = "Gagal mengecek jawaban: ${it.message}"
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text("✓ SUBMIT ANSWER", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // "Waiting/Ready Screen"
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val myReadyState = if (myRole == "playerA") playerAReady.value else playerBReady.value

                                    if (!myReadyState) {
                                        Text(
                                            text = "⚡ Ready to Battle?",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1A237E)
                                            )
                                        )
                                        Spacer(Modifier.height(24.dp))
                                        Button(
                                            onClick = {
                                                roomRef.child(myRole).child("ready").setValue(1).addOnSuccessListener {
                                                    val otherReady = if (myRole == "playerA") playerBReady.value else playerAReady.value
                                                    if (otherReady) {
                                                        roomRef.child("status").setValue("playing")
                                                        generateNewQuestion(roomRef)
                                                        message = "Game Dimulai!"
                                                    } else {
                                                        message = "Kamu sudah siap, menunggu lawan."
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth(0.7f)
                                                .height(64.dp)
                                                .scale(pulseScale),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50)
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text("🚀 I'M READY!", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.scale(pulseScale)
                                        ) {
                                            Text("⏳", fontSize = 64.sp)
                                            Spacer(Modifier.height(16.dp))
                                            Text(
                                                text = "Waiting for opponent...",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    color = Color(0xFF666666)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            if (message.isNotBlank() && winner.isBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Surface(
                                    color = when {
                                        message.contains("BENAR") -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                        message.contains("SALAH") -> Color(0xFFFF6B6B).copy(alpha = 0.2f)
                                        else -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = message,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))


                        TextButton(
                            onClick = {

                                roomRef.child(myRole).removeValue().addOnCompleteListener {

                                    val otherRole = if (myRole == "playerA") "playerB" else "playerA"
                                    roomRef.child(otherRole).child("name").get().addOnSuccessListener { snap ->
                                        val otherPlayerName = snap.getValue(String::class.java)

                                        if (otherPlayerName.isNullOrBlank()) {
                                            roomRef.removeValue()
                                        }

                                        // 3B. (Selalu) Kembali ke Lobby
                                        onLeaveRoom()
                                    }.addOnFailureListener {
                                        // Jika gagal cek, tetap keluar
                                        onLeaveRoom()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚪 Leave Room", color = Color(0xFFFF6B6B))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun PlayerCard(
    playerName: String,
    score: Int,
    isReady: Boolean,
    playerLabel: String,
    color: Color,
    isActive: Boolean
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .then(if (isActive) Modifier.border(3.dp, Color.Yellow, RoundedCornerShape(16.dp)) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isActive) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playerLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                if (isReady) {
                    Spacer(Modifier.width(4.dp))
                    Text("✅", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = playerName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = score.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                }
            }
        }
    }
}


fun generateRoomCode(length: Int = 5): String {
    val chars = ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}


fun generateNewQuestion(roomRef: DatabaseReference) {
    val num1 = (1..20).random()
    val num2 = (1..10).random()
    val question = "$num1 + $num2 = ?"
    val answer = num1 + num2

    val updates = mapOf(
        "currentQuestion" to question,
        "currentAnswer" to answer
    )
    roomRef.updateChildren(updates)
}


fun simpleIntListener(onChange: (Int?) -> Unit): ValueEventListener {
    return object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            onChange(snapshot.getValue(Int::class.java))
        }
        override fun onCancelled(error: DatabaseError) {}
    }
}


fun simpleStringListener(onChange: (String?) -> Unit): ValueEventListener {
    return object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            onChange(snapshot.getValue(String::class.java))
        }
        override fun onCancelled(error: DatabaseError) {}
    }
}