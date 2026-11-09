package com.riccodex.verse

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object Verses {
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
    }

    // fallback: a new verse every 30 minutes even if the phone is never unlocked
    fun schedule(c: Context) {
        val wm = WorkManager.getInstance(c)
        wm.cancelUniqueWork("hourly")
        val req = PeriodicWorkRequestBuilder<RefreshWorker>(30, TimeUnit.MINUTES).build()
        wm.enqueueUniquePeriodicWork("every30", ExistingPeriodicWorkPolicy.KEEP, req)
    }
}

class RefreshWorker(c: Context, p: WorkerParameters) : Worker(c, p) {
    override fun doWork(): Result { Verses.refresh(applicationContext); return Result.success() }
}
