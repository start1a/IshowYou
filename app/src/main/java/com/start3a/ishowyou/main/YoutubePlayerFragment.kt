package com.start3a.ishowyou.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener
import com.start3a.ishowyou.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_youtube_player.*

class YoutubePlayerFragment : Fragment() {

    private var viewModel: MainViewModel? = null
    private var isHost = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_youtube_player, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = activity!!.application!!.let {
            ViewModelProvider(
                activity!!.viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(MainViewModel::class.java)
        }

        viewModel!!.let { vm ->
            // 화면이 중지되면 자동 재생 중지
            lifecycle.addObserver(youtubePlayerView)

            youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

                override fun onReady(youTubePlayer: YouTubePlayer) {
                    // 풀스크린 뷰 설정
                    youtubePlayerView.addFullScreenListener(object :
                        YouTubePlayerFullScreenListener {
                        override fun onYouTubePlayerEnterFullScreen() {
                            // 윈도우 제거
                            hideSystemUi(activity!!.window.decorView)
                            // 뷰 크기 조절
                            activity!!.contentViewFrame.layoutParams.width = 1500
                            activity!!.contentViewFrame.layoutParams.height =
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            // 화면 회전
                            activity!!.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }

                        override fun onYouTubePlayerExitFullScreen() {
                            showSystemUi(activity!!.window.decorView)

                            activity!!.contentViewFrame.layoutParams.width =
                                ConstraintLayout.LayoutParams.MATCH_PARENT
                            activity!!.contentViewFrame.layoutParams.height =
                                LinearLayout.LayoutParams.WRAP_CONTENT

                            activity!!.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }
                    })

                    // Seekbar
                    youtube_player_seekbar.youtubePlayerSeekBarListener =
                        object : YouTubePlayerSeekBarListener {
                            override fun seekTo(time: Float) {
                                Toast.makeText(activity!!, "SeekTo222!!", Toast.LENGTH_SHORT).show()
                                youTubePlayer.seekTo(time)
                                if (isHost) {
                                    vm.seekBarYoutubeClicked(time)
                                }
                            }
                        }

                    btnSet.setOnClickListener {
                        // Seekbar 감지 리스너
                        if (!isHost) {
                            vm.setYoutubeSeekbarChangedListener {
                                youTubePlayer.seekTo(it)
                            }
                        }
                    }
                }
            })

            btnHost.setOnClickListener {
                isHost = true
            }
        }
    }

    private fun hideSystemUi(mDecorView: View) {
        mDecorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        View.SYSTEM_UI_FLAG_FULLSCREEN;
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun showSystemUi(mDecorView: View) {
        mDecorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.removeYoutubeSeekbarChangedListener()
    }
}