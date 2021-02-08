package com.start3a.ishowyou

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_video_selection)

        viewModel = application!!.let {
            ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory(it))
                .get(YoutubeVideoSelectionViewModel::class.java)
        }

        viewModel!!.let { vm ->
            initAdapter()
        }
    }

    private fun initAdapter() {
        viewModel!!.let { vm ->
            listVideoAdapter = YoutubeVideoListAdapter(vm.listVideo.value!!).apply {
                videoClicked = {
                    Log.d(TAG, "${vm.listVideo.value!![it].title}\n${vm.listVideo.value!![it].channelTitle}")
                }
            }
            videoRecyclerView.adapter = listVideoAdapter
            videoRecyclerView.layoutManager = LinearLayoutManager(this)

            vm.listVideo.observe(this, {
                listVideoAdapter?.notifyDataSetChanged()
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_search, menu)
        (menu!!.findItem(R.id.action_search).actionView as SearchView).let { searchView ->
            searchView.maxWidth = Int.MAX_VALUE
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    Log.d(TAG, query.toString())
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
                            val data = response.body()!!
                            val list = mutableListOf<YoutubeSearchData>()
                            data.items.forEach {
                                Log.d(TAG, "item : ${it.snippet.title}")
                                val video = YoutubeSearchData(
                                    it.snippet.title,
                                    it.snippet.description,
                                    it.snippet.channelTitle,
                                    it.snippet.thumbnails.high.url
                                )
                                list.add(video)
                            }
                            viewModel!!.listVideo.addAll(list)
                        }
                    }

                    override fun onFailure(
                        call: Call<YoutubeSearchJsonData>,
                        t: Throwable
                    ) {
                        Log.d(TAG, "youtube video search is failed.\n$t")
                    }

                })
        }.run()
    }
}