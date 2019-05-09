package android.demo.com.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MusicBaseService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

//    class MusicBaseBinder : Binder() {
//        fun getService
//    }
}
