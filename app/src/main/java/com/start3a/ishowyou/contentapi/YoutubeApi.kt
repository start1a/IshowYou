package com.start3a.ishowyou.contentapi

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


// Youtube search API
class RetrofitYoutubeService {

    fun getService(): YoutubeApi = retrofit.create(YoutubeApi::class.java)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

interface YoutubeApi {

    @GET("search")
    fun getSearchedVideoList(
        @Query("q") q: String,
        @Query("key") key: String = ApiKey.key,
        @Query("type") type: String = "video",
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResult: Int = 50
    ): Call<YoutubeSearchJsonData>

}

