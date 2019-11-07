package videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.viewmodels.tv

import org.videolan.medialibrary.media.MediaLibraryItem
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.providers.HeaderProvider

interface TvBrowserModel {

    var currentItem: MediaLibraryItem?
    var nbColumns: Int
    val provider: HeaderProvider
}