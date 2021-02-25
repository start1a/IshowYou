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
import com.start3a.ishowyou.R
import com.start3a.ishowyou.YoutubeVideoSelectionActivity
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.room.ChatRoomViewModel
import kotlinx.android.synthetic.main.fragment_youtube_content_edit.*


class YoutubeContentEditFragment : Fragment() {

    private var viewModel: ChatRoomViewModel? = null
    private var listVideoAdapter: YoutubePlayListAdapter? = null

    // onAcitivityResult (deprecated)
    // -> ActivityResultContracts
    private val requestActivityForSearchVideos: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            // 비디오 리스트
            if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
                val videos =
                    activityResult.data!!.extras!!.getParcelableArrayList<YoutubeSearchData>("videos")!!
                viewModel!!.addVideoToPlaylist_Youtube(videos)
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
            initView()
            initAdapter()
        }
    }

    private fun initView() {
        viewModel!!.let { vm ->
            // 영상 선택 감지
            vm.curVideoSelected.observe(viewLifecycleOwner) {
                updateVideoInfo(it)
            }

            vm.curVideoPlayed.observe(viewLifecycleOwner) {
                updateVideoInfo(it)
            }

            vm.initContentEdit_Youtube(
                // 비디오 리스트에 추가
                { vm.listPlayYoutube.add(it) },

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

            if (!vm.isHost)
                btnAddVideo.visibility = View.GONE
        }
    }

    private fun updateVideoInfo(video: YoutubeSearchData) {
        textVideoTitle.text = video.title
        textVideoDesc.text = video.desc
        textVideoChannelTitle.text = video.channelTitle
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->
            listVideoAdapter = YoutubePlayListAdapter(vm.listPlayYoutube.value!!, vm.isHost).apply {
                // 영상 클릭 시 정보 텍스트 표시
                videoClicked = {
                    val video = vm.listPlayYoutube.value!![it]
                    vm.curVideoSelected.value = video
                    vm.curVideoPlayed.value = video
                    vm.curSeekbarPos.value = 0.0f
                }
                // 비디오 삭제 버튼 클릭
                videoRemoved = {
                    val curVideo = vm.curVideoPlayed.value!!
                    if (curVideo.createdTime == list[it].createdTime)
                        Toast.makeText(requireActivity(), "재생 중인 영상은 제거할 수 없습니다.", Toast.LENGTH_LONG).show()
                    else vm.removeVideoPlaylist_Youtube(it)
                }
            }
            playlistRecyclerView.adapter = listVideoAdapter
            playlistRecyclerView.layoutManager = LinearLayoutManager(activity)

            // 리스트 감지
            vm.listPlayYoutube.observe(viewLifecycleOwner) {
                listVideoAdapter?.notifyDataSetChanged()
            }

            btnAddVideo.setOnClickListener {
                val intent = Intent(requireActivity(), YoutubeVideoSelectionActivity::class.java)
                requestActivityForSearchVideos.launch(intent)
            }
        }
    }
}