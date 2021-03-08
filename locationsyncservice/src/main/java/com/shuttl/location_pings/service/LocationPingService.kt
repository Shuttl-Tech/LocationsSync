package com.shuttl.location_pings.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import com.shuttl.location_pings.callbacks.LocationPingServiceCallback
import com.shuttl.location_pings.config.components.LocationConfigs
import com.shuttl.location_pings.config.components.LocationsDB
import com.shuttl.location_pings.config.open_lib.getWakeLock
import com.shuttl.location_pings.config.open_lib.releaseSafely
import com.shuttl.location_pings.custom.notification
import com.shuttl.location_pings.data.repo.LocationRepo
import java.util.*
import java.util.concurrent.TimeUnit

class LocationPingService : Service() {

    private var configs: LocationConfigs = LocationConfigs()
    private var callback: LocationPingServiceCallback<Any>? = null
    private val customBinder = CustomBinder()
    private var minSyncMultiplier = 1

    private var wakeLock: PowerManager.WakeLock? = null

    private var timer: CountDownTimer? = null
    private var longTimer: Timer? = null
    private var timerTask: TimerTask? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("LPing", "Broadcast ${intent?.action}")

            context?.let {
                when (intent?.action) {
                    ACTION_ALARM -> {
                        Log.i("LPing", "Alarm")
                    }
                    else -> return
                }
            }
        }
    }

    private val ACTION_ALARM by lazy { "loc_save_alarm" }


    private fun pingLocations() {
        try {
            val locationsDao = LocationsDB.create(applicationContext)?.locationsDao()
            val locationRepo = LocationRepo(locationsDao)

            val locations = locationsDao?.getLimitedLocations(configs.batchSize)

            locations?.apply {
                val lastSaveTime = TimeUnit.MILLISECONDS.toMillis(last().time.toLong())
                val currentTime = System.currentTimeMillis() / 1000

                Log.e("Debug", "dateTime $lastSaveTime, currentTime $currentTime")

                if ((currentTime - lastSaveTime) > configs.inactivitySyncInterval) {
                    minSyncMultiplier = 3
                    sendMessage()
                    Log.e("Debug", "time to reset timer tasks and counter 3")
                } else if (minSyncMultiplier == 3) {
                    minSyncMultiplier = 1
                    sendMessage()
                    Log.e("Debug", "time to reset timer tasks and counter 1")
                }
            }

            locationRepo.syncLocations(
                configs.xApiKey
                    ?: "",
                configs.syncUrl ?: "",
                configs.batchSize,
                this.applicationContext,
                configs.canReuseLastLocation ?: false,
                callback
            )
        } catch (e: Exception) {
            Log.e("LocationsHelper", e.toString())
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        configs = intent?.getParcelableExtra("config") ?: LocationConfigs()
        startForeground(
            1,
            notification(
                this,
                "Updating trip details...",
                configs.smallIcon,
                intent?.getParcelableExtra("pendingIntent")
            )
        )
        return customBinder
    }

    override fun onCreate() {
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (configs.timeout <= 0) {
                longTimer?.cancel()
                timerTask?.cancel()
            } else timer?.cancel()
            if (configs.alarm == true) {
                cancelAlarm()
                unregisterReceiver(receiver)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals("STOP")) {
            wakeLock?.releaseSafely {}
            callback?.serviceStoppedManually()
            try {
                if (configs.timeout <= 0) {
                    longTimer?.cancel()
                    timerTask?.cancel()
                } else timer?.cancel()
                if (configs.alarm == true) {
                    cancelAlarm()
                    unregisterReceiver(receiver)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stopSelf()
        }
        configs = intent?.getParcelableExtra("config") ?: LocationConfigs()
        return START_STICKY
    }

    private fun stopWork() {
        timer?.cancel()
        longTimer?.cancel()
        timerTask?.cancel()
        timer = null
        timerTask = null
        longTimer = null
    }

    private fun work() {
        try {
            callback?.serviceStarted()
            if (configs.wakeLock == true) {
                wakeLock = this.getWakeLock()
                wakeLock?.acquire(200 * 60 * 1000L /*200 minutes*/)
            }
            if (configs.timeout <= 0) {
                if (timerTask == null) {
                    timerTask = object : TimerTask() {
                        override fun run() {
                            pingLocations()
                        }
                    }
                }
                if (longTimer == null) {
                    longTimer = Timer()
                }
                longTimer?.schedule(
                    timerTask,
                    0,
                    configs.minSyncInterval.toLong() / minSyncMultiplier
                )
            } else {
                if (timer == null) {
                    timer = object : CountDownTimer(
                        configs.timeout.toLong(),
                        configs.minSyncInterval.toLong() / minSyncMultiplier
                    ) {
                        override fun onTick(millisUntilFinished: Long) {
                            pingLocations()
                        }

                        override fun onFinish() {
                            callback?.serviceStopped()
                            stopForeground(true)
                        }
                    }
                }
                timer?.start()
            }
            if (configs.alarm == true) {
                registerReceiver(receiver, IntentFilter(ACTION_ALARM));
                scheduleAlarm()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCallbackAndWork(c: LocationPingServiceCallback<Any>?) {
        callback = c
        work()
    }

    inner class CustomBinder : Binder() {
        fun getService(): LocationPingService {
            return this@LocationPingService
        }
    }


    private fun getAlarmIntent(): PendingIntent {
        val i = Intent(ACTION_ALARM)
        i.setPackage(packageName)
        return PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun scheduleAlarm() {
        Log.i("LPing", "Scheduling at alarm ${Date(System.currentTimeMillis() + 30000)}")
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    getAlarmIntent()
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                (getSystemService(Context.ALARM_SERVICE) as AlarmManager).setExact(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    getAlarmIntent()
                )
            }
            else -> {
                (getSystemService(Context.ALARM_SERVICE) as AlarmManager).set(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 5000,
                    getAlarmIntent()
                )
            }
        }
    }

    private fun cancelAlarm() {
        (getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(getAlarmIntent())
    }

    private fun sendMessage() {
        val message = Message().apply {
            arg1 = 1001
        }
        mHandler.sendMessage(message)
    }

    private val mHandler = Handler(Looper.getMainLooper()) {
        if (it.arg1 == 1001) {
            stopWork()
            Handler().postDelayed({
                work()
            }, 5 * 60 * 1000)
        }
        return@Handler false
    }

}