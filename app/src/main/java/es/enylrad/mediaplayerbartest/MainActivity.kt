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

        createMediaPlayerBar()
    }

    private fun createMediaPlayerBar() {
        val song = TrackSound(name = "Bohemian Rhapsody", url = "http://hcmaslov.d-real.sci-nnov.ru/public/mp3/Queen/Queen%20'Bohemian%20Rhapsody'.mp3")
        layout_test.addView(MusicPlayerBar(context = this, song = song))
    }
}
