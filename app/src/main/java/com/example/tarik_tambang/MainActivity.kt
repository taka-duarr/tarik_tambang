package com.example.tarik_tambang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Kita ganti GameScreen() dengan GameNavigation() sebagai entry point
                GameNavigation()
            }
        }
    }
}

/**
 * BARU: Composable ini bertugas sebagai "Router"
 * Dia akan menampilkan Lobby ATAU Game, tergantung state.
 */
@Composable
fun GameNavigation() {
    // State untuk menyimpan kode room yang sedang aktif.
    // Jika null, kita ada di Lobby. Jika terisi, kita ada di dalam game.
    var activeRoomCode by remember { mutableStateOf<String?>(null) }

    if (activeRoomCode == null) {
        // Tampilkan Lobby jika belum masuk room
        LobbyScreen(
            onJoinRoom = { code ->
                // Ini dipanggil saat user berhasil join/buat room
                activeRoomCode = code
            }
        )
    } else {
        // Tampilkan Game jika sudah ada kode room
        ActiveGameScreen(
            roomCode = activeRoomCode!!,
            onLeaveRoom = {
                // Ini dipanggil saat user klik "Leave Room"
                activeRoomCode = null
            }
        )
    }
}

/**
 * BARU: UI untuk menu utama (Lobby)
 * (Dengan perbaikan pada tombol "Buat Room Baru")
 */
