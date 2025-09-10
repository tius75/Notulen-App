package com.your_package_name

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.webkit.JavascriptInterface
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import android.content.ClipData
import android.content.ClipboardManager

class WebAppInterface(private val mContext: Context) {

    private val activity = mContext as MainActivity

    @JavascriptInterface
    fun shareText(text: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        mContext.startActivity(Intent.createChooser(shareIntent, "Bagikan dengan"))
    }

    @JavascriptInterface
    fun saveFile(fileName: String, base64Data: String) {
        try {
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use {
                it.write(decodedBytes)
            }
            activity.runOnUiThread {
                Toast.makeText(mContext, "File berhasil disimpan di ${file.path}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            activity.runOnUiThread {
                Toast.makeText(mContext, "Gagal menyimpan file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    @JavascriptInterface
    fun startVoiceToText(targetId: String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Silakan bicara...")
        }
        if (intent.resolveActivity(mContext.packageManager) != null) {
            (mContext as? MainActivity)?.startActivityForResult(intent, (mContext as? MainActivity)?.SPEECH_REQUEST_CODE ?: 100)
        } else {
            activity.runOnUiThread {
                Toast.makeText(mContext, "Pengenalan suara tidak tersedia di perangkat ini.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @JavascriptInterface
    fun readText(text: String) {
        if (activity.textToSpeech.isSpeaking) {
            activity.textToSpeech.stop()
        }
        activity.textToSpeech.language = Locale("id", "ID")
        activity.textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    @JavascriptInterface
    fun backupData() {
        activity.runOnUiThread {
            Toast.makeText(mContext, "Fitur cadangan belum diimplementasikan.", Toast.LENGTH_SHORT).show()
        }
    }
    
    @JavascriptInterface
    fun copyToClipboard(text: String) {
        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        activity.runOnUiThread {
            Toast.makeText(mContext, "Teks disalin ke clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}
