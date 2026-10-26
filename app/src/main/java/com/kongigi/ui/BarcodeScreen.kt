package com.kongigi.ui

import android.app.Activity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScreen(g: GifticonEntity, onBack: () -> Unit, onEdit: () -> Unit = {}) {
    val context = LocalContext.current

    // 계산대 앞에서 쓰는 화면 — 화면 밝기 최대로
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            Spacer(Modifier.height(16.dp))
        }
    }
}
