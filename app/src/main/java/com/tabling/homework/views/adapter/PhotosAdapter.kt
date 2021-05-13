package com.tabling.homework.views.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tabling.homework.R
import com.tabling.homework.model.PicSumPhoto
import com.tabling.homework.views.activities.ImageDetailActivity
import com.tabling.homework.views.activities.WebViewActivity
import org.jetbrains.anko.startActivity

class PhotosAdapter(
    private val activity: Activity, private val items: ArrayList<PicSumPhoto?>
) : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(activity)
            .load(items[position]?.downloadUrl)
            .override(items[position]!!.width, items[position]!!.height)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .thumbnail(0.1f)
            .placeholder(R.drawable.empty_img)
            .into(holder.photo)
        holder.author.text = items[position]!!.author
        holder.linkUrl.text = items[position]!!.url
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_picsum_photo,
                parent,
                false
            )
        )
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val parentLayout = view.findViewById(R.id.parent_layout) as ConstraintLayout
        val photo = view.findViewById(R.id.iv_photo) as ImageView
        val author = view.findViewById(R.id.tv_author) as TextView
        val linkUrl = view.findViewById(R.id.tv_link_url) as TextView

        init {
            linkUrl.setOnClickListener(this)
            parentLayout.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.parent_layout -> {
                    activity.startActivity<ImageDetailActivity>("imageUrl" to items[adapterPosition]?.downloadUrl)
                }
                R.id.tv_link_url -> {
                    activity.startActivity<WebViewActivity>("url" to items[adapterPosition]?.url)
                }
            }
        }
    }
}