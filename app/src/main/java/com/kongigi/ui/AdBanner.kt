package com.kongigi.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// 테스트 광고 유닛 ID — 출시 전 실제 유닛 ID로 교체
private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun AdBanner() {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = TEST_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
