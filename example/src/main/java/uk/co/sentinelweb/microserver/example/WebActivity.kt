package uk.co.sentinelweb.microserver.example

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import butterknife.Bind
import butterknife.ButterKnife

const val EXTRA_URL=  "url"

class WebActivity : AppCompatActivity() {
    companion object {
        fun getIntent(c: Context, url: String): Intent {
            val intent = Intent(c, WebActivity::class.java)
            intent.putExtra(EXTRA_URL, url);
            return intent
        }
    }

    @Bind(R.id.webview)
    lateinit var webview:WebView

    @Bind(R.id.progress)
    lateinit var progress:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        ButterKnife.bind(this)
        webview.setWebViewClient(Client())
    }

    override fun onStart() {
        super.onStart()
        val url = getIntent().getStringExtra(EXTRA_URL)
        webview.loadUrl(url)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webview.canGoBack()) {
                webview.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    inner class Client : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progress.visibility=View.VISIBLE;
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            progress.visibility=View.GONE;
        }
    }

}
