package com.riccodex.verse

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class VerseWidget : AppWidgetProvider() {
    override fun onEnabled(c: Context) { Verses.schedule(c) }

    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) {
        for (id in ids) m.updateAppWidget(id, views(c))
    }

    override fun onReceive(c: Context, i: Intent) {
        super.onReceive(c, i)
        if (i.action == Verses.ACTION_NEW) Verses.refresh(c)
    }

    companion object {
        fun views(c: Context): RemoteViews {
            val p = Verses.prefs(c)
            if (p.getString("text", null) == null) Verses.pick(c)
            val v = RemoteViews(c.packageName, R.layout.widget)
            v.setTextViewText(R.id.verse, "“" + p.getString("text", "") + "”")
            v.setTextViewText(R.id.ref, p.getString("ref", ""))
            v.setTextViewText(R.id.tr, Verses.translation(c))
            val tap = Intent(c, VerseWidget::class.java).setAction(Verses.ACTION_NEW)
            v.setOnClickPendingIntent(R.id.root,
                PendingIntent.getBroadcast(c, 0, tap, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            return v
        }
    }
}
