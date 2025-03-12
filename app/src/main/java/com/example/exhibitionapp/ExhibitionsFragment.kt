package com.example.exhibitionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.exhibitionapp.databinding.FragmentExhibitionsBinding


class ExhibitionsFragment : Fragment() {
    private var _binding: FragmentExhibitionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExhibitionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получение данных о выставке из аргументов
        val exhibition = arguments?.getParcelable<Exhibition>("exhibition")

        // Установка данных в представление
        exhibition?.let {
            binding.exhibitionTitle.text = it.title
            binding.exhibitionDescription.text = it.description
            binding.exhibitionImage.setImageResource(it.imageResId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}