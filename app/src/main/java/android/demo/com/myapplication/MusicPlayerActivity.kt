package android.demo.com.myapplication

import Main.MainActivity
import Main.MainActivity.Companion.CURRENT_SONG
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.SeekBar
import data.EventBusData.ServiceTrigerEvent
import data.Song
import kotlinx.android.synthetic.main.activity_play.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import utils.MusicUtils
import java.util.*

class MusicPlayerActivity: AppCompatActivity() {

    var currentPosition: Int = 0
    var timer = Timer()
    val context = this
    lateinit var filePath: String
    lateinit var mPlayBinder: MusicBaseService.MusicBaseBinder
    lateinit var mConnection: ServiceConnection
    lateinit var mCurrentSong: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        EventBus.getDefault().register(this)
        filePath = (intent.getSerializableExtra("filePath") as Song).path
        mCurrentSong = (intent.getSerializableExtra("filePath") as Song)
        val isPlayingSong: Boolean = intent.extras.get(MainActivity.IS_PLAYING_SONG) as Boolean
        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mPlayBinder = service as MusicBaseService.MusicBaseBinder
                mSeekBar.max = mPlayBinder.getSongDuration()
                if(isPlayingSong) {
                    mSeekBar.progress = mPlayBinder.getCurrentPosition()
                    currentPosition = mPlayBinder.getCurrentPosition()
                    updatePlayIv()
                    if(mPlayBinder.isPlayingState()) {
                        timer = Timer()
                        timer.schedule(object : TimerTask() {
                            override fun run() {
                                mSeekBar.progress = mPlayBinder.getCurrentPosition()
                                mSeekBar.post {  playedTv.text = MusicUtils.formatTime(mPlayBinder.getCurrentPosition()) }
                                currentPosition = mPlayBinder.getCurrentPosition()
                            }
                        },0, 50)
                    }
                } else {
                    mPlayBinder.prepareSource(filePath)
                }
            }
        }
        bindService(Intent(this, MusicBaseService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        init()
        setResult(Activity.RESULT_OK, Intent().putExtra(CURRENT_SONG, mCurrentSong))
    }

    fun init() {
        initListener()
        initView()
    }

    fun initView() {
        updateAlbumIv()
        songName.text = mCurrentSong.song.trim()
        singerName.text = mCurrentSong.singer.trim()
        durationTv.text = MusicUtils.formatTime(mCurrentSong.duration)
    }

    fun updateAlbumIv() {
        val albumArt: String = MusicUtils.getAlbumArt(mCurrentSong.albumId, this)
        var bitmap: Bitmap? = null
        bitmap = BitmapFactory.decodeFile(albumArt)
        val bmpDraw = BitmapDrawable(bitmap)
        albumIv.setImageDrawable(bmpDraw)
    }

    fun initListener() {
        playBt.setOnClickListener {
            playOrPauseMusic()
        }
        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                timer.cancel()
                timer.purge()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mPlayBinder.seekTo(mSeekBar.progress)
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        mSeekBar.progress = mPlayBinder.getCurrentPosition()
                        mSeekBar.post {  playedTv.text = MusicUtils.formatTime(mPlayBinder.getCurrentPosition()) }
                        currentPosition = mPlayBinder.getCurrentPosition()
                        Log.d("MainActivity", "stopone")
                    }
                },0, 50)
                Log.d("MainActivity", "Stop")
            }
        })
    }

    fun updatePlayIv() {
        if(mPlayBinder.isPlayingState()) {
            playBt.setImageDrawable(context.getDrawable(R.drawable.stopimage))
        } else {
            playBt.setImageDrawable(context.getDrawable(R.drawable.playingimage))
        }
    }

    fun playOrPauseMusic() {
        mPlayBinder.playOrPauseMusic()
        updatePlayIv()
        if(!mPlayBinder.isPlayingState()) {
            timer.cancel()
            timer.purge()
        } else {
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    mSeekBar.progress = mPlayBinder.getCurrentPosition()
                    mSeekBar.post {  playedTv.text = MusicUtils.formatTime(mPlayBinder.getCurrentPosition()) }
                    currentPosition = mPlayBinder.getCurrentPosition()
                }
            },0, 50)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayComplete(event: ServiceTrigerEvent) {
        if(event.type == ServiceTrigerEvent.ON_MUSIC_ACTIVITY_PLAY_COMPLETE) {
            playBt.setImageDrawable(context.getDrawable(R.drawable.playingimage))
            timer.cancel()
            timer.purge()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        timer.purge()
        unbindService(mConnection)
        EventBus.getDefault().unregister(this)
    }
}