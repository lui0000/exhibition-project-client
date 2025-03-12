package com.example.exhibitionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.example.exhibitionapp.databinding.FragmentAppForArtistsBinding

class AppForArtistsFragment : Fragment() {

    private var _binding: FragmentAppForArtistsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppForArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Пример данных для выставки
        val exhibitions = listOf("Выставка 1", "Выставка 2", "Выставка 3")

        // Создание адаптера для AutoCompleteTextView
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, exhibitions)

        // Установка адаптера для AutoCompleteTextView
        val autoCompleteTextView = binding.exhibitionSpinner
        autoCompleteTextView.setAdapter(adapter)

        // Обработка выбора элемента (если необходимо)
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedExhibition = parent.getItemAtPosition(position).toString()
            // Используйте выбранную выставку по вашему усмотрению
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
