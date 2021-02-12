package com.start3a.ishowyou

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.start3a.ishowyou.contentapi.RetrofitYoutubeService
import com.start3a.ishowyou.contentapi.YoutubeSearchData
import com.start3a.ishowyou.contentapi.YoutubeSearchJsonData
import kotlinx.android.synthetic.main.activity_youtube_video_selection.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YoutubeVideoSelectionActivity : AppCompatActivity() {

    private val TAG = "YoutubeVideoSelectionActivity"
    private var viewModel: YoutubeVideoSelectionViewModel? = null
    private var listVideoAdapter: YoutubeVideoListAdapter? = null
    private var listVideoSelectedAdapter: YoutubeVideoSelectedListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_video_selection)
        supportActionBar?.title = "유튜브 영상 검색"

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(YoutubeVideoSelectionViewModel::class.java)
        }

        viewModel!!.let { vm ->
            initVideoListAdapter()
            initVideoSelectedListAdapter()
        }
    }

    override fun onBackPressed() {
        val list = viewModel!!.listVideoSelected.value!! as ArrayList<YoutubeSearchData>
        val intent = Intent().apply {
            putParcelableArrayListExtra("videos", list)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
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

                    if (indexSearched == -1)
                        vm.listVideoSelected.add(list[pos])
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
                        requestVideoList(query)

                    return true
                }

                override fun onQueryTextChange(newText: String?) = true
            })
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun requestVideoList(query: String) {
        Runnable {
            RetrofitYoutubeService.getService().getSearchedVideoList(query)
                .enqueue(object :
                    Callback<YoutubeSearchJsonData> {
                    override fun onResponse(
                        call: Call<YoutubeSearchJsonData>,
                        response: Response<YoutubeSearchJsonData>
                    ) {
                        if (response.isSuccessful) {
                            val vList = viewModel!!.listVideo.apply { value!!.clear() }

                            val searchedVideoList = mutableListOf<YoutubeSearchData>()
                            response.body()!!.items.forEach {
                                val video = YoutubeSearchData().apply {
                                    title = it.snippet.title
                                    desc = it.snippet.description
                                    channelTitle = it.snippet.channelTitle
                                    videoId = it.id.videoId
                                    thumbnail = it.snippet.thumbnails.high.url
                                    thumbnailSmall = it.snippet.thumbnails.default.url
                                }
                                searchedVideoList.add(video)
                            }
                            vList.addAll(searchedVideoList)
                        }
                    }

                    override fun onFailure(call: Call<YoutubeSearchJsonData>, t: Throwable) {
                        Log.d(TAG, "youtube video search is failed.\n$t")
                    }
                })
        }.run()
    }
}