package com.finvu.android_demo

import android.app.Application
import android.util.Log
import com.finvu.android.FinvuManager
import com.finvu.android.publicInterface.FinvuEvent
import com.finvu.android.publicInterface.FinvuEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class DemoApplication : Application() {

    // Application-level coroutine scope
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val eventListener = object : FinvuEventListener {
        override fun onEvent(event: FinvuEvent) {
            Log.d("FinvuEvents", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d("FinvuEvents", "Event: ${event.eventName}")
            Log.d("FinvuEvents", "Category: ${event.eventCategory}")
            Log.d("FinvuEvents", "Timestamp: ${event.timestamp}")

            event.params.forEach { (key, value) ->
                Log.d("FinvuEvents", "  $key: $value")
            }
            Log.d("FinvuEvents", "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }

    override fun onCreate() {
        super.onCreate()

        FinvuManager.shared.addEventListener(eventListener)
        FinvuManager.shared.setEventsEnabled(true)

        Log.d("DemoApp", "✅ Finvu event tracking enabled")
    }

    override fun onTerminate() {
        FinvuManager.shared.removeEventListener(eventListener)
        super.onTerminate()
    }
}