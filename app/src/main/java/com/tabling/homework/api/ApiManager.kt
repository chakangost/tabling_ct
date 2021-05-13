package com.tabling.homework.api

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.GsonBuilder
import com.tabling.homework.BuildConfig
import com.tabling.homework.application.HomeworkApplication
import com.tabling.homework.store.PersistentCookieStore
import com.tabling.homework.utils.GsonDateFormatAdapter
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import java.util.concurrent.TimeUnit

object ApiManager {

    private val BASE_URL: String by lazy {
        if (BuildConfig.DEBUG) {
            Urls.DEVELOP_URL
        } else {
            Urls.RELEASE_URL
        }
    }

    val apiService: ApiService by lazy {
        val context: Context = HomeworkApplication.getInstance()
        val cookieStore = PersistentCookieStore(context)
        val cookieManager = CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL)

        val client: OkHttpClient = OkHttpClient.Builder().apply {
            cookieJar(JavaNetCookieJar(cookieManager))
            addNetworkInterceptor(UserAgentInterceptor())
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(10, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                addNetworkInterceptor(StethoInterceptor())
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            }
        }.build()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(
                GsonConverterFactory.create(
                GsonBuilder()
                .registerTypeAdapter(Date::class.java, GsonDateFormatAdapter())
                .setPrettyPrinting()
                .create()))
            .client(client)
            .build()
        retrofit.create(ApiService::class.java)
    }

    private class UserAgentInterceptor: Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            return chain.proceed(original.newBuilder().header("User-Agent", "ProjectAndroid/${BuildConfig.VERSION_NAME}")
                .url(original.url().toString()).build())
        }
    }
}