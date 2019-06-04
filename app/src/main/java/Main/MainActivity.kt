package Main

import android.app.Activity
import android.content.*
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
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.widget.EditText
import data.LocalCategoryData
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import utils.database
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    companion object {
        val START_TO_PLAY_MUSIC = 11
        val START_TO_SONG_LIST_ACTIVITY = 9
        lateinit var localMusics: MutableList<BaseData>
        var localSongList: MutableList<BaseData> = ArrayList()
        lateinit var currentPlayList: MutableList<BaseData>
        lateinit var currentSong: Song
    }

    val context = this
    lateinit var songRecyclerView: RecyclerView
    lateinit var singerRecycleView: RecyclerView
    lateinit var songListRecycleView: RecyclerView
    val songAdapter: LocalTypeAdapter = LocalTypeAdapter()
    val singerAdapter: LocalTypeAdapter = LocalTypeAdapter()
    val songListAdapter: LocalTypeAdapter = LocalTypeAdapter()

    lateinit var mPlayBinder: MusicBaseService.MusicBaseBinder
    lateinit var mConnection: ServiceConnection

    private var currentSongIndex: Int = 0

    lateinit var localSingers: MutableList<BaseData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)
        val intent = Intent(this@MainActivity, MusicBaseService::class.java)
        mConnection = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mPlayBinder = service as MusicBaseService.MusicBaseBinder
                prepareMusic()
            }
        }
        initLocalData()
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        initSingerData()
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
        //歌曲列表
        songRecyclerView = RecyclerView(this)
        songRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        songAdapter.dataList = localMusics
        songAdapter.songItemClickListener = object : LocalTypeAdapter.SongItemClickListener {
            override fun onItemClick(song: Song) {
                var isPlayingSong = false
                if(song == currentSong) {
                    isPlayingSong = true
                }
                startActivityForResult(
                    Intent(this@MainActivity, MusicPlayerActivity::class.java)
                        .putExtra(SettingData.FILE_PATH, song)
                        .putExtra(SettingData.IS_PLAYING_SONG, isPlayingSong)
                        .putExtra(SettingData.CURRENT_PLAY_LIST, currentPlayList as Serializable)
                    , START_TO_PLAY_MUSIC)
            }
        }
        songRecyclerView.adapter = songAdapter
        Log.d("MainActivity", (localMusics[0] as Song).song)

        //歌手列表
        singerRecycleView = RecyclerView(this)
        singerRecycleView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        singerAdapter.dataList = localSingers
        singerAdapter.songListItemClickListener = object : LocalTypeAdapter.SongListItemClickListener {
            override fun onItemClick(data: LocalCategoryData, position: Int) {
                startActivityForResult(Intent(this@MainActivity, PlayListActivity.PlayListActivity::class.java)
                    .putExtra(SettingData.SONG_LIST, data)
                    , START_TO_SONG_LIST_ACTIVITY)
            }
        }
        singerRecycleView.adapter = singerAdapter


        //歌单列表
        songListRecycleView = RecyclerView(this)
        songListRecycleView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        songListAdapter.dataList = localSongList
        songListAdapter.songListItemClickListener = object : LocalTypeAdapter.SongListItemClickListener {
            override fun onItemClick(data: LocalCategoryData, position: Int) {
                startActivityForResult(Intent(this@MainActivity, PlayListActivity.PlayListActivity::class.java)
                    .putExtra(SettingData.SONG_LIST, data as Serializable)
                    .putExtra(SettingData.SONG_LIST_INDEX, position),
                    START_TO_SONG_LIST_ACTIVITY)
            }
        }
        songListAdapter.createNewItemListener = object : LocalTypeAdapter.CreateNewItemListener {
            override fun onClickCreateBt() {
                val editText = EditText(context)
                val dialog = AlertDialog.Builder(context).setTitle("请输入歌单名称")
                    .setView(editText)
                    .setPositiveButton("确定", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            val text = editText.text.trim()
                            if(!TextUtils.isEmpty(text)) {
                                var lastId : Long = 0
                                //插入数据库数据
                                database.use {
                                    lastId = insert("PlayList",
                                        "name" to text.toString())
                                }
                                localSongList.add(LocalCategoryData(text.toString(),
                                    lastId.toInt(), arrayListOf()))
                                songListAdapter.notifyDataSetChanged()
                            }
                        }
                    }).setNegativeButton("取消",null).show()
                dialog.setCanceledOnTouchOutside(true)
            }
        }
        songListRecycleView.adapter = songListAdapter

        viewList.add(songRecyclerView)
        viewList.add(singerRecycleView)
        viewList.add(songListRecycleView)
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
        currentPlayList = localMusics
        if(localMusics.size >= 0) {
            currentSongIndex = 0
            currentSong = ( localMusics[0] as Song)
        }
        saveDataToSQLite()
