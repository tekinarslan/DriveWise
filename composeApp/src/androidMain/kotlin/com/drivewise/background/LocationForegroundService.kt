package com.drivewise.background

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.drivewise.app.db.AppDatabase
import com.drivewise.tracking.RawGpsSample
import com.google.android.gms.location.*
import org.koin.android.ext.android.inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt
import com.drivewise.app.R

class LocationForegroundService : Service() {

    private val db: AppDatabase by inject()

    private lateinit var fused: FusedLocationProviderClient
    private var callback: LocationCallback? = null

    private var lessonId: String = ""
    private var lastSavedAtMs: Long = 0L
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    // notification live data
    private val nm by lazy { getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
    private var startedAtMs: Long = 0L
    private var lastSpeedKmh: Double = 0.0
    private var pointsSaved: Int = 0

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        android.util.Log.e("DriveWise-NOTIF", "🔥 SERVICE STARTED intent=$intent")

        if (action == ACTION_STOP) {
            stopUpdates()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // start (restart'larda intent null gelebilir)
        val incomingLessonId = intent?.getStringExtra(EXTRA_LESSON_ID)
        if (!incomingLessonId.isNullOrBlank()) {
            lessonId = incomingLessonId
        } else if (lessonId.isBlank()) {
            lessonId = "lesson_unknown"
        }

        // init session timers (only first start)
        if (startedAtMs == 0L) startedAtMs = System.currentTimeMillis()
        android.util.Log.i("DriveWise NOTIF", "Service onStartCommand -> startForeground")

        // First foreground notification
        startForeground(
            NOTIF_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("TEST")
                .setContentText("Foreground test")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .build()
        )
        /*startForeground(
            NOTIF_ID,
            buildNotification(
                speedKmh = lastSpeedKmh,
                elapsedSec = elapsedSec(),
                points = pointsSaved
            )
        )*/

        startUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        stopUpdates()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ---- Location updates ----

    @SuppressLint("MissingPermission")
    private fun startUpdates() {
        if (callback != null) return

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L
        ).setMinUpdateIntervalMillis(1000L)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return

                val speedKmh = (loc.speed * 3.6).coerceAtLeast(0.0)
                val bearing = if (loc.hasBearing()) loc.bearing.toDouble() else null

                val sample = RawGpsSample(
                    lat = loc.latitude,
                    lon = loc.longitude,
                    speedKmh = speedKmh,
                    bearingDeg = bearing,
                    timestampMs = loc.time
                )

                // live values (even if not saved)
                lastSpeedKmh = speedKmh

                if (shouldSave(sample)) {
                    insertDb(sample)
                    pointsSaved += 1
                }

                // ✅ canlı notification (her callback'te güncellemek istersen burası iyi)
                updateNotification()
            }
        }

        fused.requestLocationUpdates(request, callback!!, Looper.getMainLooper())
    }

    private fun stopUpdates() {
        callback?.let { fused.removeLocationUpdates(it) }
        callback = null
    }

    // ---- Sampling + DB ----

    private fun shouldSave(s: RawGpsSample): Boolean {
        val now = s.timestampMs

        // 1) en az 1000ms aralık
        if (now - lastSavedAtMs < 1000L) return false

        // 2) ya 3 sn geçti, ya 10m hareket etti
        val timeGate = (now - lastSavedAtMs) >= 3000L

        val movedGate = run {
            val la = lastLat;
            val lo = lastLon
            if (la == null || lo == null) true
            else {
                val meters = approxMeters(la, lo, s.lat, s.lon)
                meters >= 10.0
            }
        }

        return timeGate || movedGate
    }

    private fun insertDb(s: RawGpsSample) {
        db.trackpointQueries.createPoint(
            lesson_id = lessonId,
            lat = s.lat,
            lon = s.lon,
            speed_kmh = s.speedKmh,
            bearing_deg = s.bearingDeg,
            timestamp_ms = s.timestampMs
        )

        lastSavedAtMs = s.timestampMs
        lastLat = s.lat
        lastLon = s.lon

        android.util.Log.i(
            "DriveWise",
            "DB insert: lesson=$lessonId lat=${s.lat} lon=${s.lon} speed=${s.speedKmh} ts=${s.timestampMs}"
        )
    }

    private fun approxMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = (lat2 - lat1) * 111_111.0
        val dLon = (lon2 - lon1) * 111_111.0 * cos(lat1 * PI / 180.0)
        return sqrt(dLat * dLat + dLon * dLon)
    }

    // ---- Notification ----

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "DriveWise Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Background drive tracking"
                setShowBadge(false)
            }
            nm.createNotificationChannel(ch)
        }
    }

    private fun elapsedSec(): Int =
        if (startedAtMs == 0L) 0 else ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
            .coerceAtLeast(0)

    private fun updateNotification() {
        nm.notify(
            NOTIF_ID,
            buildNotification(
                speedKmh = lastSpeedKmh,
                elapsedSec = elapsedSec(),
                points = pointsSaved
            )
        )
    }

    private fun buildNotification(speedKmh: Double, elapsedSec: Int, points: Int): Notification {

        val stopIntent = Intent(this, LocationForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // open app on tap
        val openIntent = packageManager.getLaunchIntentForPackage(packageName)
        val openPending = PendingIntent.getActivity(
            this, 2, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val speedText = "${speedKmh.round1()} km/h"
        val timeText = formatHms(elapsedSec)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DriveWise • Session running")
            .setContentText("Speed: $speedText • Time: $timeText")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Speed: $speedText\nTime: $timeText\nPoints saved: $points"
                )
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, "Stop", stopPending)
            .build()
    }

    private fun Double.round1(): String = ((this * 10).toInt() / 10.0).toString()

    private fun formatHms(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        fun p2(v: Int) = if (v < 10) "0$v" else v.toString()
        return "${p2(h)}:${p2(m)}:${p2(s)}"
    }

    companion object {
        const val ACTION_START = "com.drivewise.START"
        const val ACTION_STOP = "com.drivewise.STOP"
        const val EXTRA_LESSON_ID = "lesson_id"

        private const val CHANNEL_ID = "drivewise_tracking"
        private const val NOTIF_ID = 1001
    }
}
