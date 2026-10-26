package com.kongigi.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kongigi.GifticonViewModel
import com.kongigi.data.GifticonEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(vm: GifticonViewModel, gifticon: GifticonEntity, onBack: () -> Unit) {
    val context = LocalContext.current
    val fmt = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }

    var imageUri by remember { mutableStateOf<Uri?>(Uri.parse(gifticon.imageUri).takeIf { gifticon.imageUri.isNotEmpty() }) }
    var brand by remember { mutableStateOf(gifticon.brand) }
    var product by remember { mutableStateOf(gifticon.productName) }
    var expiryText by remember {
        mutableStateOf(if (gifticon.expiryDate > 0) fmt.format(Date(gifticon.expiryDate)) else "")
    }
    var barcodeVal by remember { mutableStateOf(gifticon.barcodeValue) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        imageUri = copyToInternalEdit(context, uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기프티콘 수정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            imageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
            OutlinedButton(onClick = { picker.launch("image/*") }, Modifier.fillMaxWidth()) {
                Text("사진 변경")
            }

            OutlinedTextField(
                value = brand, onValueChange = { brand = it },
                label = { Text("브랜드") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = product, onValueChange = { product = it },
                label = { Text("상품명") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = expiryText,
                onValueChange = { expiryText = formatExpiry(it) },
                label = { Text("유효기간 (YYYY.MM.DD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = barcodeVal, onValueChange = { barcodeVal = it },
                label = { Text("바코드 값") }, modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    vm.update(
                        gifticon.copy(
                            brand = brand.trim(),
                            productName = product.trim(),
                            expiryDate = parseExpiryMillisEdit(expiryText),
                            barcodeValue = barcodeVal.trim(),
                            imageUri = imageUri?.toString() ?: gifticon.imageUri
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = brand.isNotBlank()
            ) {
                Text("저장")
            }
        }
    }
}

private fun copyToInternalEdit(context: android.content.Context, uri: Uri): Uri {
    val dir = java.io.File(context.filesDir, "images").apply { mkdirs() }
    val file = java.io.File(dir, "${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { it.copyTo(file.outputStream()) }
    return Uri.fromFile(file)
}

private fun parseExpiryMillisEdit(text: String): Long {
    val parts = text.trim().split(".")
    if (parts.size < 3) return 0L
    return try {
        Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 23, 59, 59)
        }.timeInMillis
    } catch (e: Exception) { 0L }
}
