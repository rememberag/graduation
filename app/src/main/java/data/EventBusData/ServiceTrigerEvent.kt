package data.EventBusData

class ServiceTrigerEvent(var type: String) {

    companion object {
        val ON_MUSIC_PLAY_COMPLETE = "on_music_play_complete"
        val ON_MUSIC_ACTIVITY_PLAY_COMPLETE = "on_music_activity_play_complete"
    }
}