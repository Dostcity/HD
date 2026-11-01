package com.destekbilisim.foldpenalti;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import java.util.concurrent.Executor;

/** Full-screen WebView shell for the Fold Penaltı game. Reports the fold (hinge) position to the page. */
public class MainActivity extends Activity {
    private WebView web;
    private WindowInfoTrackerCallbackAdapter tracker;
    private final Executor mainExecutor = r -> runOnUiThread(r);
    private final androidx.core.util.Consumer<WindowLayoutInfo> layoutListener = this::onLayoutInfo;
    private String lastHinge = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#060A14"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) { pushHinge(true); }
            @Override public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest req) {
                android.net.Uri u = req.getUrl();
                if ("file".equals(u.getScheme())) return false;
                try { startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true; // WhatsApp / tarayıcı linkleri dışarıda açılır
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");

        tracker = new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(this));
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(getWindow(), web);
        c.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        c.hide(WindowInsetsCompat.Type.systemBars());
    }

    @Override protected void onStart() {
        super.onStart();
        tracker.addWindowLayoutInfoListener(this, mainExecutor, layoutListener);
    }

    @Override protected void onStop() {
        super.onStop();
        tracker.removeWindowLayoutInfoListener(layoutListener);
    }

    @Override protected void onResume() { super.onResume(); web.onResume(); hideSystemBars(); }
    @Override protected void onPause() { super.onPause(); web.onPause(); }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private float hingeFraction = -1f;

    private void onLayoutInfo(WindowLayoutInfo info) {
        float frac = -1f;
        for (DisplayFeature f : info.getDisplayFeatures()) {
            if (f instanceof FoldingFeature) {
                FoldingFeature ff = (FoldingFeature) f;
                boolean tabletop = ff.getState() == FoldingFeature.State.HALF_OPENED
                        && ff.getOrientation() == FoldingFeature.Orientation.HORIZONTAL;
                if (tabletop) {
                    Rect b = ff.getBounds();
                    int h = web.getHeight() > 0 ? web.getHeight() : getResources().getDisplayMetrics().heightPixels;
                    frac = (b.top + b.height() / 2f) / h;
                }
            }
        }
        hingeFraction = frac;
        pushHinge(false);
    }

    private void pushHinge(boolean force) {
        String js = "window.__hinge&&window.__hinge(" + hingeFraction + ")";
        if (!force && js.equals(lastHinge)) return;
        lastHinge = js;
        web.evaluateJavascript(js, null);
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
