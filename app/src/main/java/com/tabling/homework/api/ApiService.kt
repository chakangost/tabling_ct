package com.tabling.homework.api

import com.tabling.homework.model.PicSumPhoto
import io.reactivex.Flowable
import retrofit2.http.GET

interface ApiService {

    @GET("/v2/list")
    fun fetchPicSumPhotos(): Flowable<List<PicSumPhoto>>
}