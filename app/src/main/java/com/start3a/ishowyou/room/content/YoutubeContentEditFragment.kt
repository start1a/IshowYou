package com.start3a.ishowyou.room.content

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
    private val requestActivityForSearchVideos: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        // 비디오 리스트
        if (activityResult.resultCode == Activity.RESULT_OK && activityResult.data != null) {
            val videos = activityResult.data!!.extras!!.getParcelableArrayList<YoutubeSearchData>("videos")
            viewModel!!.listPlayYoutube.addAll(videos!!)
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

        }
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->
            listVideoAdapter = YoutubePlayListAdapter(vm.listPlayYoutube.value!!).apply {
                videoClicked = {
                    val video = vm.listPlayYoutube.value!![it]
                    vm.curVideoSelected.value = video
                }
            }
            playlistRecyclerView.adapter = listVideoAdapter
            playlistRecyclerView.layoutManager = LinearLayoutManager(activity)

            // 리스트 감지
            vm.listPlayYoutube.observe(viewLifecycleOwner) {
                listVideoAdapter?.notifyDataSetChanged()
            }

            // 영상 선택 감지
            vm.curVideoSelected.observe(viewLifecycleOwner) {
                textVideoTitle.text = it.title
                textVideoDesc.text = it.desc
                textVideoChannelTitle.text = it.channelTitle
            }

            btnAddVideo.setOnClickListener {
                val intent = Intent(requireActivity(), YoutubeVideoSelectionActivity::class.java)
                requestActivityForSearchVideos.launch(intent)
            }
        }
    }
}