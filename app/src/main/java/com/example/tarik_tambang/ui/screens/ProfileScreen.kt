package com.example.tarik_tambang.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.UserPrefs
import com.example.tarik_tambang.ui.components.BackButton
import androidx.compose.foundation.clickable
import com.example.tarik_tambang.api.ApiClient
import com.example.tarik_tambang.api.UpdateProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private fun deleteAccount(username: String, onLogout: () -> Unit) {
    ApiClient.instance.deleteAccount(username)
        .enqueue(object : Callback<UpdateProfileResponse> {

            override fun onResponse(
                call: Call<UpdateProfileResponse>,
                response: Response<UpdateProfileResponse>
            ) {
                val res = response.body()

                if (res != null && res.success) {
                    onLogout()  // kembali ke login
                }
            }

            override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                // optional: bisa tambahkan log
            }
        })
}

@Composable
fun ProfileScreen(
    username: String,
    onUsernameUpdated: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var newUsername by remember { mutableStateOf(username) }
    var newPassword by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    // Background animation
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

        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFCC0000), Color(0xFF990000), Color.Black),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .alpha(0.8f)
        )

        // Animated diagonal stripes
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(80.dp)
                    .offset(y = (index * 200 - offset).dp)
                    .rotate(-45f)
                    .background(Color.Red.copy(alpha = 0.08f))
            )
        }
        BackButton(onClick = onBack)
        Column(
            modifier = Modifier.fillMaxSize().padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "PROFILE",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = LocalTextStyle.current.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )

            Spacer(Modifier.height(40.dp))

            // Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, Color(0xFFE60012), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "ACCOUNT DETAILS",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.height(24.dp))

                    // Username field
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("USERNAME", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE60012),
                            unfocusedBorderColor = Color(0xFF444444),
                            cursorColor = Color(0xFFE60012),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("NEW PASSWORD (optional)", fontWeight = FontWeight.Bold) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE60012),
                            unfocusedBorderColor = Color(0xFF444444),
                            cursorColor = Color(0xFFE60012),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(24.dp))


                    // UPDATE BUTTON
                    Button(
                        onClick = { /* update */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = if (loading) "UPDATING..." else "UPDATE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // DELETE BUTTON
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "DELETE ACCOUNT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // LOGOUT BUTTON
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "LOGOUT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }



                    if (message.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(message, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))


        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "DELETE ACCOUNT?",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Akun akan dihapus permanen. Lanjutkan?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleteAccount(username, onLogout)   // 🔥 Panggil fungsi delete
                    }
                ) {
                    Text("DELETE", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

}
