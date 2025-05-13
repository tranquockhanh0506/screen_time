package com.example.app_limiter

import android.app.*
import android.app.usage.*
import android.content.*
import android.content.pm.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import androidx.annotation.NonNull
import kotlinx.coroutines.*
import android.Manifest
import java.util.Calendar
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding


class AppLimiterPlugin: FlutterPlugin, MethodCallHandler,ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var activity: Activity? = null
    // Coroutine scope for background tasks
    var job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

    // Helper methods from MainActivity.kt
    private fun checkDrawOverlayPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(activity)
        } else {
            true
        }
    }

    private fun requestDrawOverlayPermission(activity: Activity, requestCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.packageName))
                activity.startActivityForResult(intent, requestCode)
                return false
            }
        }
        return true
    }

    private fun checkQueryAllPackagesPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(Manifest.permission.QUERY_ALL_PACKAGES)
        } else {
            true
        }
    }

    private fun requestQueryAllPackagesPermission(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (activity.checkSelfPermission(Manifest.permission.QUERY_ALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.QUERY_ALL_PACKAGES), 2)
                return false
            }
        }
        return true
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        context.startActivity(intent)
    }

    private fun isServiceRunning(serviceClassName: String, context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun setAlarm(hour: Int, minute: Int, second: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
        }
        val intent = Intent(context, BlockAppService::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, pendingIntentFlags)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    // Flutter plugin logic to handle method calls
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_limiter")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }

            "blockApp" -> {
                val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("Blocking", true).apply()
                val intent = Intent(context, BlockAppService::class.java)
                context.startService(intent)
                result.success(null)
            }

            "unblockApp" -> {
                val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("Blocking", false).apply()
                val intent = Intent(context, BlockAppService::class.java)
                context.stopService(intent)
                result.success(null)
            }

            "checkPermission" -> {
                val hasOverlayPermission = activity?.let { checkDrawOverlayPermission(it) } ?: false
                val hasQueryPermission = activity?.let { requestQueryAllPackagesPermission(it) } ?: false
                val hasUsageStatsPermission = context.let { hasUsageStatsPermission(it) }

                if (hasOverlayPermission && hasQueryPermission && hasUsageStatsPermission) {
                    result.success("approved")
                } else {
                    result.success("denied")
                }
            }

            "requestAuthorization" -> {
    val currentActivity = activity
    if (currentActivity == null) {
        result.error("NO_ACTIVITY", "Activity is null", null)
        return
    }

    if (!checkDrawOverlayPermission(currentActivity)) {
        requestDrawOverlayPermission(currentActivity, 1234)
    }

    if (!checkQueryAllPackagesPermission(context)) {
        requestQueryAllPackagesPermission(currentActivity)
    }

    if (!hasUsageStatsPermission(context)) {
        requestUsageStatsPermission(currentActivity)
    }


    result.success("settings_opened")
}


            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }
    
    override fun onDetachedFromActivity() {
        activity = null
    }
    
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }
    
    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }
    
}
