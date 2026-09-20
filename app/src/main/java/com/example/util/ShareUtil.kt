package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

object ShareUtil {

    fun copyToClipboard(context: Context, text: String, label: String = "Randomly Result"): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard?.setPrimaryClip(clip)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shareText(context: Context, title: String, content: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "$title\n\n$content\n\nGenerated with Randomly")
            }
            val chooser = Intent.createChooser(shareIntent, "Share Random Result")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: Exception) {}
    }
}