//        database.use {
//            execSQL("delete from " + "PlayList")
//            execSQL("delete from " + "Relation")
//            insert("PlayList",
//                "name" to "first")
//            insert("PlayList",
//                "name" to "second")
//            insert("Relation",
//                "songId" to 1,
//                "listId" to 1)
//            insert("Relation",
//                "songId" to 2,
//                "listId" to 1)
//        }
        val dummy = LocalCategoryData("", 0, arrayListOf())
        dummy.type = BaseData.NEW_SONG_LIST
        localSongList.add(dummy)
        updateSongListSQLite()
    }

    fun updateSongListSQLite() {
        database.use {
            select("PlayList").exec {
                while(this.moveToNext()) {
                    var item = LocalCategoryData(this.getString(this.getColumnIndex("name")),
                        this.getInt(this.getColumnIndex("id")), arrayListOf())
                    localSongList.add(item)
                }
            }
            for(item in localSongList) {
                item as LocalCategoryData
                select("Relation")
                    .exec {
                        while (this.moveToNext()) {
                            if(this.getInt(this.getColumnIndex("listId")) == item.id) {
                                select("Songs").whereArgs("id = {songid}", "songid" to this.getInt(this.getColumnIndex("songId")))
                                    .exec {
                                        this.moveToFirst()
                                        item.songList.add(Song(
                                            this.getString(this.getColumnIndex("singer")),
                                            this.getString(this.getColumnIndex("songName")),
                                            this.getString(this.getColumnIndex("path")),
                                            this.getInt(this.getColumnIndex("duration")),
                                            this.getLong(this.getColumnIndex("size")),
                                            this.getString(this.getColumnIndex("albumId"))
                                        ))
                                    }
                            }
                        }
                    }
            }
        }
    }

    fun saveDataToSQLite() {
        for(item in localMusics) {
            item as Song
            database.use {
                val sqId: Long = insert("Songs",
                    "singer" to item.singer,
                    "songName" to item.song,
                    "path" to item.path,
                    "duration" to item.duration,
                    "size" to item.size,
                    "albumId" to item.albumId)
                item.id = sqId
            }
        }
    }

    fun initSingerData() {
        localSingers = ArrayList()
        var index: Int
        for(item in localMusics) {
            index =  isContainerSingers((item as Song).singer)
            if(index == -1) {
                localSingers.add(LocalCategoryData(item.singer, 0, arrayListOf(item)))
            } else {
                (localSingers[index] as LocalCategoryData).songList.add(item)
            }
        }
    }

    fun isContainerSingers(singerName: String) : Int {
        for(i in localSingers.indices) {
            if((localSingers[i] as LocalCategoryData).name == singerName) {
                return i
            }
        }
        return -1
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
        playGroup.setOnClickListener {
            startActivityForResult(
                Intent(this@MainActivity, MusicPlayerActivity::class.java)
                    .putExtra(SettingData.FILE_PATH, currentSong)
                    .putExtra(SettingData.IS_PLAYING_SONG, true)
                    .putExtra(SettingData.CURRENT_PLAY_LIST, currentPlayList as Serializable)
                , START_TO_PLAY_MUSIC)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == START_TO_PLAY_MUSIC) {
            if(resultCode == Activity.RESULT_OK) {
                if(data != null) {
                    currentSong = data.getSerializableExtra(SettingData.CURRENT_SONG) as Song
                }
                updatePlayGroup()
                updatePlayIv()
            }
        } else if (requestCode == START_TO_SONG_LIST_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK) {
               // updateSongListSQLite()
                songListAdapter.notifyDataSetChanged()
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
