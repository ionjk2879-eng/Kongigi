package com.kongigi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kongigi.data.GifticonStore
import java.util.concurrent.TimeUnit

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val ctx = applicationContext
        val now = System.currentTimeMillis()
        val day = TimeUnit.DAYS.toMillis(1)

        val list = GifticonStore.load(ctx)
        val d7 = list.filter { !it.isUsed && it.expiryDate in (now + 6 * day)..(now + 7 * day) }
        val d1 = list.filter { !it.isUsed && it.expiryDate in now..(now + day) }

        if (d7.isEmpty() && d1.isEmpty()) return Result.success()

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel("kongigi", "콘지기 알림", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }

        d7.forEachIndexed { i, g ->
            nm.notify(1000 + i, NotificationCompat.Builder(ctx, "kongigi")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("기프티콘 만료 7일 전")
                .setContentText("${g.brand} ${g.productName}")
                .setAutoCancel(true)
                .build())
        }
        d1.forEachIndexed { i, g ->
            nm.notify(2000 + i, NotificationCompat.Builder(ctx, "kongigi")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("기프티콘 만료 1일 전!")
                .setContentText("${g.brand} ${g.productName} — 오늘 사용하세요")
                .setAutoCancel(true)
                .build())
        }

        return Result.success()
    }
}
