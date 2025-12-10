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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.api.ApiClient
import com.example.tarik_tambang.api.LoginResponse
import com.example.tarik_tambang.UserPrefs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable





@Composable

fun LoginScreen(onLogin: (String) -> Unit,onRegister: () -> Unit) {
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

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

        // Moving diagonal lines
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Title
            Text(
                text = "TARIK TAMBANG",
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


            Spacer(Modifier.height(48.dp))

            // Login Card
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0A0A0A))),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, Color(0xFFE60012), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "LOGIN",
                        fontSize = 30.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(Modifier.height(32.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.trim() },
                        label = { Text("USERNAME", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE60012),
                            unfocusedBorderColor = Color(0xFF444444),
                            cursorColor = Color(0xFFE60012),

                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,

                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,

                            focusedLeadingIconColor = Color.White,
                            unfocusedLeadingIconColor = Color.White
                        )
                    )


                    Spacer(Modifier.height(16.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { newValue -> password = newValue.trim() },
                        label = { Text("PASSWORD", fontWeight = FontWeight.Bold) },
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

                    // LOGIN BUTTON
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                message = "Username dan password tidak boleh kosong"
                                return@Button
                            }

                            loading = true
                            message = ""

                            ApiClient.instance.login(username, password)
                                .enqueue(object : Callback<LoginResponse> {

                                    override fun onResponse(
                                        call: Call<LoginResponse>,
                                        response: Response<LoginResponse>
                                    ) {
                                        loading = false
                                        val res = response.body()

                                        if (res != null && res.success) {

                                            val userNameFromApi = res.user?.username ?: ""

                                            // Simpan ke SharedPreferences
                                            UserPrefs.saveName(context, userNameFromApi)

                                            // Lanjut ke halaman berikut
                                            onLogin(userNameFromApi)

                                        } else {
                                            message = res?.message ?: "Login gagal"
                                        }
                                    }

                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                        loading = false
                                        message = t.message ?: "Tidak dapat terhubung ke server"
                                    }

                                })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012))
                    ) {
                        Text(
                            text = if (loading) "Loading..." else "LOGIN",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Belum punya akun? REGISTER",
                        color = Color(0xFFE60012),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { onRegister() }
                    )

                    // Error Message
                    if (message.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(message, color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
