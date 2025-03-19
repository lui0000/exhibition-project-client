package com.example.exhibitionapp

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import kotlinx.coroutines.CoroutineStart

class ExhibitionAdapter(
    private var exhibitions: List<ExhibitionWithPaintingResponse>, // Измените на var
    private val onItemClick: (ExhibitionWithPaintingResponse) -> Unit
) : RecyclerView.Adapter<ExhibitionAdapter.ExhibitionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExhibitionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exhibition_item, parent, false)
        return ExhibitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExhibitionViewHolder, position: Int) {
        val exhibition = exhibitions[position]
        holder.bind(exhibition)
        holder.itemView.setOnClickListener { onItemClick(exhibition) }
    }

    override fun getItemCount(): Int = exhibitions.size

    // Добавляем метод для обновления данных
    fun updateData(newExhibitions: List<ExhibitionWithPaintingResponse>) {
        exhibitions = newExhibitions // Обновляем список
        notifyDataSetChanged() // Уведомляем RecyclerView об изменениях
    }

    class ExhibitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.exhibition_title)
        private val description: TextView = itemView.findViewById(R.id.exhibition_description)
        private val image: ImageView = itemView.findViewById(R.id.exhibition_image)

        fun bind(exhibition: ExhibitionWithPaintingResponse) {
            title.text = exhibition.title
            description.text = exhibition.description

            if (!exhibition.photoData.isNullOrEmpty()) {
                Glide.with(image.context)
                    .load(exhibition.photoData)
                    .placeholder(R.drawable.placeholder_image)
                    .into(image)
            } else {
                image.setImageResource(R.drawable.placeholder_image)
            }
        }
    }
}

