package Main

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.demo.com.myapplication.*
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import data.BaseData
import data.EventBusData.ServiceTrigerEvent
import data.Song

import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import utils.MusicUtils
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory

class MainActivity : AppCompatActivity() {

    companion object {
        val START_TO_PLAY_MUSIC = 11
        val CURRENT_SONG = "current_song"
        val IS_PLAYING_SONG = "is_playing_song"
        lateinit var localMusics: MutableList<BaseData>

    }

    var currentPosition: Int = 0
    val context = this
    lateinit var songRecyclerView: RecyclerView
    val songAdapter: LocalTypeAdapter = LocalTypeAdapter()
    lateinit var mPlayBinder: MusicBaseService.MusicBaseBinder
    lateinit var mConnection: ServiceConnection
    private var currentSongIndex: Int = 0
    lateinit var currentSong: Song

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        val intent = Intent(this@MainActivity, MusicBaseService::class.java)
        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mPlayBinder = service as MusicBaseService.MusicBaseBinder
                prepareMusic()
            }
        }
        initLocalData()
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        init()
    }

    fun init() {
        initView()
        initListener()
    }

    fun initView() {
        updatePlayGroup()
        initViewPager()
    }

    fun initViewPager() {
        val viewList = ArrayList<View>()
        songRecyclerView = RecyclerView(this)
        songRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        songAdapter.dataList = localMusics
        songAdapter.songItemClickListener = object : LocalTypeAdapter.SongItemClickListener {
            override fun onItemClick(song: Song) {
                var isPlayingSong: Boolean = false
                if(song == currentSong) {
                    isPlayingSong = true
                }
                startActivityForResult(
                    Intent(this@MainActivity, MusicPlayerActivity::class.java).putExtra("filePath", song).putExtra(IS_PLAYING_SONG, isPlayingSong)
                    , START_TO_PLAY_MUSIC)
            }
        }
        songRecyclerView.adapter = songAdapter
        Log.d("MainActivity", (localMusics[0] as Song).song)

        viewList.add(songRecyclerView)
        viewList.add(RecyclerView(this))
        viewList.add(RecyclerView(this))
        mViewPager.adapter = MainPagerAdapter(viewList)
        mTabLayout.setupWithViewPager(mViewPager)
    }

    fun prepareMusic() {
        if(localMusics.size >= 0) {
            mPlayBinder.prepareSource(currentSong.path)
        }
    }

    fun initLocalData() {
        localMusics = MusicUtils.getLocalMusicData(context)
        if(localMusics.size >= 0) {
            currentSongIndex = 0
            currentSong = ( localMusics[0] as Song)
        }
    }

    fun initListener() {
        playBt.setOnClickListener {
            playOrPauseMusic()
        }
        nextSongIv.setOnClickListener {
            currentSongIndex = localMusics.indexOf(currentSong)
            currentSongIndex = (currentSongIndex + 1) % localMusics.size
            currentSong = localMusics[currentSongIndex] as Song
            mPlayBinder.prepareSource(currentSong.path)
            mPlayBinder.playOrPauseMusic()
            updatePlayGroup()
            updatePlayIv()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == START_TO_PLAY_MUSIC) {
            if(resultCode == Activity.RESULT_OK) {
                if(data != null) {
                    currentSong = data.getSerializableExtra(CURRENT_SONG) as Song
                }
                updatePlayGroup()
                updatePlayIv()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPlayComplete(event: ServiceTrigerEvent) {
        currentSongIndex = (currentSongIndex + 1) % localMusics.size
        currentSong = localMusics[currentSongIndex] as Song
        mPlayBinder.prepareSource(currentSong.path)
        mPlayBinder.playOrPauseMusic()
        updatePlayGroup()
        updatePlayIv()
    }
    
    fun updateAlbumIv() {
        val albumArt: String = MusicUtils.getAlbumArt(currentSong.albumId, this)
        var bitmap: Bitmap?
        bitmap = BitmapFactory.decodeFile(albumArt)
        val bmpDraw = BitmapDrawable(bitmap)
        albumIv.setImageDrawable(bmpDraw)
    }
    
    fun updatePlayIv() {
        if(mPlayBinder.isPlayingState()) {
            playBt.setImageDrawable(context.getDrawable(R.drawable.stopimage))
        } else {
            playBt.setImageDrawable(context.getDrawable(R.drawable.playingimage))
        }
    }

    fun updatePlayGroup() {
        updateAlbumIv()
        songTv.text = currentSong.song.trim()
        singerTv.text = currentSong.singer.trim()
    }

    fun playOrPauseMusic() {
        mPlayBinder.playOrPauseMusic()
        updatePlayIv()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnection)
        EventBus.getDefault().unregister(this)
    }

}
