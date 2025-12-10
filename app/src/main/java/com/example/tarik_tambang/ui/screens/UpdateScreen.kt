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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.UserPrefs
import com.example.tarik_tambang.api.ApiClient
import com.example.tarik_tambang.api.UpdateProfileResponse
import com.example.tarik_tambang.ui.components.BackButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun UpdateScreen(
    currentUsername: String,
    onUpdateSuccess: (newUsername: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var newUsername by remember { mutableStateOf(currentUsername) }
    var newPassword by remember { mutableStateOf("") }
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
        // Gradient Background
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

        // Moving Stripes
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(80.dp)
                    .offset(y = (index * 200f - offset).dp)
                    .rotate(-45f)
                    .background(Color.Red.copy(alpha = 0.08f))
            )
        }
        
        BackButton(onClick = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "UPDATE PROFILE",
                fontSize = 42.sp,
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

            Spacer(Modifier.height(36.dp))

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
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("NEW USERNAME") },
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
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("NEW PASSWORD (optional)") },
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
                    
                    Button(
                        onClick = {
                            if (newUsername.isBlank()) {
                                message = "Username tidak boleh kosong"
                                return@Button
                            }

                            loading = true
                            message = ""

                            ApiClient.instance.updateProfile(
                                oldUsername = currentUsername,
                                newUsername = newUsername,
                                newPassword = if (newPassword.isBlank()) null else newPassword
                            ).enqueue(object : Callback<UpdateProfileResponse> {
                                override fun onResponse(
                                    call: Call<UpdateProfileResponse>,
                                    response: Response<UpdateProfileResponse>
                                ) {
                                    loading = false
                                    val res = response.body()

                                    if (res != null && res.success) {
                                        onUpdateSuccess(res.username!!)
                                    } else {
                                        message = res?.message ?: "Gagal memperbarui profil."
                                    }
                                }

                                override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {
                                    loading = false
                                    message = "Tidak dapat terhubung ke server."
                                }
                            })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = if (loading) "UPDATING..." else "CONFIRM UPDATE",
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateScreenPreview() {
    UpdateScreen(
        currentUsername = "AndroidDev",
        onUpdateSuccess = {},
        onBack = {}
    )
}
