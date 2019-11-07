/*
 * *************************************************************************
 *  ContentActivity.java
 * **************************************************************************
 *  Copyright © 2017 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */

package videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.videolan.libvlc.RendererItem
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.PlaybackService
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.R
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.RendererDelegate
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.audio.EqualizerFragment
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.browser.ExtensionBrowser
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.dialogs.RenderersDialog
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.helpers.UiTools
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.preferences.PreferencesActivity
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.interfaces.Filterable
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.AndroidDevices
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.Preferences
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.Settings
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.Util

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@SuppressLint("Registered")
open class ContentActivity : AudioPlayerContainerActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private lateinit var searchView: SearchView
    private var showRenderers = !AndroidDevices.isChromeBook && !Util.isListEmpty(RendererDelegate.renderers.value)
    private val searchHiddenMenuItem = ArrayList<MenuItem>()


    override fun initAudioPlayerContainerActivity() {
        super.initAudioPlayerContainerActivity()
        if (!AndroidDevices.isChromeBook && !AndroidDevices.isAndroidTv
                && Settings.getInstance(this).getBoolean("enable_casting", true)) {
            PlaybackService.renderer.observe(this, Observer {
                val item = toolbar.menu.findItem(R.id.ml_menu_renderers) ?: return@Observer
                item.isVisible = showRenderers
                item.setIcon(if (!PlaybackService.hasRenderer()) R.drawable.ic_am_renderer_normal_w else R.drawable.ic_am_renderer_on_w)
            })
            RendererDelegate.renderers.observe(this, Observer<List<RendererItem>> { rendererItems ->
                showRenderers = !Util.isListEmpty(rendererItems)
                val item = toolbar.menu.findItem(R.id.ml_menu_renderers)
                if (item != null) item.isVisible = showRenderers
            })
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        UiTools.setOnDragListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (AndroidDevices.isAndroidTv) return false
        val current = currentFragment
        super.onCreateOptionsMenu(menu)
        if (current is AboutFragment) return true
        menuInflater.inflate(R.menu.activity_option, menu)
        if (current is ExtensionBrowser) {
            menu.findItem(R.id.ml_menu_last_playlist).isVisible = false
            menu.findItem(R.id.ml_menu_sortby).isVisible = false
        }
        if (current is Filterable) {
            val filterable = current as Filterable?
            val searchItem = menu.findItem(R.id.ml_menu_filter)
            searchView = searchItem.actionView as SearchView
            searchView.queryHint = getString(R.string.search_list_hint)
            searchView.setOnQueryTextListener(this)
            val query = filterable?.getFilterQuery()
            if (!query.isNullOrEmpty()) {
                activityHandler.post {
                    searchItem.expandActionView()
                    searchView.clearFocus()
                    UiTools.setKeyboardVisibility(searchView, false)
                    searchView.setQuery(query, false)
                }
            }
            searchItem.setOnActionExpandListener(this)
        } else
            menu.findItem(R.id.ml_menu_filter).isVisible = false
        menu.findItem(R.id.ml_menu_renderers).isVisible = showRenderers && Settings.getInstance(this).getBoolean("enable_casting", true)
        menu.findItem(R.id.ml_menu_renderers).setIcon(if (!PlaybackService.hasRenderer()) R.drawable.ic_am_renderer_normal_w else R.drawable.ic_am_renderer_on_w)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.ml_menu_search -> {
                startActivity(Intent(Intent.ACTION_SEARCH, null, this, SearchActivity::class.java))
                return true
            }
            R.id.action_rate_us -> {
                try{
                    var playstoreuri1: Uri = Uri.parse("market://details?id=videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer")
                    var playstoreIntent1: Intent = Intent(Intent.ACTION_VIEW, playstoreuri1)
                    this.startActivity(playstoreIntent1)
                }catch (exp:Exception){
                    var playstoreuri2: Uri = Uri.parse("https://play.google.com/store/apps/details?id=videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer")
                    var playstoreIntent2: Intent = Intent(Intent.ACTION_VIEW, playstoreuri2)
                    this.startActivity(playstoreIntent2)
                }
                return true
            }

            R.id.action_music_player ->{
                try{
                    var playstoreuri1: Uri =Uri.parse("market://details?id=com.musicplayer.musicapps.mp3player.audioplayer")
                    var playstoreIntent1: Intent = Intent(Intent.ACTION_VIEW, playstoreuri1)
                    this.startActivity(playstoreIntent1)
                }catch (exp:Exception){
                    var playstoreuri2: Uri = Uri.parse("https://play.google.com/store/apps/details?id=com.musicplayer.musicapps.mp3player.audioplayer")
                    var playstoreIntent2: Intent = Intent(Intent.ACTION_VIEW, playstoreuri2)
                    this.startActivity(playstoreIntent2)
                }
                return true
            }

            R.id.ml_menu_equalizer ->{
                EqualizerFragment().show(supportFragmentManager,"equalizer")
                return true
            }

            R.id.ml_menu_settings ->{
                startActivity(Intent(Intent.ACTION_SEARCH, null, this, PreferencesActivity::class.java))
                return true
            }



            R.id.ml_menu_renderers -> {
                if (!PlaybackService.hasRenderer() && RendererDelegate.renderers.value.size == 1) {
                    val renderer = RendererDelegate.renderers.value[0]
                    PlaybackService.renderer.value = renderer
                    val v = findViewById<View>(R.id.audio_player_container)
                    if (v != null) UiTools.snacker(v, getString(R.string.casting_connected_renderer, renderer.displayName))
                } else if (supportFragmentManager.findFragmentByTag("renderers") == null)
                    RenderersDialog().show(supportFragmentManager, "renderers")
                return true
            }
            R.id.ml_menu_filter -> {
                if (!item.isActionViewExpanded) setSearchVisibility(true)
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onQueryTextChange(filterQueryString: String): Boolean {
        val current = currentFragment
        if (current is Filterable) {
            if (filterQueryString.length < 3)
                (current as Filterable).restoreList()
            else
                (current as Filterable).filter(filterQueryString)
            return true
        }
        return false
    }

    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
        setSearchVisibility(true)
        return true
    }

    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
        setSearchVisibility(false)
        restoreCurrentList()
        return true
    }

    override fun onQueryTextSubmit(query: String) = false

    open fun forceLoadVideoFragment() {
        throw NotImplementedError("forceLoadVideoFragment not implemented")
    }

    private fun openSearchActivity() {
        startActivity(Intent(Intent.ACTION_SEARCH, null, this, SearchActivity::class.java)
                .putExtra(SearchManager.QUERY, searchView.query.toString()))
    }

    private fun setSearchVisibility(visible: Boolean) {
        val current = currentFragment
        if (current is Filterable) {
            (current as Filterable).setSearchVisibility(visible)
            makeRoomForSearch(visible)
        }
    }

    // Hide options menu items to make room for filter EditText
    private fun makeRoomForSearch(hide: Boolean) {
        val menu = toolbar.menu
        if (!hide) {
            searchHiddenMenuItem.forEach {
                it.isVisible = true
            }
            searchHiddenMenuItem.clear()
            invalidateOptionsMenu()
        } else {
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                if (menuItem.isVisible) {
                    menuItem.isVisible = false
                    searchHiddenMenuItem.add(menuItem)
                }
            }
        }
    }

    fun onClick(v: View) {
        if (v.id == R.id.searchButton) openSearchActivity()
    }

    fun closeSearchView() {
        toolbar.menu?.findItem(R.id.ml_menu_filter)?.collapseActionView()
    }

    private fun restoreCurrentList() {
        (currentFragment as? Filterable)?.restoreList()
    }

    companion object {
        const val TAG = "VLC/ContentActivity"
    }
}
