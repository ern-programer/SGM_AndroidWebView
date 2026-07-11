package com.gammasys.sgm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private EditText editIp, editPort;
    private SharedPreferences prefs;

    private static final String DEFAULT_IP = "192.168.0.113";
    private static final String DEFAULT_PORT = "3000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("sgm_prefs", MODE_PRIVATE);

        editIp = findViewById(R.id.editIp);
        editPort = findViewById(R.id.editPort);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDefault = findViewById(R.id.btnDefault);

        // Cargar valores actuales
        editIp.setText(prefs.getString("server_ip", DEFAULT_IP));
        editPort.setText(prefs.getString("server_port", DEFAULT_PORT));

        btnSave.setOnClickListener(v -> {
            String ip = editIp.getText().toString().trim();
            String port = editPort.getText().toString().trim();

            if (ip.isEmpty()) {
                editIp.setError("Ingrese la IP del servidor");
                return;
            }
            if (port.isEmpty()) {
                editPort.setError("Ingrese el puerto");
                return;
            }

            prefs.edit()
                .putString("server_ip", ip)
                .putString("server_port", port)
                .apply();

            Toast.makeText(this, "✅ Configuración guardada", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnDefault.setOnClickListener(v -> {
            editIp.setText(DEFAULT_IP);
            editPort.setText(DEFAULT_PORT);
        });
    }
}