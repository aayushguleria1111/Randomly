package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
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
            .width(130.dp)
            .aspectRatio(0.7f)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Left Corner
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = card.rank.symbol,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = card.suit.color
                )
                Text(
                    text = card.suit.symbol,
                    fontSize = 16.sp,
                    color = card.suit.color
                )
            }

            // Center Suit Symbol
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.suit.symbol,
                    fontSize = 44.sp,
                    color = card.suit.color
                )
            }

            // Bottom Right Corner (Inverted)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rotate(180f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.symbol,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = card.suit.color
                )
                Text(
                    text = card.suit.symbol,
                    fontSize = 16.sp,
                    color = card.suit.color
                )
            }
        }
    }
}
