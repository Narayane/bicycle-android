package com.sebastienbalard.bicycle

import android.app.Application

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme_NoActionBar)
    }
}