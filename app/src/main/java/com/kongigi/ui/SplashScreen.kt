package com.kongigi.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kongigi.R
import kotlinx.coroutines.delay

private val splashGreen = Color(0xFF1B5E3B)
private val splashYellow = Color(0xFFF5A623)

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1800)
        onFinish()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(splashGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(splashYellow),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.ic_kongigi_full,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(160.dp)
                        .padding(20.dp)
                )
            }
            Text(
                "콘지기",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "기프티콘을 스마트하게 관리하세요",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp
            )
        }
    }
}
