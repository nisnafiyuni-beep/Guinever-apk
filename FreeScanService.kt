package com.example.guinevereautofree

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

class FreeScanService : Service() {
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: VirtualDisplay? = null
    private var worker: HandlerThread? = null
    private var workerHandler: Handler? = null
    private val processing = AtomicBoolean(false)
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    private var lastTapAt = 0L
    private var lastTargetKey = ""
    private var previousOcrText = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(7, notification())
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED) ?: Activity.RESULT_CANCELED
        val data = if (Build.VERSION.SDK_INT >= 33) intent?.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                   else @Suppress("DEPRECATION") intent?.getParcelableExtra(EXTRA_RESULT_DATA)
        if (resultCode == Activity.RESULT_OK && data != null && projection == null) startProjection(resultCode, data)
        return START_NOT_STICKY
    }

    private fun startProjection(code: Int, data: Intent) {
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mgr.getMediaProjection(code, data)
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi
        worker = HandlerThread("free-ocr").also { it.start() }
        workerHandler = Handler(worker!!.looper)
        reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        reader!!.setOnImageAvailableListener({ source ->
            val image = source.acquireLatestImage() ?: return@setOnImageAvailableListener
            if (!processing.compareAndSet(false, true)) { image.close(); return@setOnImageAvailableListener }
            try {
                val plane = image.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * width
                val padded = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                padded.copyPixelsFromBuffer(buffer)
                val bitmap = Bitmap.createBitmap(padded, 0, 0, width, height)
                padded.recycle()
                image.close()
                scan(bitmap)
            } catch (_: Exception) {
                image.close(); processing.set(false)
            }
        }, workerHandler)
        display = projection!!.createVirtualDisplay("GuinevereFreeScan", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader!!.surface, null, workerHandler)
    }

    private fun scan(bitmap: Bitmap) {
        val input = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(input)
            .addOnSuccessListener { result ->
                val now = System.currentTimeMillis()
                val currentText = result.text.trim()
                if (now - lastTapAt < 500) return@addOnSuccessListener
                // Wait for the shop to visibly update after each selection before choosing another card.
                if (lastTapAt > 0L && currentText == previousOcrText) return@addOnSuccessListener
                val free = result.textBlocks.flatMap { it.lines }
                    .filter { it.text.trim().equals("FREE", ignoreCase = true) }
                    .mapNotNull { line -> line.boundingBox?.let { it } }
                    .sortedBy { it.centerX() }
                if (free.isNotEmpty()) {
                    val target = free.first()
                    val key = "${target.centerX() / 30}_${target.centerY() / 30}"
                    if (key != lastTargetKey) {
                        // Tap the hero artwork above the FREE label, not the label or the GRATIS x1 button.
                        val x = target.exactCenterX()
                        val y = (target.centerY() - target.height() * 6.5f).coerceAtLeast(20f)
                        lastTapAt = now
                        lastTargetKey = key
                        previousOcrText = currentText
                        TapAccessibilityService.instance?.tap(x, y)
                    }
                } else {
                    lastTargetKey = ""
                    previousOcrText = currentText
                }
            }
            .addOnCompleteListener {
                bitmap.recycle()
                processing.set(false)
            }
    }

    private fun notification(): Notification = NotificationCompat.Builder(this, CHANNEL)
        .setContentTitle("Guinevere Auto Free")
        .setContentText("Memindai label FREE di toko MCGG. Tekan STOP di aplikasi untuk berhenti.")
        .setSmallIcon(android.R.drawable.ic_menu_search)
        .setOngoing(true).build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL, "Guinevere Auto Free", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        display?.release(); reader?.close(); projection?.stop(); worker?.quitSafely()
        recognizer.close()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        private const val CHANNEL = "guinevere_scan"
    }
}
