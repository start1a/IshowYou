package com.start3a.ishowyou.room.content

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.start3a.ishowyou.R


class CustomPlayerUiController(
    private var context: Context,
    private var playerUi: View,
    private var youtubePlayer: YouTubePlayer,
    private var youtubePlayerView: YouTubePlayerView,
    private val isHost: Boolean
): AbstractYouTubePlayerListener(), YouTubePlayerFullScreenListener {

    private val uiLayout: View = playerUi.findViewById(R.id.uiLayout)
    private val panelLeft: View = playerUi.findViewById(R.id.panelLeft)
    private val panelMiddle: View = playerUi.findViewById(R.id.panelMiddle)
    private val panelRight: View = playerUi.findViewById(R.id.panelRight)
    private val progressPlay: Slider = playerUi.findViewById(R.id.progressPlay)
    private val textCurTime: TextView = playerUi.findViewById(R.id.textCurTime)
    private val textDuration: TextView = playerUi.findViewById(R.id.textDuration)
    private val btnPlay: ImageButton = playerUi.findViewById(R.id.btnPlay)
    private val btnFullScreen: ImageButton = playerUi.findViewById(R.id.btnFullScreen)
    private val btnFastForward: ImageButton = playerUi.findViewById(R.id.btnFastForward)
    private val btnRewind: ImageButton = playerUi.findViewById(R.id.btnRewind)
    private val btnChatVisible: ImageButton = playerUi.findViewById(R.id.btnChatVisible)
    private val rbtnRealTime: RadioButton = playerUi.findViewById(R.id.rbtnRealtime)

    private val playerTracker: YouTubePlayerTracker = YouTubePlayerTracker()
    private var isFullscreen = false
    private var isChatVisible = true
    lateinit var changeChatVisibility: (Boolean) -> Unit

    // 실시간
    private var isRealtimeUsed = true
    lateinit var realtimeChecked: (Boolean) ->  Unit

    // 영상 이동
    lateinit var seekbarChanged: (Float) -> Unit

    // 패널 터치
    private var isPanelTouched = false
    private var isPlaySeeking = false
    private val handlerPanel = Handler(Looper.getMainLooper())
    private val runnableUiVisible = Runnable { changeUiVisibility(View.VISIBLE) }
    private val runnableOffSeek = Runnable {
        isPanelTouched = false
        isPlaySeeking = false
    }

    init {
        youtubePlayer.addListener(playerTracker)

        panelLeft.setOnClickListener {
            panelTouch(-10f)
        }

        panelMiddle.setOnClickListener {
            if (btnPlay.visibility == View.GONE) {
                changeUiVisibility(View.VISIBLE)
            }
            else {
                isPanelTouched = false
                isPlaySeeking = false
                changeUiVisibility(View.GONE)
            }
        }

        panelRight.setOnClickListener {
            panelTouch(10f)
        }

        rbtnRealTime.setOnClickListener {
            rbtnRealTime.isChecked = !isRealtimeUsed
            isRealtimeUsed = !isRealtimeUsed

            realtimeChecked(isRealtimeUsed)
        }

        btnRewind.setOnClickListener {
            seekNewTime(-10f)
        }

        btnFastForward.setOnClickListener {
            seekNewTime(10f)
        }

        btnPlay.setOnClickListener {
            if (playerTracker.state == PlayerConstants.PlayerState.PLAYING) {
                youtubePlayer.pause()
                uncheckRealtime()
            }
            else if (playerTracker.state == PlayerConstants.PlayerState.PAUSED)
                youtubePlayer.play()
        }

        btnChatVisible.setOnClickListener {
            if (isChatVisible)
                btnChatVisible.setImageResource(R.drawable.ic_baseline_arrow_back_ios_24)
            else
                btnChatVisible.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24)
            isChatVisible = !isChatVisible
            changeChatVisibility(isChatVisible)
        }

        btnFullScreen.setOnClickListener {
            Log.d("TAGG", isFullscreen.toString())
            if (isFullscreen) youtubePlayerView.exitFullScreen()
            else youtubePlayerView.enterFullScreen()
        }

        progressPlay.setLabelFormatter { millisecondsToTimeString(it) }

        progressPlay.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                progressPlay.value = slider.value
                seekbarChanged(slider.value)
                youtubePlayer.seekTo(slider.value)

                if (!isHost)
                    uncheckRealtime()
            }
        })
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        progressPlay.visibility = View.GONE
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        super.onStateChange(youTubePlayer, state)

        when (state) {
            PlayerConstants.PlayerState.PLAYING,
            PlayerConstants.PlayerState.PAUSED,
            PlayerConstants.PlayerState.VIDEO_CUED,
            PlayerConstants.PlayerState.BUFFERING-> {
                if (state == PlayerConstants.PlayerState.PLAYING)
                    btnPlay.setImageResource(R.drawable.ic_baseline_pause_24)
                if (state == PlayerConstants.PlayerState.PAUSED)
                    btnPlay.setImageResource(R.drawable.ic_baseline_play_arrow_48)
            }
        }
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        super.onCurrentSecond(youTubePlayer, second)
        textCurTime.text = millisecondsToTimeString(second)
        if (second <= progressPlay.valueTo)
            progressPlay.value = second
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        super.onVideoDuration(youTubePlayer, duration)
        textDuration.text = "/ ${millisecondsToTimeString(duration)}"
        progressPlay.valueTo = duration
    }

    override fun onYouTubePlayerEnterFullScreen() {
        isFullscreen = true
        val viewParams: ViewGroup.LayoutParams = playerUi.layoutParams
        viewParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        playerUi.layoutParams = viewParams

        btnFullScreen.setImageResource(R.drawable.ic_baseline_fullscreen_exit_24)
        btnFullScreen.let {
            it.setImageResource(R.drawable.ic_baseline_fullscreen_24)
            val layoutParams = it.layoutParams
            layoutParams.width = it.width * 2
            layoutParams.height = it.height * 2
            it.layoutParams = layoutParams
        }
        btnChatVisible.visibility = View.VISIBLE
    }

    override fun onYouTubePlayerExitFullScreen() {
        isFullscreen = false
        val viewParams = playerUi.layoutParams
        viewParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        viewParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        playerUi.layoutParams = viewParams

        btnFullScreen.let {
            it.setImageResource(R.drawable.ic_baseline_fullscreen_24)
            val layoutParams = it.layoutParams
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.layoutParams = layoutParams
        }
        btnChatVisible.let {
            isChatVisible = true
            it.visibility = View.GONE
            it.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24)
        }
    }

    fun awayFromYoutubePlayer() {
        isRealtimeUsed = false
        rbtnRealTime.isChecked = false
    }

    private fun millisecondsToTimeString(time: Float): String {
        val hour = (time / 3600).toLong().run {
            if (this != 0L) {
                if (this < 10) "0$this:"
                else "$this:"
            }
            else ""
        }
        val min = ((time % 3600) / 60).toLong().run {
            if (this != 0L) {
                if (this < 10) "0$this:"
                else "$this:"
            }
            else "0:"
        }
        val sec = (time % 60).toLong().run {
            if (this < 10) "0$this"
            else "$this"
        }

        return "$hour$min$sec"
    }

    private fun panelTouch(intervalSeek: Float) {
        if (btnPlay.visibility == View.GONE) {
            // 처음 클릭
            if (!isPanelTouched) {
                isPanelTouched = true
                handlerPanel.postDelayed(runnableUiVisible, 500)
            }
            // 두 번째 클릭
            else {
                if (!isPlaySeeking) {
                    isPlaySeeking = true
                    handlerPanel.removeCallbacks(runnableUiVisible)
                }
                // 세 번째 이상
                else handlerPanel.removeCallbacks(runnableOffSeek)

                handlerPanel.postDelayed(runnableOffSeek, 500)
                seekNewTime(intervalSeek)
            }
        }
        else {
            changeUiVisibility(View.GONE)
            isPanelTouched = false
            isPlaySeeking = false
        }
    }

    private fun seekNewTime(intervalSeek: Float) {
        val newTime = playerTracker.currentSecond + intervalSeek
        youtubePlayer.seekTo(newTime)
        seekbarChanged(playerTracker.currentSecond + intervalSeek)

        if (!isHost)
            uncheckRealtime()
    }

    private fun changeUiVisibility(visibility: Int) {
        if (visibility == View.GONE)
            uiLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.fui_transparent))
        else
            uiLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.halfBlack))

        rbtnRealTime.visibility = visibility
        btnPlay.visibility = visibility
        btnFullScreen.visibility = visibility
        btnFastForward.visibility = visibility
        btnRewind.visibility = visibility
        textDuration.visibility = visibility
        textCurTime.visibility = visibility
        progressPlay.visibility = visibility
    }

    fun newVideoPlayed() {
        progressPlay.value = 0f
        isPanelTouched = false
        isPlaySeeking = false
    }

    fun uncheckRealtime() {
        isRealtimeUsed = false
        rbtnRealTime.isChecked = false
        realtimeChecked(false)
    }
}