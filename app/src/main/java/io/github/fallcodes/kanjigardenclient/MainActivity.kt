package io.github.fallcodes.kanjigardenclient

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        buildLayout()
        configureWebView()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            webView.loadUrl(HOME_URL)
        }
    }

    private fun buildLayout() {
        webView = WebView(this).apply {
            // Match the site's background so startup goes straight from blank to the page.
            setBackgroundColor(getColor(R.color.background))
        }

        val root = FrameLayout(this).apply {
            addView(webView, MATCH_PARENT, MATCH_PARENT)
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                    WindowInsetsCompat.Type.displayCutout() or
                    WindowInsetsCompat.Type.ime()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        setContentView(root)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }

        // Mark the site's "download the app" prompt (the phone icon) as already dismissed
        // before its scripts run; the app it links to is no longer available.
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(webView, HIDE_APP_PROMPT_JS, setOf("https://$SITE_HOST"))
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                val isWeb = uri.scheme == "http" || uri.scheme == "https"
                if (isWeb && isOwnHost(uri)) return false
                // Redirects and script navigations (e.g. a login hop) stay in the app;
                // links the user taps to other sites open in the regular browser.
                if (isWeb && !request.hasGesture()) return false
                openExternally(uri)
                return true
            }

            override fun onPageFinished(view: WebView, url: String?) {
                CookieManager.getInstance().flush()
            }
        }

        // Without a WebChromeClient, WebView silently cancels alert()/confirm(), which breaks
        // the site's prompts (e.g. "Learn ahead?"). Show them as native dialogs instead.
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
                showJsDialog(message, result, withCancel = false)
                return true
            }

            override fun onJsConfirm(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
                showJsDialog(message, result, withCancel = true)
                return true
            }
        }
    }

    private fun showJsDialog(message: String?, result: JsResult, withCancel: Boolean) {
        val builder = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> result.confirm() }
            .setOnCancelListener { result.cancel() }
        if (withCancel) {
            builder.setNegativeButton(android.R.string.cancel) { _, _ -> result.cancel() }
        }
        builder.show()
    }

    private fun isOwnHost(uri: Uri): Boolean {
        val host = uri.host ?: return false
        return host == SITE_HOST || host.endsWith(".$SITE_HOST")
    }

    private fun openExternally(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            // Nothing can handle it; ignore.
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        // The site keeps its session in local storage; flush cookies too (e.g. payment pages)
        // so nothing is lost if the app is killed in the background.
        CookieManager.getInstance().flush()
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val SITE_HOST = "kanji.garden"
        private const val HOME_URL = "https://$SITE_HOST/"
        private const val HIDE_APP_PROMPT_JS =
            "try { localStorage.setItem('notifyNative', 'true'); } catch (e) {}"
    }
}
