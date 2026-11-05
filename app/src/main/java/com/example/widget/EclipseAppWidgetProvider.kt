package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class EclipseAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_SET_MODE) {
            val modeStr = intent.getStringExtra(EXTRA_MODE) ?: "ANC"
            val prefs = context.getSharedPreferences("eclipse_widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("active_mode", modeStr).apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, EclipseAppWidgetProvider::class.java)
            val allIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    companion object {
        const val ACTION_SET_MODE = "com.example.ACTION_WIDGET_SET_ANC"
        const val EXTRA_MODE = "extra_listening_mode"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.eclipse_app_widget)

            // Open app on main card click
            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingLaunch = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingLaunch)

            // Read preferences or default mock/connected state
            val prefs = context.getSharedPreferences("eclipse_widget_prefs", Context.MODE_PRIVATE)
            val activeMode = prefs.getString("active_mode", "ANC") ?: "ANC"

            // Set button pending intents
            setupModeButton(context, views, R.id.widget_btn_anc, "ANC", 1)
            setupModeButton(context, views, R.id.widget_btn_off, "NORMAL", 2)
            setupModeButton(context, views, R.id.widget_btn_trans, "TRANSPARENCY", 3)

            // Highlight active button color
            views.setInt(
                R.id.widget_btn_anc,
                "setBackgroundColor",
                if (activeMode == "ANC") 0xFF5D5FEF.toInt() else 0xFF2E313A.toInt()
            )
            views.setInt(
                R.id.widget_btn_off,
                "setBackgroundColor",
                if (activeMode == "NORMAL") 0xFF5D5FEF.toInt() else 0xFF2E313A.toInt()
            )
            views.setInt(
                R.id.widget_btn_trans,
                "setBackgroundColor",
                if (activeMode == "TRANSPARENCY") 0xFF5D5FEF.toInt() else 0xFF2E313A.toInt()
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun setupModeButton(
            context: Context,
            views: RemoteViews,
            viewId: Int,
            mode: String,
            requestCode: Int
        ) {
            val intent = Intent(context, EclipseAppWidgetProvider::class.java).apply {
                action = ACTION_SET_MODE
                putExtra(EXTRA_MODE, mode)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(viewId, pendingIntent)
        }
    }
}
