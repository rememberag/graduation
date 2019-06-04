package data

import java.io.Serializable

data class Song(var singer: String = "",// 歌手
                var song: String = "",//歌曲名
                var path: String = "",// 歌曲地址
                var duration: Int = 0,//歌曲长度
                var size: Long = 0, // 歌曲大小
                var albumId: String = "") : BaseData(), Serializable  {// 歌曲封面Id
    var id: Long = 0
}



