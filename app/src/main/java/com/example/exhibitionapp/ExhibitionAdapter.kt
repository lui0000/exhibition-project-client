package com.example.exhibitionapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExhibitionAdapter(
    private val exhibitions: List<Exhibition>,
    private val onItemClick: (Exhibition) -> Unit // Добавьте лямбда-функцию для обработки кликов
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

    class ExhibitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.exhibition_title)
        private val description: TextView = itemView.findViewById(R.id.exhibition_description)
        private val image: ImageView = itemView.findViewById(R.id.exhibition_image)

        fun bind(exhibition: Exhibition) {
            title.text = exhibition.title
            description.text = exhibition.description
            image.setImageResource(exhibition.imageResId)
        }
    }
}
