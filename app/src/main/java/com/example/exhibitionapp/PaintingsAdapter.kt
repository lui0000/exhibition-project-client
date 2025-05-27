package com.example.exhibitionapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PaintingsAdapter(private val paintingUrls: List<String>) :
    RecyclerView.Adapter<PaintingsAdapter.PaintingViewHolder>() {

    inner class PaintingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.painting_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaintingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_painting, parent, false)
        return PaintingViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaintingViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(paintingUrls[position])
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(holder.imageView)
    }

    override fun getItemCount() = paintingUrls.size
}