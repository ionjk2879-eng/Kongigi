package com.kongigi.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kongigi.data.GifticonEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScreen(
    g: GifticonEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit = {},
    onBalanceRegister: () -> Unit = {}
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (context as Activity).window
        val orig = window.attributes.screenBrightness
        window.attributes = window.attributes.apply { screenBrightness = 1.0f }
        onDispose { window.attributes = window.attributes.apply { screenBrightness = orig } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${g.brand} · ${g.productName}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "수정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (g.imageUri.isNotEmpty()) {
                AsyncImage(
                    model = Uri.parse(g.imageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            if (g.barcodeValue.isNotEmpty()) {
                Text(g.barcodeValue, style = MaterialTheme.typography.headlineSmall)
            }

            // 잔액 표시
            if (g.remainingBalance != null) {
                val date = SimpleDateFormat("MM.dd", Locale.KOREA)
                    .format(Date(g.balanceCheckedAt ?: 0))
                Surface(
                    color = Color(0xFF1B5E3B).copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "잔액 ${"%,d".format(g.remainingBalance)}원 · $date 기준",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1B5E3B)
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val url = "https://www.google.com/search?q=${Uri.encode("${g.brand} 잔액 조회")}"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("잔액 조회")
                }
                Button(
                    onClick = onBalanceRegister,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E3B))
                ) {
                    Text("잔액 등록")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
