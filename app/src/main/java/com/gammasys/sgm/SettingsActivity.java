package com.gammasys.sgm;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    private EditText editIp, editPort;
    private Switch switchProtocol;
    private TextView textSsid;
    private SharedPreferences prefs;
    private String currentSsid;

    private static final String DEFAULT_IP = "192.168.0.113";
    private static final String DEFAULT_PORT = "3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("sgm_prefs", MODE_PRIVATE);

        editIp = findViewById(R.id.editIp);
        editPort = findViewById(R.id.editPort);
        switchProtocol = findViewById(R.id.switchProtocol);
        textSsid = findViewById(R.id.textSsid);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDefault = findViewById(R.id.btnDefault);

        // Detectar red WiFi actual
        currentSsid = getCurrentSsid();
        if (currentSsid != null) {
            textSsid.setText("📡 Red: " + currentSsid);
        } else {
            textSsid.setText("📡 Sin WiFi detectado");
        }

        // Cargar IP guardada para esta red (o global)
        String ip = getIpForSsid(currentSsid);
        editIp.setText(ip);
        editPort.setText(prefs.getString("server_port", DEFAULT_PORT));
        switchProtocol.setChecked(prefs.getString("server_protocol", "http").equals("https"));

        btnSave.setOnClickListener(v -> {
            String newIp = editIp.getText().toString().trim();
            String port = editPort.getText().toString().trim();

            if (newIp.isEmpty()) {
                editIp.setError("Ingrese la IP del servidor");
                return;
            }
            if (port.isEmpty()) {
                editPort.setError("Ingrese el puerto");
                return;
            }

            String protocol = switchProtocol.isChecked() ? "https" : "http";

            // Guardar IP para esta red WiFi específica
            if (currentSsid != null) {
                saveIpForSsid(currentSsid, newIp);
            }

            // También guardar como global
            prefs.edit()
                .putString("server_ip", newIp)
                .putString("server_port", port)
                .putString("server_protocol", protocol)
                .apply();

            Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnDefault.setOnClickListener(v -> {
            editIp.setText(DEFAULT_IP);
            editPort.setText(DEFAULT_PORT);
            switchProtocol.setChecked(false);
        });
    }

    private String getIpForSsid(String ssid) {
        if (ssid != null) {
            try {
                String mapJson = prefs.getString("ssid_ips", "{}");
                JSONObject map = new JSONObject(mapJson);
                if (map.has(ssid)) {
                    return map.getString(ssid);
                }
            } catch (JSONException e) { /* usar fallback */ }
        }
        return prefs.getString("server_ip", DEFAULT_IP);
    }

    private void saveIpForSsid(String ssid, String ip) {
        try {
            String mapJson = prefs.getString("ssid_ips", "{}");
            JSONObject map = new JSONObject(mapJson);
            map.put(ssid, ip);
            prefs.edit().putString("ssid_ips", map.toString()).apply();
        } catch (JSONException e) {
            // Si falla, al menos queda la global
        }
    }

    @SuppressLint("MissingPermission")
    private String getCurrentSsid() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wm == null) return null;
            WifiInfo info = wm.getConnectionInfo();
            if (info == null) return null;
            String ssid = info.getSSID();
            if (ssid == null || ssid.equals("<unknown ssid>") || ssid.equals("0x")) return null;
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            return ssid;
        } catch (Exception e) {
            return null;
        }
    }
}