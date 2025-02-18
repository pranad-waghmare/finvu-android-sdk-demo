package com.finvu.android_demo

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class DemoApplication: Application() {

    // Define a CoroutineScope for the entire app
    val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onTerminate() {
        super.onTerminate()
        // Cancel the scope when the application is terminated
        applicationScope.cancel()
    }

}
