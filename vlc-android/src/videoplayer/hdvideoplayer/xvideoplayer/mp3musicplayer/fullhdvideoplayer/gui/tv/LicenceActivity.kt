package videoplayer.hdvideoplayer.xvideoplayer.mp3musicplayer.fullhdvideoplayer.gui.tv

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.view.View
import android.webkit.WebView

class LicenceActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val revision = getString(R.string.build_revision)
        val licence = WebView(this)
        //licence.loadData(Util.readAsset("licence.htm", "").replace("!COMMITID!", revision), "text/html", "UTF8")
        setContentView(licence)
        (licence.parent as View).setBackgroundColor(Color.LTGRAY)
        TvUtil.applyOverscanMargin(this)
    }
}
