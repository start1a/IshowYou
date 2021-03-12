package com.start3a.ishowyou.room.content.videoselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import kotlinx.android.synthetic.main.activity_youtube_video_selection.*

class YoutubeVideoSelectionActivity : AppCompatActivity() {

    private var viewModel: YoutubeVideoSelectionViewModel? = null
    private var listVideoAdapter: YoutubeVideoListAdapter? = null
    private var listVideoSelectedAdapter: YoutubeVideoSelectedListAdapter? = null

    private var setVideoDuration: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_video_selection)
        supportActionBar?.title = "유튜브 영상 검색"

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(YoutubeVideoSelectionViewModel::class.java).apply {
                    init(applicationContext)
                }
        }

        viewModel!!.let { vm ->
            initVideoListAdapter()
            initVideoSelectedListAdapter()

            val selList = vm.listVideoSelected.value!!
            lifecycle.addObserver(videoSelectionPlayer)
            videoSelectionPlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)

                    setVideoDuration = {
                        youTubePlayer.loadVideo(selList[vm.indexDurationSave].videoId, 0f)
                        youTubePlayer.mute()
                    }
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    super.onVideoDuration(youTubePlayer, duration)
                    selList[vm.indexDurationSave].duration = duration
                    // 다음 영상이 있으면 계속 작업
                    if (vm.indexDurationSave + 1 < selList.size)
                        youTubePlayer.loadVideo(selList[++vm.indexDurationSave].videoId, 0f)
                    else {
                        if (vm.isEndVideoSelection) endVideoSelection()
                        vm.isLoadVideosStarted = false
                    }
                }
            })
        }
    }

    override fun onBackPressed() {
        val vm = viewModel!!
        val selList = vm.listVideoSelected.value!!

        // 아직 duration 추출 작업 중
        if (vm.indexDurationSave < selList.size) {
            vm.isEndVideoSelection = true

            // 저장 중 로딩 Ui 출력
            loading_layout.visibility = View.VISIBLE
        }
        else endVideoSelection()
    }

    private fun initVideoListAdapter() {
        viewModel!!.let { vm ->
            listVideoAdapter = YoutubeVideoListAdapter(vm.listVideo.value!!).apply {
                // 아이템 추가
                videoClicked = { pos ->
                    // 중복 추가 방지
                    val selList = vm.listVideoSelected.value!!
                    var indexSearched = -1
                    for (i in 0 until selList.size)
                        if (list[pos] == selList[i]) {
                            indexSearched = i
                            break
                        }

                    if (indexSearched == -1) {
                        vm.listVideoSelected.add(list[pos])
                        // 비디오 duration 추출 작업 시작
                        if (!vm.isLoadVideosStarted) {
                            vm.isLoadVideosStarted = true
                            setVideoDuration?.invoke()
                        }
                    }
                }
            }
            videoRecyclerView.adapter = listVideoAdapter
            videoRecyclerView.layoutManager = LinearLayoutManager(this)

            vm.listVideo.observe(this, {
                listVideoAdapter?.notifyDataSetChanged()
            })
        }
    }

    private fun initVideoSelectedListAdapter() {
        viewModel!!.let { vm ->
            listVideoSelectedAdapter =
                YoutubeVideoSelectedListAdapter(vm.listVideoSelected.value!!).apply {
                    // 뷰 클릭 시 해당 아이템이 검색된 리스트에 있으면 스크롤
                    videoClicked = { pos ->
                        val vList = vm.listVideo.value!!
                        for (i in 0 until vList.size)
                            if (vList[i] == list[pos]) {
                                videoRecyclerView.scrollToPosition(i)
                                break
                            }
                    }

                    videoDeleted = { pos ->
                        vm.listVideoSelected.removeAt(pos)
                    }
                }
            videoSelectRecyclerView.adapter = listVideoSelectedAdapter
            videoSelectRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            vm.listVideoSelected.observe(this) {
                listVideoSelectedAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_search, menu)
        // 메뉴에서 검색
        (menu!!.findItem(R.id.action_search).actionView as SearchView).let { searchView ->
            searchView.maxWidth = Int.MAX_VALUE
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {

                    // 검색 성공
                    if (!query.isNullOrEmpty())
                        viewModel!!.getVideosByKeyword(query)

                    return true
                }

                override fun onQueryTextChange(newText: String?) = true
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun endVideoSelection() {
        val list = viewModel!!.listVideoSelected.value!! as ArrayList<YoutubeSearchData>
        if (list.size > 0) {
            val intent = Intent().apply {
                putParcelableArrayListExtra("videos", list)
            }
            setResult(Activity.RESULT_OK, intent)
        }
        finish()
    }
}