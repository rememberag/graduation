package utils

import android.content.Context
import android.provider.MediaStore
import data.Song

class MusicUtils {

    companion object {
        fun getLocalMusicData(context: Context) : MutableList<Song> {
            val localMusics = ArrayList<Song>()
            val cursor = context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null,
                MediaStore.Audio.AudioColumns.IS_MUSIC)
            if(cursor != null) {
                while(cursor.moveToNext()) {
                    val song = Song(
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                    )
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
    }
}