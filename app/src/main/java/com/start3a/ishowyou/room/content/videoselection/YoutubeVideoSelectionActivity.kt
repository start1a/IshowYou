package com.start3a.ishowyou.room.content.videoselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.start3a.ishowyou.R
import com.start3a.ishowyou.contentapi.VideoSelectInfo
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import kotlinx.android.synthetic.main.activity_youtube_video_selection.*

class YoutubeVideoSelectionActivity : AppCompatActivity() {

    private var viewModel: YoutubeVideoSelectionViewModel? = null
    private var listVideoAdapter: YoutubeVideoListAdapter? = null
    private var listVideoSelectedAdapter: YoutubeVideoSelectedListAdapter? = null
    private var listSearchHistoryAdapter: SearchHistoryAdapter? = null

    private var mSearchView: SearchView? = null
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
            initView()
            initVideoListAdapter()
            initVideoSelectedListAdapter()
            initSearchHistoryListAdapter()

            val selList = vm.listVideoSelected.value!!
            lifecycle.addObserver(videoSelectionPlayer)
            videoSelectionPlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)

                    setVideoDuration = {
                        youTubePlayer.loadVideo(selList[++vm.indexDurationSave].videoId, 0f)
                        notifyExtractIndex(vm.indexDurationSave)
                    }
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    super.onVideoDuration(youTubePlayer, duration)
                    selList[vm.indexDurationSave].duration = duration
                    // 다음 영상이 있으면 계속 작업
                    if (vm.indexDurationSave + 1 < selList.size) {
                        notifyExtractIndex(++vm.indexDurationSave)
                        youTubePlayer.loadVideo(selList[vm.indexDurationSave].videoId, 0f)
                    }
                    // 작업 완료
                    else {
                        if (vm.isEndVideoSelection) endVideoSelection()
                        vm.isLoadVideosStarted = false
                        notifyExtractIndex(-1)
                    }
                }

                override fun onStateChange(
                        youTubePlayer: YouTubePlayer,
                        state: PlayerConstants.PlayerState
                    ) {
                        super.onStateChange(youTubePlayer, state)

                        when (state) {
                            PlayerConstants.PlayerState.PLAYING ->
                                youTubePlayer.pause()
                            PlayerConstants.PlayerState.BUFFERING ->
                                youTubePlayer.mute()
                        }
                }

                override fun onError(
                    youTubePlayer: YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {
                    super.onError(youTubePlayer, error)

                    when (error) {
                        PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER ->
                            Toast.makeText(applicationContext, "재생이 불가능한 비디오입니다.", Toast.LENGTH_SHORT).show()
                        PlayerConstants.PlayerError.VIDEO_NOT_FOUND ->
                            Toast.makeText(applicationContext, "비디오를 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(applicationContext, "error : $error", Toast.LENGTH_SHORT).show()
                    }

                    // 선택 삭제
                    vm.selectedVideoInfoList.let {
                        if (it[vm.indexDurationSave].keyword == vm.curQueryKeyword) {
                            val index = it[vm.indexDurationSave].indexToSearchList
                            notifySearchedVideoCheck(index, -1)
                        }
                        selList.removeAt(vm.indexDurationSave)
                        it.removeAt(vm.indexDurationSave)
                        repositionVideoChecking(vm.indexDurationSave)
                    }

                    // 다음
                    if (vm.indexDurationSave < selList.size)
                        youTubePlayer.loadVideo(selList[vm.indexDurationSave].videoId, 0f)
                    else {
                        vm.indexDurationSave--
                        vm.isLoadVideosStarted = false
                        notifyExtractIndex(-1)
                    }
                }
            })
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initView() {
        val vm = viewModel!!

        btnShowCurWatchedVideos.setOnClickListener {
            vm.getCurWatchedVideos({
                btnShowCurWatchedVideos.visibility = View.GONE
                listVideoAdapter!!.initSelectionList()
            }, {
                Toast.makeText(applicationContext, "검색 결과가 존재하지 않습니다.", Toast.LENGTH_LONG).show()
            })
        }

        btnRoomCreate.setOnClickListener {
            // 아직 duration 추출 작업 중
            if (vm.isLoadVideosStarted) {
                vm.isEndVideoSelection = true

                // 저장 중 로딩 Ui 출력
                loadingView(true)
            } else endVideoSelection()
        }

        loading_layout.setOnClickListener {}
    }

    private fun initVideoListAdapter() {
        viewModel!!.let { vm ->
            listVideoAdapter = YoutubeVideoListAdapter(vm.listVideo.value!!).apply {
                // 아이템 클릭
                videoClicked = { pos ->
                    // 중복 여부 체크
                    val indexSearched = vm.listVideoSelected.findIndex(list[pos])

                    val selVidInfoList = vm.selectedVideoInfoList
                    // 추가
                    if (indexSearched == -1) {

                        vm.listVideoSelected.add(list[pos])
                        selVidInfoList.add(VideoSelectInfo(vm.curQueryKeyword, pos))
                        notifySearchedVideoCheck(pos, selVidInfoList.lastIndex)

                        // 비디오 duration 추출 작업 시작
                        if (!vm.isLoadVideosStarted) {
                            vm.isLoadVideosStarted = true
                            setVideoDuration?.invoke()
                        }
                    }
                    // 삭제
                    else {
                        // 추출 중이면
                        if (vm.isLoadVideosStarted && vm.indexDurationSave == selectionList[pos])
                            Toast.makeText(applicationContext, "로딩이 완료된 후 제거가 가능합니다.", Toast.LENGTH_SHORT).show()

                        else {
                            vm.listVideoSelected.removeAt(selectionList[pos])
                            selVidInfoList.removeAt(selectionList[pos])
                            repositionVideoChecking(selectionList[pos])
                            selectionList[pos] = -1
                            --vm.indexDurationSave
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
                        val indexSearched = vm.listVideo.findIndex(list[pos])
                        if (indexSearched != -1)
                            videoRecyclerView.scrollToPosition(indexSearched)
                    }

                    videoDeleted = { pos ->
                        val selInfoList = vm.selectedVideoInfoList

                        vm.listVideoSelected.removeAt(pos)
                        val index = selInfoList[pos].indexToSearchList
                        if (selInfoList[pos].keyword == vm.curQueryKeyword)
                            notifySearchedVideoCheck(index, -1)
                        selInfoList.removeAt(pos)
                        repositionVideoChecking(pos)
                        --vm.indexDurationSave
                    }
                }
            videoSelectRecyclerView.adapter = listVideoSelectedAdapter
            videoSelectRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            vm.listVideoSelected.observe(this) {
                listVideoSelectedAdapter?.notifyDataSetChanged()

                if (it.size > 0) btnRoomCreate.visibility = View.VISIBLE
                else btnRoomCreate.visibility = View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_search, menu)

        // 메뉴에서 검색
        mSearchView = menu!!.findItem(R.id.action_search).actionView as SearchView

        mSearchView!!.let {
            it.maxWidth  = Int.MAX_VALUE

            it.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val vm = viewModel!!
                    // 검색 성공
                    if (query != null && vm.isQueryAvailable(query)) {
                        listVideoAdapter?.initSelectionList()
                        queryVideos(query)
                        vm.insertSearchKeyword(query)
                        btnShowCurWatchedVideos.visibility = View.VISIBLE
                    }

                    if (!query.isNullOrEmpty() && query.isNotBlank())
                        mSearchView!!.clearFocus()

                    return true
                }

                override fun onQueryTextChange(newText: String?) = true
            })

            it.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    listSearchHistoryAdapter?.notifyDataSetChanged()
                    historySearchRecyclerView.visibility = View.VISIBLE
                    btnShowCurWatchedVideos.visibility = View.GONE
                } else {
                    historySearchRecyclerView.visibility = View.GONE
                    btnShowCurWatchedVideos.visibility = View.VISIBLE
                }
            }

            it.isIconified = false
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun initSearchHistoryListAdapter() {
        viewModel!!.let { vm ->
            vm.getAllSearchHistory()

            listSearchHistoryAdapter =
                SearchHistoryAdapter(vm.listSearchHistory).apply {
                    viewClicked = { pos ->
                        val keyword = list[pos].keyword

                        if (vm.isQueryAvailable(keyword)) {
                            listVideoAdapter!!.initSelectionList()
                            queryVideos(keyword)
                        }
                        vm.updateSearchKeyword(pos)
                        mSearchView!!.run {
                            setQuery(keyword, false)
                            clearFocus()
                        }
                    }

                    pasteButtonClicked = { pos ->
                        mSearchView!!.setQuery(list[pos].keyword, false)
                    }
                }
            historySearchRecyclerView.adapter = listSearchHistoryAdapter
            historySearchRecyclerView.layoutManager = LinearLayoutManager(this)
        }
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

    private fun notifyExtractIndex(index: Int) {
        listVideoSelectedAdapter!!.let {
            it.indexExtrating = index
            it.notifyDataSetChanged()
        }
    }

    private fun notifySearchedVideoCheck(index: Int, selectedIndex: Int) {
        listVideoAdapter!!.let {
            it.selectionList[index] = selectedIndex
            it.notifyItemChanged(index)
        }
    }

    private fun loadingView(visible: Boolean) {
        if (visible) loading_layout.visibility = View.VISIBLE
        else loading_layout.visibility = View.GONE
    }

    private fun queryVideos(keyword: String) {
        viewModel!!.let { vm ->
            vm.curQueryKeyword = keyword
            loadingView(true)
            vm.getVideosByKeyword(keyword) {
                loadingView(false)
                checkContainVideoInSelectionList()
            }
        }
    }

    private fun checkContainVideoInSelectionList() {
        viewModel!!.let { vm ->
            val listSelected = vm.listVideoSelected

            if (listSelected.value!!.size == 0)
                return

            for (i in 0 until vm.selectedVideoInfoList.size) {
                val item = vm.selectedVideoInfoList[i]

                if (item.keyword == vm.curQueryKeyword)
                    listVideoAdapter!!.selectionList[item.indexToSearchList] = i
            }

            listVideoAdapter?.notifyDataSetChanged()
        }
    }

    // 선택한 비디오를 삭제 후 뒷 번호의 비디오의 체크 위치 1칸 조정
    private fun repositionVideoChecking(startIndex: Int) {
        viewModel!!.let { vm ->
            val selInfolist = vm.selectedVideoInfoList
            val size = vm.listVideoSelected.value!!.size

            for (i in startIndex until size) {
                if (selInfolist[i].keyword == vm.curQueryKeyword) {
                    val index = selInfolist[i].indexToSearchList
                    listVideoAdapter!!.selectionList[index] -= 1
                }
            }
            listVideoAdapter!!.notifyDataSetChanged()
        }
    }
}