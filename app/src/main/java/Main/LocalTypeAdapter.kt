package Main

import android.annotation.SuppressLint
import android.demo.com.myapplication.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import data.BaseData
import data.LocalCategoryData
import data.Song
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_new_song_list.view.*
import kotlinx.android.synthetic.main.item_song.view.*
import utils.MusicUtils

class LocalTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    lateinit var dataList: MutableList<BaseData>
    lateinit var songItemClickListener: SongItemClickListener
    lateinit var songListItemClickListener: SongListItemClickListener
    lateinit var createNewItemListener: CreateNewItemListener

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(dataList[position] is Song) {
            R.layout.item_song
        } else if(dataList[position].type == BaseData.NEW_SONG_LIST || dataList[position].type == BaseData.ADD_SONG_TO_LIST){
            R.layout.item_new_song_list
        } else {
            R.layout.item_category
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == R.layout.item_song) {
            SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false))
        }else if(viewType == R.layout.item_new_song_list){
            NewSongListViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_new_song_list, parent, false))
        } else {
            CategoryViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category, parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if(dataList[position] is Song) {
            (viewHolder as SongViewHolder).bindData(position)
        } else {
            if(viewHolder is NewSongListViewHolder) {
                 viewHolder.bindData(position)
            } else {
                (viewHolder as CategoryViewHolder).bindData(position)
            }
        }
    }

    inner class NewSongListViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        fun bindData(position: Int) {
            view.newTv.setOnClickListener {
                createNewItemListener.onClickCreateBt()
            }
            view.newIv.setOnClickListener {
                createNewItemListener.onClickCreateBt()
            }
            if(dataList[position].type == BaseData.NEW_SONG_LIST) {
                view.newTv.text = "新建歌单"
            } else if(dataList[position].type == BaseData.ADD_SONG_TO_LIST) {
                view.newTv.text = "添加歌曲"
            }
        }
    }

    inner class CategoryViewHolder(var view: View): RecyclerView.ViewHolder(view) {

        @SuppressLint("SetTextI18n")
        fun bindData(position: Int) {
            view.singerTv.text = (dataList[position] as LocalCategoryData).name
            view.songCount.text = (dataList[position] as LocalCategoryData).songList.size.toString() + "首"
            view.setOnClickListener {
                songListItemClickListener.onItemClick(dataList[position] as LocalCategoryData, position)
            }
        }
    }

    inner class SongViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        fun bindData(position: Int) {
            val songName = (dataList[position] as Song).song.trim()
            if(TextUtils.isEmpty(songName)) {
                view.songName.text = "未知"
            } else {
                view.songName.text = songName
            }
            setAlbumIv(position)
            view.singerName.text = (dataList[position] as Song).singer.trim()
            view.durationTv.text = MusicUtils.formatTime((dataList[position] as Song).duration)
            view.setOnClickListener{
                songItemClickListener.onItemClick((dataList[position] as Song))
            }
        }

        fun setAlbumIv(position: Int) {
            val albumArt: String = MusicUtils.getAlbumArt((dataList[position] as Song).albumId, view.context)
            var bitmap: Bitmap? = null
            bitmap = BitmapFactory.decodeFile(albumArt)
            val bmpDraw = BitmapDrawable(bitmap)
            view.albumIv.setImageDrawable(bmpDraw)
        }
    }

    interface SongItemClickListener {
        fun onItemClick(song: Song)
    }

    interface SongListItemClickListener {
        fun onItemClick(data: LocalCategoryData, position: Int)
    }

    interface CreateNewItemListener {
        fun onClickCreateBt()
    }

}