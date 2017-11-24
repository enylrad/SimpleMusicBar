package es.enylrad.simplemusicplayerbar

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import kotlinx.android.synthetic.main.media_player.view.*
import java.lang.Exception
import java.util.concurrent.TimeUnit

class MusicPlayerBar : RelativeLayout {

    private val mediaPlayer: MediaPlayer = MediaPlayer()

    private var mediaFileLengthInMilliseconds: Int = 0

    private val handlerMusic: Handler = Handler()

    private val notification = Runnable { primarySeekBarProgressUpdater() }

    private var songLoad = false

    private var mReset = true

    private var mPosition: Int = 0

    private var listener: ((MusicPlayerBar) -> Unit)? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, song: TrackSound) : super(context) {
        init()
        config(song)
    }

    constructor(context: Context, song: TrackSound, listener: (MusicPlayerBar) -> Unit) : super(context) {
        init()
        this.listener = listener
        config(song)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        inflate(context, R.layout.media_player, this)
    }

    private fun config(song: TrackSound) {
        btn_play_pause.setOnClickListener {
            listener?.invoke(this@MusicPlayerBar)
            onClickPlayLogic(song.url)
        }
        song_name.text = song.name
        seekBarDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, p1: Int, fromUser: Boolean) {
                if (fromUser) {
                    onTouchSeekLogic(seekBar)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        seekBarDistance.isEnabled = mediaPlayer.isPlaying
    }

    private fun showProgressBar(show: Boolean) {
        progressbar_play.visibility = if (show) View.VISIBLE else View.GONE
        img_play_pause.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun onClickPlayLogic(url: String) {
        if (!songLoad || mReset) {
            loadSongAndPrepare(url)
        } else {
            logicPlay()
        }
        logicSongCompleted()
    }

    private fun loadSongAndPrepare(url: String) {
        try {

            showProgressBar(true)

            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mReset = false
            mediaPlayer.setOnPreparedListener {
                songLoad = true
                mediaPlayer.seekTo(mPosition)
                showProgressBar(false)

                logicPlay()
            }
        } catch (e: Exception) {
            showProgressBar(false)

            logicPlay()
        }
    }

    private fun logicPlay() {
        mediaFileLengthInMilliseconds = mediaPlayer.duration // gets the song length in milliseconds from URL

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            btnPlay(true)
        } else {
            mediaPlayer.start()
            btnPlay(false)
        }
        seekBarDistance.isEnabled = mediaPlayer.isPlaying

        primarySeekBarProgressUpdater()
    }

    private fun logicSongCompleted() {
        mediaPlayer.setOnCompletionListener {
            btnPlay(true)
            mediaPlayer.seekTo(0)
            setSeekAndTime()
        }
    }

    fun stopMusic() {
        handlerMusic.removeCallbacks(notification)
        mReset = true
        mPosition = mediaPlayer.currentPosition
        btnPlay(true)
        showProgressBar(false)
        seekBarDistance.isEnabled = false
        mediaPlayer.reset()
    }

    private fun btnPlay(play: Boolean) {
        if (play) {
            img_play_pause.setImageResource(android.R.drawable.ic_media_play)
            btn_play_pause.background = ContextCompat.getDrawable(context, android.R.color.black)
        } else {
            img_play_pause.setImageResource(android.R.drawable.ic_media_pause)
            btn_play_pause.background = ContextCompat.getDrawable(context, R.color.grey)
        }
    }

    private fun onTouchSeekLogic(seekBar: SeekBar?) {
        /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
        if (mediaPlayer.isPlaying) {
            val progress = seekBar?.progress ?: 0
            val playPositionInMilliseconds = mediaFileLengthInMilliseconds / 100 * progress
            mediaPlayer.seekTo(playPositionInMilliseconds)
        }
    }

    /** Method which updates the SeekBar primary progress by current song playing position */
    private fun primarySeekBarProgressUpdater() {
        if (mediaPlayer.isPlaying) {
            setSeekAndTime()
            handlerMusic.postDelayed(notification, 1000)
        }

    }

    private fun setSeekAndTime() {
        val time = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds - mediaPlayer.currentPosition.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(mediaFileLengthInMilliseconds - mediaPlayer.currentPosition.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaFileLengthInMilliseconds - mediaPlayer.currentPosition.toLong()))
        )

        time_song.text = time
        seekBarDistance.progress = (mediaPlayer.currentPosition.toFloat() / mediaFileLengthInMilliseconds * 100).toInt()
    }

    companion object {
        private val TAG = MusicPlayerBar::class.java.name ?: ""
    }

}