package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.RandomGenerators.PlayingCard

@Composable
fun PlayingCardView(
    card: PlayingCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(width = 86.dp, height = 124.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            // Top Left
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.symbol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = card.suit.color
                )
                Text(
                    text = card.suit.symbol,
                    fontSize = 12.sp,
                    color = card.suit.color
                )
            }

            // Center Symbol
            Text(
                text = card.suit.symbol,
                fontSize = 32.sp,
                color = card.suit.color,
                modifier = Modifier.align(Alignment.Center)
            )

            // Bottom Right
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.suit.symbol,
                    fontSize = 12.sp,
                    color = card.suit.color
                )
                Text(
                    text = card.rank.symbol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = card.suit.color
                )
            }
        }
    }
}
