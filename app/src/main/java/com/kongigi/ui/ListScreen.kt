package com.kongigi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kongigi.GifticonViewModel
import com.kongigi.data.GifticonEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ListScreen(vm: GifticonViewModel, onAdd: () -> Unit, onBarcode: (GifticonEntity) -> Unit) {
    val tabs = listOf("사용 가능", "D-7 이내", "사용 완료")
    var tab by remember { mutableIntStateOf(0) }
    var toDelete by remember { mutableStateOf<GifticonEntity?>(null) }

    val now = System.currentTimeMillis()
    val soon = now + 7L * 24 * 60 * 60 * 1000
    val items = when (tab) {
        0 -> vm.all.filter { !it.isUsed && it.expiryDate > now }
        1 -> vm.all.filter { !it.isUsed && it.expiryDate in now..soon }
        else -> vm.all.filter { it.isUsed }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "추가") }
        },
        bottomBar = { AdBanner() }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                tabs.forEachIndexed { i, title ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(title) })
                }
            }
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("기프티콘이 없어요", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { g ->
                        GifticonCard(g, onClick = { onBarcode(g) }, onDelete = { toDelete = g })
                    }
                }
            }
        }
    }

    toDelete?.let { g ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("삭제") },
            text = { Text("${g.brand} ${g.productName}을(를) 삭제하시겠어요?") },
            confirmButton = {
                TextButton(onClick = { vm.delete(g); toDelete = null }) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun GifticonCard(g: GifticonEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val fmt = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREA) }
    val expiry = if (g.expiryDate > 0) fmt.format(Date(g.expiryDate)) else "날짜 없음"

    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(g.brand.ifBlank { "브랜드 없음" }, style = MaterialTheme.typography.titleMedium)
                Text(g.productName.ifBlank { "상품명 없음" }, style = MaterialTheme.typography.bodyMedium)
                Text("유효기간: $expiry", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
                if (g.remainingBalance != null) {
                    val date = SimpleDateFormat("MM.dd", Locale.KOREA)
                        .format(Date(g.balanceCheckedAt ?: 0))
                    Text(
                        "잔액 ${"%,d".format(g.remainingBalance)}원 · $date 기준",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B5E3B)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
