package com.start3a.ishowyou.main.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.start3a.ishowyou.R
import com.start3a.ishowyou.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_lobby_youtube.*

class LobbyYoutubeFragment : Fragment() {

    private val INTERVAL_VIDEO_SEEK = 10.0f
    private var viewModel: MainViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lobby_youtube, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = requireActivity().application!!.let {
            ViewModelProvider(
                requireActivity().viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(MainViewModel::class.java)
        }

        viewModel!!.let { vm ->
            // 화면이 중지되면 자동 재생 중지
            lifecycle.addObserver(lobby_youtubePlayerView)
            lobby_youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    lobby_youtubePlayerView.getPlayerUiController().setCustomAction1(
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fast_rewind_24)!!
                    ) {
                        youTubePlayer.seekTo(vm.timeCurVideo - INTERVAL_VIDEO_SEEK)
                    }
                    lobby_youtubePlayerView.getPlayerUiController().setCustomAction2(
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fast_forward_24)!!
                    ) {
                        youTubePlayer.seekTo(vm.timeCurVideo + INTERVAL_VIDEO_SEEK)
                    }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)
                    vm.timeCurVideo = second
                }
            })

            // 풀스크린 뷰 설정
            lobby_youtubePlayerView.addFullScreenListener(object :
                YouTubePlayerFullScreenListener {
                override fun onYouTubePlayerEnterFullScreen() {
                    vm.mFullScreenController.rotate(true)
                }

                override fun onYouTubePlayerExitFullScreen() {
                    vm.mFullScreenController.rotate(false)
                }
            })

            vm.mFullScreenController.contentExitFullScreenMode = {
                lobby_youtubePlayerView.exitFullScreen()
            }
        }
    }
}