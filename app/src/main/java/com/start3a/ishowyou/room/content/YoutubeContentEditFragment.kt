package com.start3a.ishowyou.room.content

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.room.ChatRoomViewModel
import com.start3a.ishowyou.room.content.videoselection.YoutubeVideoSelectionActivity
import kotlinx.android.synthetic.main.fragment_youtube_content_edit.*
import kotlinx.android.synthetic.main.layout_draggable_bottom.*


class YoutubeContentEditFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null
    private var listVideoAdapter: YoutubePlayListAdapter? = null

    private lateinit var curTab: BottomNavigationView

    // onAcitivityResult (deprecated)
    // -> ActivityResultContracts
    private val requestActivityForSearchVideos: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            // 비디오 리스트
            if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                activityResult.data!!.extras!!.getParcelableArrayList<YoutubeSearchData>("videos")?.let { videos ->
                    viewModel!!.addVideoToPlaylist_Youtube(videos)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_youtube_content_edit, container, false)
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
            initListData()
            initAdapter()
        }
    }

    private fun initListData() {
        viewModel!!.let { vm ->
            vm.curVideoPlayed.observe(viewLifecycleOwner) {
                if (it.duration != -1f) {
                    listVideoAdapter?.videoPlayed = it
                    listVideoAdapter?.notifyDataSetChanged()
                }
            }

            curTab = requireActivity().bottom_navigation_chatroom
            val badge = curTab.getOrCreateBadge(R.id.action_contents).run {
                number = 0
                isVisible = false
                this
            }
            vm.notifyPlayListChanged(
                // 비디오 리스트에 추가
                {
                    vm.listPlayYoutube.add(it)
                    if (curTab.selectedItemId != R.id.action_contents && !vm.isHost) {
                        badge.number = badge.number + 1
                        badge.isVisible = true
                    }
                },

                // 비디오 리스트에서 제거
                {
                    val list = vm.listPlayYoutube.value!!
                    var indexSearched = -1
                    for (i in 0 until list.size)
                        if (list[i] == it) {
                            indexSearched = i
                        }

                    if (indexSearched != -1)
                        vm.listPlayYoutube.removeAt(indexSearched)
                }
            )
        }
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->
            listVideoAdapter = YoutubePlayListAdapter(vm.listPlayYoutube.value!!, vm.curVideoPlayed.value, vm.isHost).apply {
                // 영상 클릭 시 정보 텍스트 표시
                videoClicked = {
                    val video = vm.listPlayYoutube.value!![it]
                    val curVideo = vm.curVideoPlayed.value!!

                    if (video.createdTime != curVideo.createdTime) {
                        if (!vm.isHost)
                            vm.customPlayerUiController.checkRealtime(false)

                        vm.curSeekbarPos.value = 0.0f
                        vm.curVideoSelected.value = video
                        vm.curVideoPlayed.value = video
                    }
                }

                // 비디오 삭제 버튼 클릭
                videoRemoved = {
                    val curVideo = vm.curVideoPlayed.value!!
                    if (!vm.isRealtimeUsed.value!!)
                        Toast.makeText(requireActivity(), "실시간 버튼을 켜 주세요.", Toast.LENGTH_LONG).show()
                    else if (curVideo.createdTime == list[it].createdTime)
                        Toast.makeText(requireActivity(), "재생 중인 영상은 제거할 수 없습니다.", Toast.LENGTH_LONG).show()
                    else vm.removeVideoPlaylist_Youtube(it)
                }
                // 새 비디오 추가 버튼
                videoAdd = {
                    val intent = Intent(requireActivity(), YoutubeVideoSelectionActivity::class.java)
                    requestActivityForSearchVideos.launch(intent)
                }
            }
            playlistRecyclerView.let {
                it.adapter = listVideoAdapter
                it.layoutManager = LinearLayoutManager(activity)
            }

            // 리스트 감지
            vm.listPlayYoutube.observe(viewLifecycleOwner) {
                listVideoAdapter?.notifyDataSetChanged()
            }
        }
    }
}