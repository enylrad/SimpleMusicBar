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

    val mMediaPlayer: MediaPlayer = MediaPlayer()

    private var mMediaFileLengthInMilliseconds: Int = 0

    private val mHandlerMusic: Handler = Handler()

    private val mRunnable: Runnable = Runnable { primarySeekBarProgressUpdater() }

    private var mReset: Boolean = true

    private var mPauseSong: Boolean = false

    private var mLastPosition: Int = 0

    private var mListener: ((MusicPlayerBar) -> Unit)? = null

    private var mSong: TrackSound? = null

    constructor(context: Context) : super(context) {
        init()
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

    fun config(song: TrackSound, listener: ((MusicPlayerBar) -> Unit)? = null): MusicPlayerBar {
        mSong = song
        mListener = listener
        setTextSong(mSong?.name ?: "")
        seekBarIsEnabled(false)

        listenerBtnPlayPause()
        listenerSeekBar()
        return this
    }

    private fun setTextSong(song: String) {
        song_name.text = song
    }

    private fun seekBarIsEnabled(isEnabled: Boolean) {
        seek_bar_time.isEnabled = isEnabled
    }

    private fun listenerBtnPlayPause() {
        btn_play_pause.setOnClickListener {
            mListener?.invoke(this@MusicPlayerBar)
            configSong()
        }
    }

    private fun listenerSeekBar() {
        seek_bar_time.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
    }

    private fun listenerSongCompleted() {
        mMediaPlayer.setOnCompletionListener {
            setResourcePlayPause(true)
            mMediaPlayer.seekTo(0)
            setProgressAndTime()
        }
    }

    private fun showProgressBar(show: Boolean) {
        progressbar_play.visibility = if (show) View.VISIBLE else View.GONE
        img_play_pause.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun configSong() {
        if (mReset) {
            logicPrepareSong()
        } else {
            logicPlayPause()
        }
    }

    private fun logicPrepareSong() {
        try {
            showProgressBar(true)

            mPauseSong = false
            mReset = false

            mMediaPlayer.setDataSource(mSong?.url ?: "")
            mMediaPlayer.prepareAsync()
            mMediaPlayer.setOnPreparedListener {
                mMediaPlayer.seekTo(mLastPosition)
                showProgressBar(false)

                if (!mPauseSong) {
                    logicPlayPause()
                }
            }
            listenerBuffering()
            listenerSongCompleted()
        } catch (e: Exception) {
            showProgressBar(false)
            logicPlayPause()
        }
    }

    private fun listenerBuffering() {
        var firstTime = true
        mMediaPlayer.setOnBufferingUpdateListener { _, progress ->
            if (!firstTime) {
                if (progress <= seek_bar_time.max) {
                    seek_bar_time.secondaryProgress = progress
                }
            }
            firstTime = false
        }
    }

    private fun logicPlayPause() {
        mMediaFileLengthInMilliseconds = mMediaPlayer.duration

        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.pause()
            setResourcePlayPause(true)
        } else {
            mMediaPlayer.start()
            setResourcePlayPause(false)
        }
        seekBarIsEnabled(mMediaPlayer.isPlaying)

        primarySeekBarProgressUpdater()
    }

    fun resetMusic() {
        mHandlerMusic.removeCallbacks(mRunnable)
        mReset = true
        setResourcePlayPause(true)
        saveCurrentPosition()
        showProgressBar(false)
        seekBarIsEnabled(false)
        mMediaPlayer.reset()
    }

    fun pauseMusic() {
        mHandlerMusic.removeCallbacks(mRunnable)
        mPauseSong = true
        setResourcePlayPause(true)
        seekBarIsEnabled(false)
        if (mMediaPlayer.isPlaying) {
            mMediaPlayer.pause()
        }
    }

    private fun saveCurrentPosition() {
        val pos = mMediaPlayer.currentPosition
        if (pos != 0) {
            mLastPosition = pos
            seek_bar_time.secondaryProgress = 0
        }
    }

    private fun setResourcePlayPause(play: Boolean) {
        if (play) {
            img_play_pause.setImageResource(android.R.drawable.ic_media_play)
            btn_play_pause.background = ContextCompat.getDrawable(context, android.R.color.black)
        } else {
            img_play_pause.setImageResource(android.R.drawable.ic_media_pause)
            btn_play_pause.background = ContextCompat.getDrawable(context, R.color.grey)
        }
    }

    private fun onTouchSeekLogic(seekBar: SeekBar?) {
        if (mMediaPlayer.isPlaying) {
            val progress = seekBar?.progress ?: 0
            val playPositionInMilliseconds = mMediaFileLengthInMilliseconds / 100 * progress
            mMediaPlayer.seekTo(playPositionInMilliseconds)
        }
    }

    /** Method which updates the SeekBar primary progress by current song playing position */
    private fun primarySeekBarProgressUpdater() {
        if (mMediaPlayer.isPlaying) {
            setProgressAndTime()
            mHandlerMusic.postDelayed(mRunnable, 1000)
        }

    }

    private fun setProgressAndTime() {
        val time = String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(mMediaFileLengthInMilliseconds - mMediaPlayer.currentPosition.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(mMediaFileLengthInMilliseconds - mMediaPlayer.currentPosition.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mMediaFileLengthInMilliseconds - mMediaPlayer.currentPosition.toLong()))
        )

        time_song.text = time
        seek_bar_time.progress = (mMediaPlayer.currentPosition.toFloat() / mMediaFileLengthInMilliseconds * 100).toInt()
    }

    companion object {
        private val TAG = MusicPlayerBar::class.java.name ?: ""
    }

}