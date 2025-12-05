package com.example.tarik_tambang.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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