package com.example.guinevereautofree

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val captureRequest = 501
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
        }
        status = TextView(this).apply {
            textSize = 16f
            text = "GUINEVERE AUTO FREE\n\nPrototype: deteksi tulisan FREE pada kartu hero toko."
        }
        val accessibility = Button(this).apply {
            text = "1. Aktifkan izin klik (Accessibility)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        val start = Button(this).apply {
            text = "2. Mulai scan layar"
            setOnClickListener { requestCapture() }
        }
        val stop = Button(this).apply {
            text = "STOP scan"
            setOnClickListener { stopService(Intent(this@MainActivity, FreeScanService::class.java)); status.text = "Scan dihentikan." }
        }
        layout.addView(status)
        layout.addView(accessibility)
        layout.addView(start)
        layout.addView(stop)
        setContentView(layout)
    }

    private fun requestCapture() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(manager.createScreenCaptureIntent(), captureRequest)
    }

    @Deprecated("Deprecated by platform API; retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == captureRequest && resultCode == Activity.RESULT_OK && data != null) {
            val intent = Intent(this, FreeScanService::class.java).apply {
                putExtra(FreeScanService.EXTRA_RESULT_CODE, resultCode)
                putExtra(FreeScanService.EXTRA_RESULT_DATA, data)
            }
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent) else startService(intent)
            status.text = "Screen scan aktif. Buka MCGG; STOP untuk menghentikan."
        }
    }
}
