package com.tabling.homework.application

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.stetho.Stetho
import com.tabling.homework.BuildConfig
import com.tabling.homework.utils.NetworkMonitor

class HomeworkApplication : Application() {

    companion object {
        private lateinit var instance: HomeworkApplication

        fun getInstance() : HomeworkApplication {
            return instance
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate() {
        super.onCreate()
        instance = this
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
        NetworkMonitor(this).startNetworkCallback()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onTerminate() {
        super.onTerminate()
        NetworkMonitor(this).stopNetworkCallback()
    }
}