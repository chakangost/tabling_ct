package com.tabling.homework.views.activities

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tabling.homework.R
import kotlinx.android.synthetic.main.activity_image_detail_view.*

class ImageDetailActivity : AppCompatActivity() {

    private val scaleGestureDetector by lazy { ScaleGestureDetector(this, ScaleListener()) }
    private var mScaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail_view)
        init()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    private fun init() {
        initView()
        initListener()
    }

    private fun initView() {
        Glide.with(this)
            .load(intent.getStringExtra("imageUrl"))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.empty_img)
            .into(imageView)
    }

    private fun initListener() {
        image_viewer_back_button.setOnClickListener {
            finish()
        }
    }

    inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            mScaleFactor = 0.1f.coerceAtLeast(mScaleFactor.coerceAtMost(10.0f))
            imageView?.scaleX = mScaleFactor
            imageView?.scaleY = mScaleFactor
            return true
        }
    }
}