package com.start3a.ishowyou.room.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener
import com.start3a.ishowyou.R
import com.start3a.ishowyou.room.ChatRoomViewModel
import kotlinx.android.synthetic.main.fragment_youtube_player.*
import java.util.*

class YoutubePlayerFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null

    private var restoreNewTime: ((Float) -> Unit)? = null
    private var updatePlayedVideo: (() -> Unit)? = null
    private var requestVideoPlayState: (() -> Unit)? = null
    private val INTERVAL_VIDEO_SEEK = 10.0f

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
            youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

                override fun onReady(youTubePlayer: YouTubePlayer) {

                    youtubePlayerView.getPlayerUiController().setCustomAction1(
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fast_rewind_24)!!
                    ) {
                        val time = vm.timeCurVideo - INTERVAL_VIDEO_SEEK
                        vm.curSeekbarPos.value = time
                        youTubePlayer.seekTo(time)
                    }
                    youtubePlayerView.getPlayerUiController().setCustomAction2(ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_fast_forward_24)!!
                    ) {
                        val time = vm.timeCurVideo + INTERVAL_VIDEO_SEEK
                        vm.curSeekbarPos.value = time
                        youTubePlayer.seekTo(time)
                    }

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

                    restoreNewTime = { timeElapsed ->
                        val restVideoTime = vm.durationVideo - vm.timeCurVideo

                        // 해당 영상이 끝나지 않음
                        if (restVideoTime > timeElapsed) {
                            val time = vm.timeCurVideo + timeElapsed
                            youTubePlayer.seekTo(time)
                            youTubePlayer.play()
                            vm.curSeekbarPos.value = time
                        }
                        // 남은 재생 시간 초과
                        else {
                            val time = timeElapsed - restVideoTime
                            vm.PlayNextVideo(time)
                        }
                    }

                    requestVideoPlayState = {
                        vm.requestVideoPlayState { playState, curTime, saveTime ->
                            vm.curVideoPlayed.value = playState.curVideo
                            val timeElapsed = (curTime - saveTime).toFloat() / 1000
                            val restVideoTime = playState.duration - playState.seekbar

                            // 현재 영상이 끝나지 않음
                            if (restVideoTime > timeElapsed) {
                                youTubePlayer.loadVideo(
                                    playState.curVideo.videoId,
                                    playState.seekbar + timeElapsed
                                )
                            } else vm.PlayNextVideo(timeElapsed - restVideoTime)
                        }
                    }

                    // 영상 선택
                    vm.curVideoSelected.observe(viewLifecycleOwner) {
                        vm.setNewYoutubeVideoSelected(it.videoId)
                    }

                    // 새 영상이 실행됨
                    vm.curVideoPlayed.observe(viewLifecycleOwner) {
                        youTubePlayer.loadVideo(it.videoId, 0.0f)
                        updatePlayedVideo = {
                            vm.setNewYoutubeVideoPlayed(it, vm.durationVideo, vm.curSeekbarPos.value?:0.0f)
                        }
                    }

                    // SeekBar의 위치 변동됨
                    vm.curSeekbarPos.observe(viewLifecycleOwner) {
                        vm.setYoutubeVideoSeekbarChanged(it)
                    }

                    // SeekBar를 직접 조정
                    youtube_player_seekbar.youtubePlayerSeekBarListener =
                        object : YouTubePlayerSeekBarListener {
                            override fun seekTo(time: Float) {
                                youTubePlayer.seekTo(time)
                                vm.seekbarYoutubeClicked(time)
                                vm.curSeekbarPos.value = time
                            }
                        }

                    // 실시간 정보 on / off
                    vm.isRealtimeUsed.observe(viewLifecycleOwner) {
                        if (it) {
                            // 현재 재생 정보 요청
                            requestVideoPlayState?.invoke()
                            // 재생바 감지
                            vm.initContent_Youtube {
                                youTubePlayer.seekTo(it)
                            }
                            // 영상 선택 감지
                            vm.notifyNewVideoSelected { videoId ->
                                vm.retriveVideoById(videoId)?.let {
                                    vm.curVideoSelected.value = it
                                    vm.curVideoPlayed.value = it
                                }
                            }
                        }
                        else vm.inActiveYoutubeRealtimeListener()
                    }
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    when (state) {
                        PlayerConstants.PlayerState.ENDED -> {
                            vm.PlayNextVideo(0f)
                        }
                    }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)
                    vm.timeCurVideo = second
                }

                // 동영상이 로드될 때 호출
                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    super.onVideoDuration(youTubePlayer, duration)
                    vm.durationVideo = duration
                    // 새 영상이 실행될 때만 수행
                    // duration과 다른 데이터를 동시에 업로드하기 위해 onVideoDuration 호출 시점에 데이터를 업로드
                    updatePlayedVideo?.invoke()
                    updatePlayedVideo = null
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

            rbtnRealtime.setOnClickListener {
                rbtnRealtime.isChecked = !vm.isRealtimeUsed.value!!
                vm.isRealtimeUsed.value = !vm.isRealtimeUsed.value!!
            }
        }
    }

    override fun onStart() {
        super.onStart()
        restoreVideoPlayState()
    }

    override fun onStop() {
        super.onStop()
        viewModel!!.timeStopped = Date().time
    }

    // 영상 재생 상황으로 복귀
    private fun restoreVideoPlayState() {
        viewModel!!.let { vm ->
            if (vm.timeStopped != -1L) {
                // 방장이 방 멤버를 제어하지 않을 경우
                if (!vm.isRealtimeUsed.value!!) {
                    val timeStart = Date().time
                    val timeElapsed = (timeStart - vm.timeStopped).toFloat() / 1000
                    vm.timeStopped = -1
                    // 자리 비운 후 경과한 시간만큼 영상 재생이동
                    restoreNewTime?.invoke(timeElapsed)
                }
                else if (!vm.isHost) {
                    requestVideoPlayState?.invoke()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel!!.contentAvailability = null
    }
}