package es.enylrad.mediaplayerbartest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import es.enylrad.simplemusicplayerbar.MusicPlayerBar
import es.enylrad.simplemusicplayerbar.TrackSound
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listeners()
    }


    private fun listeners() {
        btn_create_bar.setOnClickListener {
            createMediaPlayerBar()
        }
    }

    /**
     * Create Simple Bar Song
     */
    private fun createMediaPlayerBar() {
        val name = et_name.text.toString()
        val url = et_url.text.toString()
        if (url.isNotEmpty()) {
            val song = TrackSound(name = name, url = url)
            layout_test.addView(MusicPlayerBar(context = this, song = song) {
                stopMusic(layout_test.indexOfChild(it)) //Stop others bar
            })
        }
    }

    /**
     * Stop all songs
     * index:Int skip index song
     */
    private fun stopMusic(index: Int = -1) {
        (0 until layout_test.childCount)
                .filter { it != index }
                .forEach { (layout_test.getChildAt(it) as MusicPlayerBar).stopMusic() }
    }

    override fun onPause() {
        super.onPause()
        stopMusic()
    }

}
