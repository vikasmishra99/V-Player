/*
 * *************************************************************************
 *  NetworkBrowserFragment.java
 * **************************************************************************
 *  Copyright © 2015-2017 VLC authors and VideoLAN
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

package videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.browser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.videolan.medialibrary.interfaces.media.AbstractMediaWrapper
import org.videolan.tools.isStarted
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.ExternalMonitor
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.R
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.VideoPlayerApplication
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.dialogs.NetworkServerDialog
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.dialogs.VlcLoginDialog
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.view.EmptyLoadingState
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.CTX_FAV_ADD
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.CTX_FAV_EDIT
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.util.Util
import videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.viewmodels.browser.NetworkModel

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class NetworkBrowserFragment : BaseBrowserFragment() {

    override val categoryTitle: String
        get() = getString(R.string.network_browsing)

    private val mLocalReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isResumed)
                goBack()
            else
                goBack = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, NetworkModel.Factory(requireContext(), mrl, showHiddenFiles)).get(NetworkModel::class.java)
        if (isRootDirectory) swipeRefreshLayout.isEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_option_network, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.ml_menu_save)
        item.isVisible = !isRootDirectory
        launch {
            val isFavorite = mrl != null && withContext(Dispatchers.IO) { browserFavRepository.browserFavExists(Uri.parse(mrl)) }
            item.setIcon(if (isFavorite)
                R.drawable.ic_menu_bookmark_w
            else
                R.drawable.ic_menu_bookmark_outline_w)
            item.setTitle(if (isFavorite) R.string.favorites_remove else R.string.favorites_add)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isRootDirectory) LocalBroadcastManager.getInstance(VideoPlayerApplication.appContext).registerReceiver(mLocalReceiver, IntentFilter(VlcLoginDialog.ACTION_DIALOG_CANCELED))
        fabPlay?.setImageResource(if (isRootDirectory) R.drawable.ic_fab_add else R.drawable.ic_fab_play)
        fabPlay?.setOnClickListener(this)
    }

    override fun refresh() {
        if (ExternalMonitor.isConnected)
            super.refresh()
        else {
            updateEmptyView()
            adapter.clear()
        }
    }

    override fun createFragment(): Fragment {
        return NetworkBrowserFragment()
    }

    override fun onStop() {
        super.onStop()
        if (!isRootDirectory) LocalBroadcastManager.getInstance(VideoPlayerApplication.appContext).unregisterReceiver(mLocalReceiver)
        goBack = false
    }

    override fun onCtxAction(position: Int, option: Int) {
        val mw = this.adapter.getItem(position) as AbstractMediaWrapper
        when (option) {
            CTX_FAV_ADD -> browserFavRepository.addNetworkFavItem(mw.uri, mw.title, mw.artworkURL)
            CTX_FAV_EDIT -> showAddServerDialog(mw)
            else -> super.onCtxAction(position, option)
        }
    }

    override fun browseRoot() {}

    private fun allowLAN(): Boolean {
        return ExternalMonitor.isLan || ExternalMonitor.isVPN
    }

    /**
     * Update views visibility and emptiness info
     */
    override fun updateEmptyView() {
        if (ExternalMonitor.isConnected) {
            if (Util.isListEmpty(viewModel.dataset.value)) {
                if (swipeRefreshLayout.isRefreshing) {
                    binding.emptyLoading.state = EmptyLoadingState.LOADING
                    binding.networkList.visibility = View.GONE
                } else {

                    if (isRootDirectory) {
                        if (allowLAN()) {
                            binding.emptyLoading.state = EmptyLoadingState.LOADING
                            binding.emptyLoading.loadingText = R.string.network_shares_discovery
                        } else {
                            binding.emptyLoading.state = EmptyLoadingState.EMPTY
                            binding.emptyLoading.emptyText = R.string.network_connection_needed
                        }
                    } else {
                        binding.emptyLoading.state = EmptyLoadingState.EMPTY
                        binding.emptyLoading.emptyText = R.string.network_empty
                    }
                    binding.networkList.visibility = View.GONE
                    handler.sendEmptyMessage(MSG_HIDE_LOADING)
                }
            } else {
                binding.emptyLoading.state = EmptyLoadingState.NONE
                binding.networkList.visibility = View.VISIBLE
            }
        } else {
            binding.emptyLoading.state = EmptyLoadingState.EMPTY
            binding.emptyLoading.emptyText = R.string.network_connection_needed
            binding.networkList.visibility = View.GONE
            binding.showFavorites = false
        }
    }

    override fun onClick(v: View) {
        if (!isRootDirectory)
            super.onClick(v)
        else if (v.id == R.id.fab) showAddServerDialog(null)
    }

    private fun showAddServerDialog(mw: AbstractMediaWrapper?) {
        val fm = fragmentManager ?: return
        val dialog = NetworkServerDialog()
        if (mw != null) dialog.setServer(mw)
        dialog.show(fm, "fragment_add_server")
    }

    override fun onUpdateFinished(adapter: RecyclerView.Adapter<*>) {
        super.onUpdateFinished(adapter)
        if (isRootDirectory && isStarted()) fabPlay?.show()
    }
}
