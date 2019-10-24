package br.com.streamingexoplayer

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util


class ExoPlayerActivity : AppCompatActivity() {


    private var playbackStateListener: PlaybackStateListener? = null

    private var playerView: PlayerView? = null
    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exo_player)
        playerView = findViewById(R.id.video_view)

        playbackStateListener = PlaybackStateListener()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }


    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        if (player == null) {
            val trackSelector = DefaultTrackSelector()
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd()
            )
            player = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        }

        playerView!!.player = player
        val uri = Uri.parse(getString(R.string.media_url_dash))
        val mediaSource = buildMediaSource(uri)

        player!!.playWhenReady = playWhenReady
        player!!.seekTo(currentWindow, playbackPosition)
        player!!.addListener(playbackStateListener)
        player!!.prepare(mediaSource, false, false)
    }

    private fun releasePlayer() {
        if (player != null) {
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.removeListener(playbackStateListener)
            player!!.release()
            player = null
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, "exoplayer-codelab")
        val mediaSourceFactory = DashMediaSource.Factory(dataSourceFactory)
        return mediaSourceFactory.createMediaSource(uri)
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        playerView!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private inner class PlaybackStateListener : Player.EventListener {

        override fun onPlayerStateChanged(
            playWhenReady: Boolean,
            playbackState: Int
        ) {
            val stateString: String
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> stateString = "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> stateString = "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> stateString = "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> stateString = "ExoPlayer.STATE_ENDED     -"
                else -> stateString = "UNKNOWN_STATE             -"
            }
            Log.d(
                TAG, "changed state to " + stateString
                        + " playWhenReady: " + playWhenReady
            )
        }
    }

    companion object {
        private val TAG = ExoPlayerActivity::class.java.name
    }
}
