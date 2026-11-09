package com.riccodex.verse

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*

class MainActivity : Activity() {
    private lateinit var verse: TextView
    private lateinit var ref: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val d = resources.displayMetrics.density
        fun dp(x: Int) = (x * d).toInt()
        val p = Verses.prefs(this)

        verse = TextView(this)
        verse.textSize = 22f
        verse.setPadding(0, dp(28), 0, dp(8))
        ref = TextView(this)
        ref.textSize = 14f
        ref.alpha = 0.6f

        val group = RadioGroup(this)
        group.orientation = RadioGroup.HORIZONTAL
        for (t in listOf("KJV", "WEB")) {
            val rb = RadioButton(this)
            rb.text = t
            rb.id = View.generateViewId()
            rb.isChecked = Verses.translation(this) == t
            group.addView(rb)
        }
        group.setOnCheckedChangeListener { g, id ->
            p.edit().putString("tr", g.findViewById<RadioButton>(id).text.toString()).apply()
            Verses.refresh(this); show()
        }

        val next = Button(this)
        next.text = "New verse"
        next.setOnClickListener { Verses.refresh(this); show() }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(24), dp(56), dp(24), dp(24))
        listOf(group, verse, ref, next).forEach { root.addView(it) }
        setContentView(root)
    }

    override fun onStart() {
        super.onStart()
        Verses.refresh(this)   // new verse every time the app opens
        Verses.schedule(this)  // 30 minute fallback
        try { startForegroundService(Intent(this, UnlockService::class.java)) } catch (e: Exception) {}
        show()
    }

    private fun show() {
        val p = Verses.prefs(this)
        verse.text = "“" + p.getString("text", "") + "”"
        ref.text = p.getString("ref", "") + " · " + Verses.translation(this)
    }
}
