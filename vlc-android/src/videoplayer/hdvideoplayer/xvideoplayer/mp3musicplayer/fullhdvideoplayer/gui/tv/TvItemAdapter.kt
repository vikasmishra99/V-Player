package videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.tv

interface TvItemAdapter : TvFocusableAdapter {
    fun submitList(pagedList: Any?)

    var focusNext: Int
}
