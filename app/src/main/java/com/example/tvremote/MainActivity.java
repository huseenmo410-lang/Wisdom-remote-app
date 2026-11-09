package com.example.tvremote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private TvNetworkService networkService;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        webView = new WebView(this);
        setContentView(webView);

        networkService = new TvNetworkService();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Binding Java class to HTML JavaScript context
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidNativeBridge");

        // Load UI from Assets folder
        webView.loadUrl("file:///android_asset/index.html");
    }

    // Bridge methods called directly from JS buttons in index.html
    public class WebAppInterface {
        @JavascriptInterface
        public void sendTvCommand(String ipAddress, String keyCommand, boolean isLegacyPort) {
            if (isLegacyPort) {
                // Send raw C++ TCP Socket packet for older legacy TV models (Port 55000)
                byte[] rawPayload = ("KEY_" + keyCommand).getBytes();
                boolean result = networkService.sendLegacyCommandNative(ipAddress, 55000, rawPayload);
                showToast(result ? "C++ Socket Sent: " + keyCommand : "C++ Socket Failed");
            } else {
                // Send Java WebSocket packet (Port 8001)
                networkService.sendWebSocketCommand(ipAddress, keyCommand);
                showToast("WebSocket Sent: " + keyCommand);
            }
        }
    }

    private void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}

