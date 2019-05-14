package utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import data.BaseData
import data.Song
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MusicUtils {

    lateinit var context: Context

    companion object {
        fun getLocalMusicData(context: Context) : MutableList<BaseData> {
            val localMusics = ArrayList<BaseData>()
            val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null,
                MediaStore.Audio.AudioColumns.IS_MUSIC)
            if(cursor != null) {
                while(cursor.moveToNext()) {
                    val song = Song(
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                    )
                    Log.d("MusicUtils",cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)) )
                    Log.d("MusicUtils",cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)) )
                    if(song.song.contains("-")) {
                        val dummy = song.song.split("-")
                        song.singer = dummy[0]
                        song.song = dummy[1]
                    }
                    if(song.size > 1024 * 800) {
                        localMusics.add(song)
                    }
                }
                cursor.close()
            }
            return localMusics
        }

        @SuppressLint("SimpleDateFormat")
        fun formatTime(duration: Int) : String {
            val date = Date(duration.toLong())
            val sdf = SimpleDateFormat("mm:ss")
            val totalTime = sdf.format(date)
            return totalTime
        }

        fun getAlbumArt(albumId: String, context: Context) : String {
            val mUriAlbums = "content://media/external/audio/albums"
            val projection: Array<String> = arrayOf("album_art")
            var cursor: Cursor? = null
            var albumArt: String = ""
            try {
                cursor = context.contentResolver.query(Uri.parse("$mUriAlbums/$albumId"), projection,null, null, null)
                if (cursor.count > 0 && cursor.columnCount > 0) {
                    cursor.moveToNext()
                    albumArt = cursor.getString(0)
                }
            }catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if(cursor != null) {
                    cursor.close()
                    cursor = null
                }
            }
            return albumArt
        }
    }
}