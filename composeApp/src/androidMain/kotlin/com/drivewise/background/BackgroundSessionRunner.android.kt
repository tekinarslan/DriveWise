package com.drivewise.background

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.drivewise.tracking.RawGpsSample
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun createBackgroundSessionRunner(): BackgroundSessionRunner = AndroidServiceSessionRunner()

private class AndroidServiceSessionRunner : BackgroundSessionRunner, KoinComponent {
    private val context: Context by inject()
    private var running = false

    override fun start(lessonId: String, onSample: (RawGpsSample) -> Unit) {
        // Android tam background’ta DB insert service içinde; onSample burada kullanılmayacak.
        Log.e("DriveWise-NOTIF", "🔥 AndroidServiceSessionRunner.start() called")
        if (running) return
        running = true

        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_START
            putExtra(LocationForegroundService.EXTRA_LESSON_ID, lessonId)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop() {
        if (!running) return
        running = false

        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_STOP
        }
        context.startService(intent) // service stop action ile self-stop
    }

    override fun isRunning(): Boolean = running
}
