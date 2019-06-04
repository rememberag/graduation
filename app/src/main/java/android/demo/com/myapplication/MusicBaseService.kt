package android.demo.com.myapplication

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import data.EventBusData.ServiceTrigerEvent
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.lang.Exception

class MusicBaseService : Service() {

    var mPlayer: MediaPlayer? = MediaPlayer()
    val iBinder: IBinder = MusicBaseBinder()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder {
        mPlayer!!.setOnCompletionListener {
            EventBus.getDefault().post(ServiceTrigerEvent(ServiceTrigerEvent.ON_MUSIC_ACTIVITY_PLAY_COMPLETE))
        }
        return iBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.release()
        }
        mPlayer = null
        return super.onUnbind(intent)
    }

    inner class MusicBaseBinder : Binder() {

        fun prepareSource(filePath: String) {
            if(mPlayer != null) {
                mPlayer!!.reset()
                try{
                    mPlayer!!.setDataSource(filePath)
                    mPlayer!!.prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun playOrPauseMusic() {
            if(mPlayer != null) {
                if(mPlayer!!.isPlaying) {
                    mPlayer!!.pause()
                } else {
                    mPlayer!!.start()
                }
            }
        }

        fun isPlayingState() : Boolean {
            if(mPlayer != null) {
                return mPlayer!!.isPlaying
            } else {
                return false
            }
        }

        fun getSongDuration(): Int{
            if(mPlayer != null) {
                return mPlayer!!.duration
            }
            return 0
        }

        fun seekTo(position: Int) {
            if(mPlayer != null) {
                mPlayer!!.seekTo(position)
            }
        }

        fun getCurrentPosition() : Int {
            if(mPlayer != null) {
                return mPlayer!!.currentPosition
            }
            return 0
        }

        fun getMediaPlayer(): MediaPlayer {
            if(mPlayer == null) {
                mPlayer = MediaPlayer()
            }
            return mPlayer!!
        }

        fun rePlayMusic() {
            if(mPlayer != null) {
                mPlayer!!.stop()
                try {
                    mPlayer!!.prepare()
                    mPlayer!!.seekTo(0)
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


    }
}
