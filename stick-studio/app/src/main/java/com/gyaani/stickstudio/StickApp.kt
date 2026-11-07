package com.gyaani.stickstudio

import android.app.Application

class StickApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ProjectStore.init(this)
        Prefs.init(this)
    }
}
