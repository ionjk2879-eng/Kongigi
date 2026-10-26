package com.kongigi.ui

import android.content.Context
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
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.kongigi.GifticonViewModel
import com.kongigi.data.GifticonEntity
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen(vm: GifticonViewModel, gifticon: GifticonEntity, onBack: () -> Unit) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var balanceText by remember { mutableStateOf(gifticon.remainingBalance?.toString() ?: "") }
    var recognizing by remember { mutableStateOf(false) }

    val cameraFile = remember {
        File(context.filesDir, "temp").also { it.mkdirs() }.let { File(it, "camera_balance.jpg") }
    }
    val cameraUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", cameraFile)
    }

    fun runOcr(uri: Uri) {
        recognizing = true
        val image = InputImage.fromFilePath(context, uri)
        TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            .process(image)
            .addOnSuccessListener { balanceText = parseBalance(it.text) }
            .addOnCompleteListener { recognizing = false }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val localUri = copyBalanceScreenshot(context, uri)
        imageUri = localUri
        runOcr(localUri)
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = Uri.fromFile(cameraFile)
            runOcr(Uri.fromFile(cameraFile))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("잔액 등록") },
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
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(gifticon.brand, style = MaterialTheme.typography.titleMedium)
                    Text(gifticon.productName, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { camera.launch(cameraUri) },
                    modifier = Modifier.weight(1f)
                ) { Text("카메라 촬영") }
                OutlinedButton(
                    onClick = { picker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) { Text("갤러리 선택") }
            }

            imageUri?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }

            if (recognizing) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("잔액 인식 중...", style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it.filter { c -> c.isDigit() } },
                label = { Text("잔액 (원)") },
                suffix = { Text("원") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !recognizing
            )

            Button(
                onClick = {
                    val b = balanceText.toIntOrNull()
                    if (b != null) vm.updateBalance(gifticon.id, b, System.currentTimeMillis())
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !recognizing && balanceText.isNotBlank()
            ) {
                Text("저장")
            }
        }
    }
}

private fun copyBalanceScreenshot(context: Context, uri: Uri): Uri {
    val dir = File(context.filesDir, "temp").apply { mkdirs() }
    val file = File(dir, "balance_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { it.copyTo(file.outputStream()) }
    return Uri.fromFile(file)
}

private fun parseBalance(text: String): String {
    val keywords = listOf("잔액", "잔여금액", "현재잔액", "남은금액", "잔여")
    for (keyword in keywords) {
        val idx = text.indexOf(keyword)
        if (idx >= 0) {
            val after = text.substring(idx + keyword.length).take(30)
            val match = Regex("""(\d[\d,]+)""").find(after)
            if (match != null) return match.value.replace(",", "")
        }
    }
    return ""
}
