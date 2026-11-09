package com.riccodex.verse

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

object Verses {
    const val ACTION_NEW = "com.riccodex.verse.NEW"

    fun prefs(c: Context) = c.getSharedPreferences("verse", Context.MODE_PRIVATE)
    fun translation(c: Context) = prefs(c).getString("tr", "KJV") ?: "KJV"

    // pick a random verse from the whole Bible of the chosen translation
    fun pick(c: Context) {
        val lines = c.assets.open(translation(c).lowercase() + ".txt").bufferedReader().use { it.readLines() }
        val parts = lines.random().split('\t')
        prefs(c).edit().putString("ref", parts[0]).putString("text", parts[1]).apply()
    }

    fun refresh(c: Context) { pick(c); update(c) }

    fun update(c: Context) {
        val m = AppWidgetManager.getInstance(c)
        for (id in m.getAppWidgetIds(ComponentName(c, VerseWidget::class.java)))
            m.updateAppWidget(id, VerseWidget.views(c))
        notifyLock(c)
    }

    fun schedule(c: Context) {
        val req = PeriodicWorkRequestBuilder<RefreshWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(c).enqueueUniquePeriodicWork("hourly", ExistingPeriodicWorkPolicy.KEEP, req)
    }

    fun notifyLock(c: Context) {
        val nm = c.getSystemService(NotificationManager::class.java)
        val p = prefs(c)
        if (!p.getBoolean("lock", false)) { nm.cancel(1); return }
        val ch = NotificationChannel("verse", "Verse", NotificationManager.IMPORTANCE_LOW)
        ch.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        nm.createNotificationChannel(ch)
        val text = p.getString("text", "") ?: ""
        val open = PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(c, "verse")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle((p.getString("ref", "") ?: "") + " · " + translation(c))
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true).setOnlyAlertOnce(true).setContentIntent(open).build()
        nm.notify(1, n)
    }
}

class RefreshWorker(c: Context, p: WorkerParameters) : Worker(c, p) {
    override fun doWork(): Result { Verses.refresh(applicationContext); return Result.success() }
}
