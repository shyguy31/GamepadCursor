package com.example.gamepadcursor

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOverlayPermission = findViewById<Button>(R.id.btn_request_overlay_permission)
        val btnAccessibilitySettings = findViewById<Button>(R.id.btn_open_accessibility_settings)

        btnOverlayPermission.setOnClickListener {
            requestOverlayPermission()
        }

        btnAccessibilitySettings.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(
                    this,
                    "Please enable 'Display over other apps' permission",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        Toast.makeText(
            this,
            "Enable 'GamepadCursor' in Accessibility Services",
            Toast.LENGTH_LONG
        ).show()
    }
}
