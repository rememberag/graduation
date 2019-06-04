package PlayListActivity

import Main.LocalTypeAdapter
import Main.MainActivity
import Main.MainActivity.Companion.START_TO_PLAY_MUSIC
import Main.SettingData
import android.app.Activity
import android.content.Intent
import android.demo.com.myapplication.MusicPlayerActivity
import android.demo.com.myapplication.R
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.Toast
import data.BaseData
import data.LocalCategoryData
import data.Song
import kotlinx.android.synthetic.main.activity_play_list.*
import org.jetbrains.anko.db.insert
import utils.database
import java.io.Serializable

class PlayListActivity : AppCompatActivity(){

    val songAdapter: LocalTypeAdapter = LocalTypeAdapter()
    lateinit var currentPlayList: MutableList<BaseData>
    lateinit var songListData: MutableList<BaseData>
    lateinit var originData: LocalCategoryData
    lateinit var dialogAdapter: LocalTypeAdapter
    var songListIndex: Int = 0
    var isFromSingerList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_list)
        initView()
        initListener()
        setResult(Activity.RESULT_OK)
    }

    fun initListener() {

    }

    fun initView() {
        songList.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        songListIndex = intent.getIntExtra(SettingData.SONG_LIST_INDEX, -1)
        originData = if(songListIndex == -1) {
            isFromSingerList = true
            intent.getSerializableExtra(SettingData.SONG_LIST) as LocalCategoryData
        } else {
            isFromSingerList = false
            MainActivity.localSongList[songListIndex] as LocalCategoryData
        }
        songListData = originData.songList as MutableList<BaseData>
        if(!isFromSingerList) {
            val dummy = LocalCategoryData("", 0, arrayListOf())
            dummy.type = BaseData.ADD_SONG_TO_LIST
            songListData.add(0, dummy)
        }
        songAdapter.dataList = songListData
        songList.adapter = songAdapter
        songAdapter.songItemClickListener = object : LocalTypeAdapter.SongItemClickListener {
            override fun onItemClick(song: Song) {
                currentPlayList = songListData
                val dummy = if(isFromSingerList) {
                    currentPlayList
                } else {
                    ArrayList(currentPlayList.subList(1, currentPlayList.size))
                }
                startActivityForResult(
                    Intent(this@PlayListActivity, MusicPlayerActivity::class.java)
                        .putExtra(SettingData.FILE_PATH, song)
                        .putExtra(SettingData.IS_PLAYING_SONG, false)
                        .putExtra(SettingData.CURRENT_PLAY_LIST, dummy as Serializable)
                    , START_TO_PLAY_MUSIC)
            }
        }
        songAdapter.createNewItemListener = object : LocalTypeAdapter.CreateNewItemListener {
            override fun onClickCreateBt() {
                val recycleView = RecyclerView(this@PlayListActivity)
                recycleView.layoutManager = LinearLayoutManager(this@PlayListActivity, LinearLayout.VERTICAL, false)
                dialogAdapter = LocalTypeAdapter()
                dialogAdapter.dataList = MainActivity.localMusics
                dialogAdapter.songItemClickListener = object : LocalTypeAdapter.SongItemClickListener {
                    override fun onItemClick(song: Song) {
                        if(isAddedToSongList(song)) {
                            Toast.makeText(this@PlayListActivity, "歌曲已存在", Toast.LENGTH_SHORT).show()
                        } else {
                            database.use {
                                insert("Relation",
                                    "songId" to song.id,
                                    "listId" to originData.id)
                            }
                            songListData.add((songListData.size), song)
                            songAdapter.notifyDataSetChanged()
                            Toast.makeText(this@PlayListActivity, "成功添加到歌单", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                recycleView.adapter = dialogAdapter
                val dialog = AlertDialog.Builder(this@PlayListActivity).setTitle("添加歌曲到歌单")
                    .setView(recycleView)
                    .show()
                dialog.setCanceledOnTouchOutside(true)
            }
        }
    }

    fun isAddedToSongList(song: Song) : Boolean {
        for(item in songListData) {
            if(item == song) {
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(!isFromSingerList && songListData[0].type == BaseData.ADD_SONG_TO_LIST) {
            songListData.removeAt(0)
        }
    }
}