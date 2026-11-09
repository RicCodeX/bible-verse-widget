package com.riccodex.verse

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder

// Listens for phone unlock and gives the widget a new verse
class UnlockService : Service() {
    private val rx = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) { Verses.refresh(c) }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("svc", "Verse", NotificationManager.IMPORTANCE_MIN))
        val n = Notification.Builder(this, "svc")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Verse").build()
        startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        registerReceiver(rx, IntentFilter(Intent.ACTION_USER_PRESENT))
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int) = START_STICKY
    override fun onBind(i: Intent?): IBinder? = null
    override fun onDestroy() { unregisterReceiver(rx); super.onDestroy() }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        try { c.startForegroundService(Intent(c, UnlockService::class.java)) } catch (e: Exception) {}
    }
}
