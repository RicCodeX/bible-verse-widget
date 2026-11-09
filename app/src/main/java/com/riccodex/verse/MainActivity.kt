package com.riccodex.verse

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var verse: TextView
    private lateinit var ref: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val d = resources.displayMetrics.density
        val font = resources.getFont(R.font.adventor)
        fun dp(x: Int) = (x * d).toInt()
        fun tv(size: Float) = TextView(this).apply { textSize = size; typeface = font }
        val p = Verses.prefs(this)

        verse = tv(22f).apply { setPadding(0, dp(28), 0, dp(8)) }
        ref = tv(14f).apply { alpha = 0.6f }

        val group = RadioGroup(this)
        group.orientation = RadioGroup.HORIZONTAL
        for (t in listOf("KJV", "WEB")) {
            val rb = RadioButton(this)
            rb.text = t; rb.typeface = font; rb.id = View.generateViewId()
            rb.isChecked = Verses.translation(this) == t
            group.addView(rb)
        }
        group.setOnCheckedChangeListener { g, id ->
            p.edit().putString("tr", g.findViewById<RadioButton>(id).text.toString()).apply()
            Verses.refresh(this); show()
        }

        val next = Button(this)
        next.text = "New verse"; next.typeface = font
        next.setOnClickListener { Verses.refresh(this); show() }

        val sw = Switch(this)
        sw.text = "Show verse on lock screen"; sw.typeface = font
        sw.isChecked = p.getBoolean("lock", false)
        sw.setOnCheckedChangeListener { _, on ->
            p.edit().putBoolean("lock", on).apply()
            if (on && Build.VERSION.SDK_INT >= 33)
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            Verses.notifyLock(this)
        }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(24), dp(56), dp(24), dp(24))
        listOf(group, verse, ref, next, sw).forEach { root.addView(it) }
        setContentView(root)
    }

    override fun onStart() {
        super.onStart()
        Verses.refresh(this)   // new verse every time the app opens
        Verses.schedule(this)  // then hourly
        show()
    }

    override fun onRequestPermissionsResult(r: Int, perms: Array<out String>, res: IntArray) {
        Verses.notifyLock(this)
    }

    private fun show() {
        val p = Verses.prefs(this)
        verse.text = "“" + p.getString("text", "") + "”"
        ref.text = p.getString("ref", "") + " · " + Verses.translation(this)
    }
}
