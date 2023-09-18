package at.htlhl.testing

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import at.htlhl.testing.data.SharedViewModel
import at.htlhl.testing.service.LocationUpdateService

class MyApplication : Application() {
    lateinit var myViewModel: SharedViewModel

    override fun onCreate() {
        super.onCreate()
        myViewModel = SharedViewModel(this)
    }
}
