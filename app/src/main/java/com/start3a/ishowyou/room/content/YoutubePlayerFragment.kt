package com.start3a.ishowyou.room.content

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.start3a.ishowyou.R
import com.start3a.ishowyou.room.ChatRoomViewModel
import kotlinx.android.synthetic.main.activity_chat_room.*
import kotlinx.android.synthetic.main.fragment_youtube_player.*
import kotlinx.android.synthetic.main.layout_draggable_top.view.*

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

        viewModel = requireActivity().application!!.let {
            ViewModelProvider(
                requireActivity().viewModelStore,
                ViewModelProvider.AndroidViewModelFactory(it)
            )
                .get(ChatRoomViewModel::class.java)
        }


        viewModel!!.let { vm ->
            initYoutubePlayer()
            initView()
        }
    }

    private fun initYoutubePlayer() {
        viewModel!!.let { vm ->
            // 화면이 중지되면 자동 재생 중지
            lifecycle.addObserver(youtubePlayerView)

            val customPlayerUi = youtubePlayerView.inflateCustomPlayerUi(R.layout.custom_player_ui)
            youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

                override fun onReady(youTubePlayer: YouTubePlayer) {

                    vm.customPlayerUiController = CustomPlayerUiController(requireContext(), customPlayerUi, youTubePlayer, youtubePlayerView, vm.isHost).apply {
                        realtimeChecked = { vm.isRealtimeUsed.value = it }
                        seekbarChanged = { time ->
                            vm.seekbarYoutubeClicked(time)
                            vm.curSeekbarPos.value = time
                        }
                        changeChatVisibility = { visible ->
                            vm.openFullScreenChatView?.invoke(visible)
                        }
                    }
                    youTubePlayer.addListener(vm.customPlayerUiController)
                    youtubePlayerView.addFullScreenListener(vm.customPlayerUiController)

                    // 풀스크린 뷰 설정
                    youtubePlayerView.addFullScreenListener(object :
                        YouTubePlayerFullScreenListener {
                        override fun onYouTubePlayerEnterFullScreen() {
                            vm.mFullScreenController.rotate(true)
                        }

                        override fun onYouTubePlayerExitFullScreen() {
                            vm.mFullScreenController.rotate(false)
                        }
                    })

                    // 영상 선택
                    vm.curVideoSelected.observe(viewLifecycleOwner) {
                        if (it.duration != -1f)
                            vm.setNewYoutubeVideoSelected(it.videoId)
                    }

                    // 새 영상이 실행됨
                    vm.curVideoPlayed.observe(viewLifecycleOwner) {
                        if (it.duration != -1f) {
                            youTubePlayer.loadVideo(it.videoId, vm.curSeekbarPos.value ?: 0.0f)
                            vm.customPlayerUiController.newVideoPlayed()
                            vm.setNewYoutubeVideoPlayed(it, vm.curSeekbarPos.value ?: 0.0f)
                            requireActivity().draggablePanel.getFrameFirst().curPlayVideoTitle.text =
                                it.title
                        }
                    }

                    // SeekBar의 위치 변동됨
                    vm.curSeekbarPos.observe(viewLifecycleOwner) {
                        if (it != -1f)
                            vm.setYoutubeVideoSeekbarChanged(it)
                    }

                    // 실시간 정보 on / off
                    vm.isRealtimeUsed.observe(viewLifecycleOwner) {
                        if (it) {
                            // 현재 재생 정보 요청
                           vm.requestVideoPlayState { playState, curTime, saveTime ->
                               // 흐른 시간 계산
                               val timeElapsed = (curTime - saveTime).toFloat() / 1000
                               val restVideoTime = playState.curVideo.duration - playState.seekbar

                               // 현재 영상이 끝나지 않음
                               if (restVideoTime > timeElapsed) {
                                   vm.curSeekbarPos.value = playState.seekbar + timeElapsed
                                   vm.curVideoPlayed.value = playState.curVideo
                               }
                               else vm.PlayNextVideo(playState.curVideo, timeElapsed - restVideoTime)
                           }
                            // 재생바 감지
                            vm.initContent_Youtube {
                                youTubePlayer.seekTo(it)
                            }
                            // 영상 선택 감지
                            vm.notifyNewVideoSelected { videoId ->
                                vm.retriveVideoById(videoId)?.let {
                                    vm.curSeekbarPos.value = 0f
                                    vm.curVideoSelected.value = it
                                    vm.curVideoPlayed.value = it
                                }
                            }
                        }
                        else vm.inActiveYoutubeRealtimeListener()
                    }

                    vm.isJoinRoom.observe(viewLifecycleOwner) {
                        if (it) vm.customPlayerUiController.checkRealtime(true)
                        else youTubePlayer.pause()
                    }
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    when (state) {
                        PlayerConstants.PlayerState.ENDED -> {
                            vm.PlayNextVideo(vm.curVideoPlayed.value!!, 0f)
                        }
                    }
                }
            })
        }
    }

    private fun initView() {
        viewModel!!.let { vm ->

            // 백 버튼용 리스너
            // 기기만 회전할 경우 유튜브 뷰의 크기가 줄어들지 않음
            vm.mFullScreenController.contentExitFullScreenMode = {
                youtubePlayerView.exitFullScreen()
            }

            // 키보드가 표시되면 유튜브 클릭을 방지하는 버튼 생성
            vm.contentAvailability = { isKeyboardVisible ->
                if (isKeyboardVisible)
                    btnCoverScreen.visibility = View.VISIBLE
                else btnCoverScreen.visibility = View.GONE
            }

            // 버튼을 누르면 키보드 종료
            btnCoverScreen.setOnClickListener {
                vm.hideKeyboard?.invoke()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel!!.let { vm ->
            vm.isRealtimeUsed.value = false
            vm.customPlayerUiController.awayFromYoutubePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.contentAvailability = null
    }
}