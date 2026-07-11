package com.gammasys.sgm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LinearLayout errorLayout;
    private ProgressBar progressBar;
    private TextView errorText;
    private SwipeRefreshLayout swipeRefresh;
    private SharedPreferences prefs;

    private static final String DEFAULT_IP = "192.168.0.113";
    private static final String DEFAULT_PORT = "3000";
    private static final int REQUEST_LOCATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Modo inmersivo: ocultar barra de estado y navegación
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
            new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("sgm_prefs", MODE_PRIVATE);

        webView = findViewById(R.id.webView);
        errorLayout = findViewById(R.id.errorLayout);
        progressBar = findViewById(R.id.progressBar);
        errorText = findViewById(R.id.errorText);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        Button btnRetry = findViewById(R.id.btnRetry);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnSettingsError = findViewById(R.id.btnSettingsError);

        btnRetry.setOnClickListener(v -> loadUrl());
        btnSettings.setOnClickListener(v -> openSettings());
        btnSettingsError.setOnClickListener(v -> openSettings());

        // Pull-to-refresh: recargar WebView al arrastrar hacia abajo
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            webView.reload();
        });

        setupWebView();

        // Pedir permiso de ubicación para detectar WiFi (Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                loadUrl();
            }
        } else {
            loadUrl();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    showError("No se pudo conectar al servidor.\nVerificá la IP y la conexión de red.");
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
    }

    private void loadUrl() {
        if (!isNetworkAvailable()) {
            showError("Sin conexión a la red.\nVerificá el WiFi o la red local.");
            return;
        }

        String ip = getIpForCurrentNetwork();
        String port = prefs.getString("server_port", DEFAULT_PORT);
        String protocol = prefs.getString("server_protocol", "http");
        String url = protocol + "://" + ip + ":" + port;

        webView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        webView.loadUrl(url);
    }

    private void showError(String message) {
        webView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorText.setText(message);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * Obtiene la IP guardada para la red WiFi actual.
     * Si no hay IP guardada para esta red, usa la IP global o el default.
     */
    private String getIpForCurrentNetwork() {
        String ssid = getCurrentSsid();

        // Buscar IP guardada para este SSID
        if (ssid != null) {
            try {
                String ssidMapJson = prefs.getString("ssid_ips", "{}");
                JSONObject ssidMap = new JSONObject(ssidMapJson);
                if (ssidMap.has(ssid)) {
                    return ssidMap.getString(ssid);
                }
            } catch (JSONException e) {
                // Mapa corrupto, usar default
            }
        }

        // Fallback: IP global guardada o default
        return prefs.getString("server_ip", DEFAULT_IP);
    }

    /**
     * Detecta el SSID de la red WiFi conectada.
     * Requiere permiso ACCESS_FINE_LOCATION en Android 8.1+.
     * Retorna null si no se puede determinar.
     */
    @SuppressLint("MissingPermission")
    private String getCurrentSsid() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wm == null) return null;
            WifiInfo info = wm.getConnectionInfo();
            if (info == null) return null;
            String ssid = info.getSSID();
            if (ssid == null || ssid.equals("<unknown ssid>") || ssid.equals("0x")) {
                return null;
            }
            // Quitar comillas envolventes
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            return ssid;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            loadUrl(); // Cargar con o sin permiso
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar si se volvió de settings
        loadUrl();
    }
}