package fm.grind.android.entities

data class CurrentTrack(val artist: String, val track: String) {
    companion object {
        val NULL = CurrentTrack("gaming radiostation", "Grind.FM")
    }
}