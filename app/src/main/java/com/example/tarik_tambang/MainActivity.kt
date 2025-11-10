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
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GameScreen()
            }
        }
    }
}

@Composable
fun GameScreen() {
    val roomRef = remember { FirebaseDatabase.getInstance().getReference("rooms/room1") }

    // ... (State nama, skor, status, dll. tidak berubah) ...
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

    // --- BARU: State untuk Kuis Matematika ---
    var questionText by remember { mutableStateOf("Menunggu soal...") }
    var userAnswer by remember { mutableStateOf("") }
    // --- SELESAI BARU ---

    // Listener realtime Firebase
    LaunchedEffect(Unit) {
        roomRef.child("playerA/name").addValueEventListener(simpleStringListener { playerAName = it ?: "" })
        roomRef.child("playerA/score").addValueEventListener(simpleIntListener { playerAScore = it ?: 0 })
        roomRef.child("playerA/ready").addValueEventListener(simpleIntListener { playerAReady.value = it == 1 })

        roomRef.child("playerB/name").addValueEventListener(simpleStringListener { playerBName = it ?: "" })
        roomRef.child("playerB/score").addValueEventListener(simpleIntListener { playerBScore = it ?: 0 })
        roomRef.child("playerB/ready").addValueEventListener(simpleIntListener { playerBReady.value = it == 1 })

        roomRef.child("status").addValueEventListener(simpleStringListener { status = it ?: "waiting" })
        roomRef.child("winner").addValueEventListener(simpleStringListener { winner = it ?: "" })

        // --- BARU: Listener untuk soal ---
        roomRef.child("currentQuestion").addValueEventListener(simpleStringListener {
            questionText = it ?: "Menunggu soal..."
        })
        // --- SELESAI BARU ---
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

            // ... (Tampilan Judul dan Skor tidak berubah) ...
            Text("🔥 Tarik Tambang (Multiplayer)", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                // sudah join
                Text("Kamu: $myName ($myRole)")
                Text("Status: $status")
                Spacer(Modifier.height(8.dp))

                if (winner.isNotBlank()) {
                    Text("🏆 Pemenang: $winner", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        // reset game
                        roomRef.child("playerA/score").setValue(0)
                        roomRef.child("playerB/score").setValue(0)
                        roomRef.child("playerA/ready").setValue(0)
                        roomRef.child("playerB/ready").setValue(0)
                        roomRef.child("winner").setValue("")
                        roomRef.child("status").setValue("waiting")
                        // --- BARU: Reset Soal ---
                        roomRef.child("currentQuestion").setValue("Menunggu...")
                        roomRef.child("currentAnswer").setValue(0)
                        // --- SELESAI BARU ---
                        message = "Game direset, tunggu pemain lain"
                    }) {
                        Text("Play Again")
                    }
                } else {

                    // --- MODIFIKASI TOTAL: Logika Kuis Matematika ---
                    if (status == "playing") {
                        // Game dimulai, tampilkan soal
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

                            // Cek jawaban yang benar di Firebase
                            roomRef.child("currentAnswer").get().addOnSuccessListener { answerSnapshot ->
                                val correctAnswer = answerSnapshot.getValue(Int::class.java)

                                if (answerInt == correctAnswer) {
                                    // Jawaban benar! Jalankan transaksi skor
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

                                            // Skor berhasil ditambah, cek pemenang DAN buat soal baru
                                            roomRef.get().addOnSuccessListener { snap ->
                                                val aScore = snap.child("playerA/score").getValue(Int::class.java) ?: 0
                                                val bScore = snap.child("playerB/score").getValue(Int::class.java) ?: 0
                                                val threshold = 10 // Batas skor kemenangan

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
                                                    // Game belum selesai, buat soal baru
                                                    generateNewQuestion(roomRef)
                                                }
                                            }
                                        }
                                    })
                                    // Kosongkan field setelah submit
                                    userAnswer = ""

                                } else {
                                    // Jawaban salah
                                    message = "SALAH! Coba lagi."
                                }
                            }.addOnFailureListener {
                                message = "Gagal mengecek jawaban: ${it.message}"
                            }
                        }) {
                            Text("Jawab")
                        }

                    } else {
                        // Status masih "waiting", tampilkan logika "Ready"
                        val myReadyState = if (myRole == "playerA") playerAReady.value else playerBReady.value

                        if (!myReadyState) {
                            // Jika saya belum siap, tampilkan tombol Ready
                            Button(onClick = {
                                roomRef.child("$myRole/ready").setValue(1).addOnSuccessListener {
                                    val otherReady = if (myRole == "playerA") playerBReady.value else playerAReady.value
                                    if (otherReady) {
                                        // --- BARU: Jika semua siap, buat soal pertama ---
                                        roomRef.child("status").setValue("playing")
                                        generateNewQuestion(roomRef) // Buat soal pertama
                                        message = "Game Dimulai!"
                                        // --- SELESAI BARU ---
                                    } else {
                                        message = "Kamu sudah siap, menunggu lawan."
                                    }
                                }
                            }) {
                                Text("Ready")
                            }
                        } else {
                            // Jika saya sudah siap, tampilkan teks menunggu
                            Text("Menunggu player lain siap...")
                        }
                    }
                    // --- SELESAI MODIFIKASI ---


                    Spacer(Modifier.height(12.dp))
                    Text(message)
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    if(myRole != null) {
                        roomRef.child("$myRole/ready").setValue(0)
                        roomRef.child("$myRole/name").setValue("") // Kosongkan nama saat keluar
                    }
                    myRole = null
                    myName = ""
                    enteredName = ""
                    message = "Kamu keluar dari room"
                }) {
                    Text("Leave Room")
                }
            }
        }
    }
}

/**
 * BARU: Helper function untuk membuat soal matematika dan menyimpannya ke Firebase.
 */
fun generateNewQuestion(roomRef: DatabaseReference) {
    val num1 = (1..20).random() // Angka acak 1-20
    val num2 = (1..10).random() // Angka acak 1-10
    val question = "$num1 + $num2 = ?"
    val answer = num1 + num2

    // Gunakan updateChildren untuk mengirim kedua data sekaligus
    val updates = mapOf(
        "currentQuestion" to question,
        "currentAnswer" to answer
    )
    roomRef.updateChildren(updates)
}


/* =========================
   HELPER LISTENERS (Tidak berubah)
   ========================= */
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