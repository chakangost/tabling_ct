package com.tabling.homework.views.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tabling.homework.R
import com.tabling.homework.api.ApiManager
import com.tabling.homework.model.PicSumPhoto
import com.tabling.homework.utils.Variables.isNetworkConnected
import com.tabling.homework.views.adapter.PhotosAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivity"
    private val dataSet: ArrayList<PicSumPhoto?> by lazy { ArrayList<PicSumPhoto?>() }
    private val adapter: PhotosAdapter by lazy { PhotosAdapter(this, dataSet) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        fetchPicSumPhotos()
    }

    private fun initView() {
        rv_main_list.run {
            rv_main_list.layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
            layoutAnimation = AnimationUtils.loadLayoutAnimation(
                this@MainActivity,
                R.anim.layout_fall_down
            )
            adapter = this@MainActivity.adapter
        }
        val decoration = DividerItemDecoration(this, LinearLayout.VERTICAL)
        rv_main_list.addItemDecoration(decoration)
    }

    @SuppressLint("CheckResult")
    private fun fetchPicSumPhotos() {
        if (!isNetworkConnected) {
            toast("네트워크 연결을 확인하세요")
            return
        }
        ApiManager.apiService.fetchPicSumPhotos()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                dataSet.clear()
                dataSet.addAll(it)
                adapter.notifyDataSetChanged()
                Log.e(tag, "success")
            }, {
                Log.e(tag, "fail : ${it.printStackTrace()}")
            })
    }
}