@Composable
fun LobbyScreen(onJoinRoom: (String) -> Unit) {
    // Referensi ke root "rooms" untuk mengecek apakah room ada
    val roomsRef = remember { FirebaseDatabase.getInstance().getReference("rooms") }

    var roomCodeInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔥 Tarik Tambang Kuis", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(48.dp))

        // --- Bagian Gabung Room ---
        Text("Gabung Room", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = roomCodeInput,
            onValueChange = { roomCodeInput = it.uppercase() }, // Otomatis uppercase
            label = { Text("Masukkan Kode Room") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (roomCodeInput.isBlank()) {
                    message = "Kode room tidak boleh kosong"
                    return@Button
                }

                // Cek apakah room ada di Firebase
                roomsRef.child(roomCodeInput).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Room ada, pindah ke game
                        onJoinRoom(roomCodeInput)
                    } else {
                        // Room tidak ada
                        message = "Room '$roomCodeInput' tidak ditemukan"
                    }
                }.addOnFailureListener {
                    message = "Gagal cek room: ${it.message}"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gabung")
        }

        Spacer(Modifier.height(48.dp))
        Text("ATAU", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(48.dp))

        // --- MODIFIKASI: Bagian Buat Room ---
        Button(
            onClick = {
                val newRoomCode = generateRoomCode()

                // BARU: Buat room-nya di Firebase dulu
                // Kita set "status" sebagai placeholder agar node-nya terbuat
                roomsRef.child(newRoomCode).child("status").setValue("waiting")
                    .addOnSuccessListener {
                        // Setelah room sukses dibuat di DB, baru pindah layar
                        message = "" // Bersihkan pesan error lama (jika ada)
                        onJoinRoom(newRoomCode)
                    }
                    .addOnFailureListener {
                        // Tampilkan error jika gagal membuat room
                        message = "Gagal membuat room: ${it.message}"
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Buat Room Baru")
        }

        Spacer(Modifier.height(24.dp))
        // PERBAIKAN: Menggunakan 'color' bukan 'style'
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}


/**
 * MODIFIKASI: Ini adalah kode GameScreen() Anda yang lama.
 * Namanya diganti jadi ActiveGameScreen() dan menerima parameter.
 */
@Composable
fun ActiveGameScreen(roomCode: String, onLeaveRoom: () -> Unit) {
    // MODIFIKASI: roomRef sekarang dinamis berdasarkan parameter roomCode
    val roomRef = remember(roomCode) {
        FirebaseDatabase.getInstance().getReference("rooms/$roomCode")
    }

    // --- (Seluruh state Anda yang lain tetap sama) ---
    var enteredName by remember { mutableStateOf("") }
    var myRole by remember { mutableStateOf<String?>(null) }
    var myName by remember { mutableStateOf("") }
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

    // --- (Semua LaunchedEffect/Listener Anda tetap sama) ---
    LaunchedEffect(roomRef) { // Kita tambahkan roomRef sebagai key
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // BARU: Tampilkan kode room agar bisa di-share
            Text("Kode Room: $roomCode", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            // --- (Sisa UI Anda dari sini ke bawah 95% SAMA) ---

            Text("🔥 Tarik Tambang (Multiplayer)", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // ... (UI Skor A dan B tidak berubah) ...
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Player A ${if (playerAReady.value) "✅" else "..."}")
                    Text(playerAName.ifEmpty { "-" })
                    Text("Score: $playerAScore")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Player B ${if (playerBReady.value) "✅" else "..."}")
                    Text(playerBName.ifEmpty { "-" })
                    Text("Score: $playerBScore")
                }
            }
            Spacer(Modifier.height(24.dp))

            if (myRole == null) {
                // ... (Logika "belum join" tidak berubah) ...
                OutlinedTextField(
                    value = enteredName,
                    onValueChange = { enteredName = it },
                    label = { Text("Masukkan nama") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Button(onClick = {
                        if (enteredName.isBlank()) {
                            message = "Nama tidak boleh kosong"
                            return@Button
                        }
                        myRole = "playerA"
                        myName = enteredName
                        roomRef.child("playerA/name").setValue(enteredName)
                        roomRef.child("playerA/score").setValue(0)
                        roomRef.child("playerA/ready").setValue(0)
                        roomRef.child("status").setValue("waiting")
                        message = "Masuk sebagai Player A"
                    }) { Text("Masuk sebagai Player A") }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        if (enteredName.isBlank()) {
                            message = "Nama tidak boleh kosong"
                            return@Button
                        }
                        myRole = "playerB"
                        myName = enteredName
                        roomRef.child("playerB/name").setValue(enteredName)
                        roomRef.child("playerB/score").setValue(0)
                        roomRef.child("playerB/ready").setValue(0)

                        roomRef.child("playerA/name").get().addOnSuccessListener { snap ->
                            val aName = snap.getValue(String::class.java)
                            if (!aName.isNullOrBlank()) {
                                roomRef.child("status").setValue("waiting")
                                message = "Game siap, klik Ready!"
                            } else {
                                roomRef.child("status").setValue("waiting")
                                message = "Menunggu Player A"
                            }
                        }
                    }) { Text("Masuk sebagai Player B") }
                }

                Spacer(Modifier.height(12.dp))
                Text(message)

            } else {
                // ... (Logika "sudah join" tidak berubah) ...
                Text("Kamu: $myName ($myRole)")
                Text("Status: $status")
                Spacer(Modifier.height(8.dp))

                if (winner.isNotBlank()) {
                    // ... (Logika "Pemenang" tidak berubah) ...
                    Text("🏆 Pemenang: $winner", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        roomRef.child("playerA/score").setValue(0)
                        roomRef.child("playerB/score").setValue(0)
                        roomRef.child("playerA/ready").setValue(0)
                        roomRef.child("playerB/ready").setValue(0)
                        roomRef.child("winner").setValue("")
                        roomRef.child("status").setValue("waiting")
                        roomRef.child("currentQuestion").setValue("Menunggu...")
                        roomRef.child("currentAnswer").setValue(0)
                        message = "Game direset, tunggu pemain lain"
                    }) {
                        Text("Play Again")
                    }
                } else {
                    // ... (Logika "Kuis" tidak berubah) ...
                    if (status == "playing") {
                        Text(questionText, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userAnswer,
                            onValueChange = { userAnswer = it },
                            label = { Text("Jawaban Anda") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        Button(onClick = {
                            val answerInt = userAnswer.toIntOrNull()
                            if (answerInt == null) {
                                message = "Masukkan jawaban berupa angka"
                                return@Button
                            }

                            roomRef.child("currentAnswer").get().addOnSuccessListener { answerSnapshot ->
                                val correctAnswer = answerSnapshot.getValue(Int::class.java)

                                if (answerInt == correctAnswer) {
                                    message = "BENAR! Menambah skor..."
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
                                    message = "SALAH! Coba lagi."
                                }
                            }.addOnFailureListener {
                                message = "Gagal mengecek jawaban: ${it.message}"
                            }
                        }) {
                            Text("Jawab")
                        }

                    } else {
                        // ... (Logika "Ready" tidak berubah) ...
                        val myReadyState = if (myRole == "playerA") playerAReady.value else playerBReady.value

                        if (!myReadyState) {
                            Button(onClick = {
                                roomRef.child("$myRole/ready").setValue(1).addOnSuccessListener {
                                    val otherReady = if (myRole == "playerA") playerBReady.value else playerAReady.value
                                    if (otherReady) {
                                        roomRef.child("status").setValue("playing")
                                        generateNewQuestion(roomRef)
                                        message = "Game Dimulai!"
                                    } else {
                                        message = "Kamu sudah siap, menunggu lawan."
                                    }
                                }
                            }) {
                                Text("Ready")
                            }
                        } else {
                            Text("Menunggu player lain siap...")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(message)
                }

                Spacer(Modifier.height(16.dp))
                // MODIFIKASI: Tombol "Leave Room" sekarang memanggil onLeaveRoom
                Button(onClick = {
                    if(myRole != null) {
                        // Bersihkan data pemain ini dari room
                        roomRef.child("$myRole/ready").setValue(0)
                        roomRef.child("$myRole/name").setValue("")
                        roomRef.child("$myRole/score").setValue(0)
                    }
                    // Panggil fungsi untuk kembali ke Lobby
                    onLeaveRoom()
                }) {
                    Text("Leave Room")
                }
            }
        }
    }
}

/**
 * BARU: Helper function untuk membuat kode room acak
 */
fun generateRoomCode(length: Int = 5): String {
    val chars = ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}


// --- (Semua helper function Anda yang lain tetap sama) ---

/**
 * Helper function untuk membuat soal matematika dan menyimpannya ke Firebase.
 */
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