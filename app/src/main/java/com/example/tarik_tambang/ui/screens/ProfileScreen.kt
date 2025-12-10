package com.example.tarik_tambang.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarik_tambang.api.ApiClient
import com.example.tarik_tambang.api.UpdateProfileResponse
import com.example.tarik_tambang.ui.components.BackButton
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
                if (response.isSuccessful && response.body()?.success == true) {
                    onLogout()
                }
            }

            override fun onFailure(call: Call<UpdateProfileResponse>, t: Throwable) {}
        })
}

@Composable
fun ProfileScreen(
    username: String,
    wins: Int,
    onUpdateClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    ProfileInfo(label = "Username", value = username)
                    Spacer(Modifier.height(16.dp))
                    ProfileInfo(label = "Wins", value = wins.toString())

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("UPDATE", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("DELETE ACC", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60012)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("LOGOUT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("DELETE ACCOUNT?", fontWeight = FontWeight.Black, color = Color.White) },
            text = { Text("Akun akan dihapus permanen. Lanjutkan?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    deleteAccount(username, onLogout)
                }) {
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

@Composable
private fun ProfileInfo(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        username = "AndroidDev",
        wins = 100,
        onUpdateClick = {},
        onLogout = {},
        onBack = {}
    )
}
