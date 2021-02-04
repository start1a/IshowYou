package com.start3a.ishowyou.room.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener
import com.start3a.ishowyou.R
import com.start3a.ishowyou.room.ChatRoomViewModel
import kotlinx.android.synthetic.main.fragment_youtube_player.*

class YoutubePlayerFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null

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
                .get(ChatRoomViewModel::class.java)
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
                            vm.mFullScreenController.rotate(true)
                        }

                        override fun onYouTubePlayerExitFullScreen() {
                            showSystemUi(activity!!.window.decorView)
                            vm.mFullScreenController.rotate(false)
                        }
                    })

                    // Seekbar
                    youtube_player_seekbar.youtubePlayerSeekBarListener =
                        object : YouTubePlayerSeekBarListener {
                            override fun seekTo(time: Float) {
                                youTubePlayer.seekTo(time)
                                vm.seekbarYoutubeClicked(time)
                            }
                        }

                    vm.initContent_Youtube {
                        youTubePlayer.seekTo(it)
                    }
                }
            })

            vm.mFullScreenController.contentExitFullScreenMode = {
                youtubePlayerView.exitFullScreen()
            }
        }
    }

    private fun hideSystemUi(mDecorView: View) {
        mDecorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun showSystemUi(mDecorView: View) {
        mDecorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}