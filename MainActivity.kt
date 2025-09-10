import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.content.Intent
import android.speech.RecognizerIntent
import android.net.Uri
import android.os.Environment
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val SPEECH_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        // Tambahkan JavaScript Interface
        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        webView.webViewClient = WebViewClient()
        // Muat file HTML dari folder 'assets'
        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val spokenText: String? = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )?.get(0)
            if (spokenText != null) {
                // Panggil fungsi JS untuk mengirimkan teks kembali
                webView.evaluateJavascript(
                    "window.onSpeechResult('$spokenText');", null
                )
            }
        }
    }

    class WebAppInterface(private val mContext: Context) {
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
                Toast.makeText(mContext, "File berhasil disimpan di ${file.path}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(mContext, "Gagal menyimpan file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        @JavascriptInterface
        fun startVoiceToText() {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            }
            if (intent.resolveActivity(mContext.packageManager) != null) {
                (mContext as? MainActivity)?.startActivityForResult(intent, (mContext as? MainActivity)?.SPEECH_REQUEST_CODE ?: 100)
            } else {
                Toast.makeText(mContext, "Pengenalan suara tidak tersedia di perangkat ini.", Toast.LENGTH_SHORT).show()
            }
        }
        
        @JavascriptInterface
        fun readText(text: String) {
            // Placeholder for Text-to-Speech implementation
            // You will need to set up TextToSpeech engine in Android
        }
        
        @JavascriptInterface
        fun backupData() {
            // Placeholder for data backup functionality
            // Implement Firebase data export to a local file
        }
    }
}
