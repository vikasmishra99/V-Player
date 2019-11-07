package videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.providers.medialibrary

import android.content.Context
import org.videolan.medialibrary.interfaces.media.AbstractVideoGroup
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.viewmodels.SortableModel


class VideoGroupsProvider(context: Context, model: SortableModel) : MedialibraryProvider<AbstractVideoGroup>(context, model) {
    override fun getAll() : Array<AbstractVideoGroup> = medialibrary.getVideoGroups(sort, desc, getTotalCount(), 0)

    override fun getTotalCount() = medialibrary.videoGroupsCount

    override fun getPage(loadSize: Int, startposition: Int) : Array<AbstractVideoGroup> = medialibrary.getVideoGroups(sort, desc, loadSize, startposition).also { completeHeaders(it, startposition) }

}