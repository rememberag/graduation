package data

import java.io.Serializable

data class LocalCategoryData(var name: String,
                             var id: Int,
                             var songList: MutableList<Song>) : BaseData(), Serializable