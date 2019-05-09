package android.demo.com.myapplication

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.util.Log
import android.widget.SeekBar

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    var currentPosition: Int = 0
    var mPlayer: MediaPlayer = MediaPlayer()
    var timer = Timer()
    val context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        init()
    }

    fun init() {
        var filepath: String = Environment.getExternalStorageDirectory().absolutePath +
                "/netease/cloudmusic/Music/Secret.mp3"
        try{
            mPlayer.setDataSource(filepath)
            mPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mSeekBar.max = mPlayer.duration
        initListener()
    }

    fun initListener() {
        mPlayer.setOnCompletionListener {
            playBt.setImageDrawable(context.getDrawable(R.drawable.playingimage))
            timer.cancel()
            timer.purge()
        }
        playBt.setOnClickListener {
            playMusic()
        }
        mSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                timer.cancel()
                timer.purge()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mPlayer.seekTo(mSeekBar.progress)
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        mSeekBar.setProgress(mPlayer.currentPosition)
                        currentPosition = mPlayer.currentPosition
                        Log.d("MainActivity", "stopone")
                    }
                },0, 50)
                Log.d("MainActivity", "Stop")
            }
        })
    }

    fun playMusic() {
        if(mPlayer.isPlaying) {
            mPlayer.pause()
            timer.cancel()
            timer.purge()
            playBt.setImageDrawable(context.getDrawable(R.drawable.playingimage))
        } else {
            mPlayer.start()
            playBt.setImageDrawable(context.getDrawable(R.drawable.stopimage))
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    mSeekBar.setProgress(mPlayer.currentPosition)
                    currentPosition = mPlayer.currentPosition
                }
            },0, 50)
        }
    }

    override fun onDestroy() {
        mPlayer.release()
        super.onDestroy()
    }

}
