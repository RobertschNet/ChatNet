package at.htlhl.testing

import android.app.Application
import at.htlhl.testing.data.SharedViewModel

class MyApplication : Application() {
    lateinit var myViewModel: SharedViewModel

    override fun onCreate() {
        super.onCreate()
        myViewModel = SharedViewModel(this)
    }
}
