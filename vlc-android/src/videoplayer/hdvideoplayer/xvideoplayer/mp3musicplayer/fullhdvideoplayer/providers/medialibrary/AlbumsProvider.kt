/*****************************************************************************
 * AlbumsProvider.kt
 *****************************************************************************
 * Copyright © 2019 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.providers.medialibrary

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.videolan.medialibrary.interfaces.AbstractMedialibrary
import org.videolan.medialibrary.interfaces.media.AbstractAlbum
import org.videolan.medialibrary.interfaces.media.AbstractArtist
import org.videolan.medialibrary.interfaces.media.AbstractGenre
import org.videolan.medialibrary.media.MediaLibraryItem
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.Settings
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.viewmodels.SortableModel


@ExperimentalCoroutinesApi
class AlbumsProvider(val parent : MediaLibraryItem?, context: Context, model: SortableModel) : MedialibraryProvider<AbstractAlbum>(context, model) {

    override val sortKey = "${super.sortKey}_${parent?.javaClass?.simpleName}"
    override fun canSortByDuration() = true
    override fun canSortByReleaseDate() = true

    init {
        sort = Settings.getInstance(context).getInt(sortKey, if (parent is AbstractArtist) AbstractMedialibrary.SORT_RELEASEDATE else AbstractMedialibrary.SORT_DEFAULT)
        desc = Settings.getInstance(context).getBoolean("${sortKey}_desc", false)
    }

    override fun getAll() : Array<AbstractAlbum> = when (parent) {
        is AbstractArtist -> parent.getAlbums(sort, desc)
        is AbstractGenre -> parent.getAlbums(sort, desc)
        else -> medialibrary.getAlbums(sort, desc)
    }

    override fun getPage(loadSize: Int, startposition: Int) : Array<AbstractAlbum> {
        val list = if (model.filterQuery == null) when(parent) {
            is AbstractArtist -> parent.getPagedAlbums(sort, desc, loadSize, startposition)
            is AbstractGenre -> parent.getPagedAlbums(sort, desc, loadSize, startposition)
            else -> medialibrary.getPagedAlbums(sort, desc, loadSize, startposition)
        } else when(parent) {
            is AbstractArtist -> parent.searchAlbums(model.filterQuery, sort, desc, loadSize, startposition)
            is AbstractGenre -> parent.searchAlbums(model.filterQuery, sort, desc, loadSize, startposition)
            else -> medialibrary.searchAlbum(model.filterQuery, sort, desc, loadSize, startposition)
        }
        return list.also { completeHeaders(it, startposition) }
    }

    override fun getTotalCount() = if (model.filterQuery == null) when(parent) {
        is AbstractArtist -> parent.albumsCount
        is AbstractGenre -> parent.albumsCount
        else -> medialibrary.albumsCount
    } else when (parent) {
        is AbstractArtist -> parent.searchAlbumsCount(model.filterQuery)
        is AbstractGenre -> parent.searchAlbumsCount(model.filterQuery)
        else -> medialibrary.getAlbumsCount(model.filterQuery)
    }
}