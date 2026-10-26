package com.kongigi.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.kongigi.GifticonViewModel
import com.kongigi.data.GifticonEntity
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(vm: GifticonViewModel, onBack: () -> Unit) {
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var brand by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var expiryText by remember { mutableStateOf("") }
    var barcodeVal by remember { mutableStateOf("") }
    var recognizing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val localUri = copyToInternal(context, uri)
        imageUri = localUri
        recognizing = true

        val image = InputImage.fromFilePath(context, localUri)
        val barcodeScanner = BarcodeScanning.getClient()
        val textRecognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

        var detectedBarcode = ""
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                detectedBarcode = barcodes.firstOrNull()?.rawValue ?: ""
            }
            .addOnCompleteListener {
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val lines = text.lines().filter { it.isNotBlank() }
                        barcodeVal = detectedBarcode
                        brand = lines.firstOrNull() ?: ""
                        product = lines.getOrElse(1) { "" }
                        expiryText = parseExpiryText(text)
                        recognizing = false
                    }
                    .addOnFailureListener {
                        barcodeVal = detectedBarcode
                        recognizing = false
                    }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("기프티콘 추가") },
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
            OutlinedButton(onClick = { picker.launch("image/*") }, Modifier.fillMaxWidth()) {
                Text(if (imageUri == null) "갤러리에서 사진 선택" else "사진 다시 선택")
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
                Text("인식 중...", style = MaterialTheme.typography.bodySmall)
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
                    vm.insert(
                        GifticonEntity(
                            brand = brand.trim(),
                            productName = product.trim(),
                            expiryDate = parseExpiryMillis(expiryText),
                            barcodeValue = barcodeVal.trim(),
                            imageUri = imageUri?.toString() ?: ""
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !recognizing && brand.isNotBlank()
            ) {
                Text("저장")
            }
        }
    }
}

internal fun formatExpiry(input: String): String {
    val d = input.filter { it.isDigit() }.take(8)
    return buildString {
        d.forEachIndexed { i, c ->
            if (i == 4 || i == 6) append('.')
            append(c)
        }
    }
}

private fun copyToInternal(context: Context, uri: Uri): Uri {
    val dir = File(context.filesDir, "images").apply { mkdirs() }
    val file = File(dir, "${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { it.copyTo(file.outputStream()) }
    return Uri.fromFile(file)
}

private fun parseExpiryText(text: String): String {
    val regex = Regex("""(\d{4})[./년]\s*(\d{1,2})[./월]\s*(\d{1,2})""")
    val match = regex.find(text) ?: return ""
    val (y, m, d) = match.destructured
    return "$y.${m.padStart(2, '0')}.${d.padStart(2, '0')}"
}

private fun parseExpiryMillis(text: String): Long {
    val parts = text.trim().split(".")
    if (parts.size < 3) return 0L
    return try {
        Calendar.getInstance().apply {
            set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 23, 59, 59)
        }.timeInMillis
    } catch (e: Exception) { 0L }
}
