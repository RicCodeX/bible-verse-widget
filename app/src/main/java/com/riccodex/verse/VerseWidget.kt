package com.riccodex.verse

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class VerseWidget : AppWidgetProvider() {
    override fun onEnabled(c: Context) { Verses.schedule(c) }

    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) {
        for (id in ids) m.updateAppWidget(id, views(c))
    }

    companion object {
        fun views(c: Context): RemoteViews {
            val p = Verses.prefs(c)
            if (p.getString("text", null) == null) Verses.pick(c)
            val v = RemoteViews(c.packageName, R.layout.widget)
            v.setTextViewText(R.id.verse, "“" + p.getString("text", "") + "”")
            v.setTextViewText(R.id.ref, p.getString("ref", ""))
            v.setTextViewText(R.id.tr, Verses.translation(c))
            return v
        }
    }
}
