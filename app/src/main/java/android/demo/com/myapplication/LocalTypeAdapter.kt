package android.demo.com.myapplication

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import data.BaseData
import data.LocalCategoryData
import data.Song
import kotlinx.android.synthetic.main.item_category.view.*
import kotlinx.android.synthetic.main.item_song.view.*

class LocalTypeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    lateinit var dataList: MutableList<BaseData>
    lateinit var songItemClickListener: SongItemClickListener

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        return if(dataList[position] is Song) {
            SongViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false))
        }else {
            CategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if(dataList[position] is Song) {
            (viewHolder as SongViewHolder).bindData(position)
        } else {
            (viewHolder as CategoryViewHolder).bindData(position)
        }
    }

    inner class CategoryViewHolder(var view: View): RecyclerView.ViewHolder(view) {

        @SuppressLint("SetTextI18n")
        fun bindData(position: Int) {
            view.singerTv.text = (dataList[position] as LocalCategoryData).name
            view.songCount.text = (dataList[position] as LocalCategoryData).count.toString() + "首"
        }
    }

    inner class SongViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

        fun bindData(position: Int) {
            view.songName.text = (dataList[position] as Song).song
            view.durationTv.text = (dataList[position] as Song).duration.toString()
            view.setOnClickListener{
                songItemClickListener.onItemClick((dataList[position] as Song))
            }
        }
    }

    interface SongItemClickListener {
        fun onItemClick(song: Song)
    }

}