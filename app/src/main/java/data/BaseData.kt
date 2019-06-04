package data

import java.io.Serializable

open  class BaseData : Serializable {
    var type: Int = 0

    companion object {
        val NEW_SONG_LIST = 12
        val ADD_SONG_TO_LIST = 9
        val DATA = 0
    }
}