package com.example.exhibitionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exhibitionapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Установка LayoutManager для RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        // Пример данных для RecyclerView
        val exhibitions = listOf(
            Exhibition("Выставка 1", "Описание выставки 1", R.drawable.image1),
            Exhibition("Выставка 2", "Описание выставки 2", R.drawable.image3)
        )

        // Установка адаптера для RecyclerView
        val adapter = ExhibitionAdapter(exhibitions) { exhibition ->
            // Создание Bundle и передача данных о выставке
            val bundle = Bundle().apply {
                putParcelable("exhibition", exhibition)
            }
            // Навигация к ExhibitionDetailFragment с передачей данных
            findNavController().navigate(R.id.action_homeFragment_to_exhibitionsFragment, bundle)
        }
        binding.recyclerView.adapter = adapter

        // Установка обработчиков для BottomNavigationView
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_account -> {
                    // Навигация к AccountFragment
                    findNavController().navigate(R.id.action_homeFragment_to_accountFragment)
                    true
                }
                R.id.navigation_create_exhibition -> {
                    // Навигация к ExhibitionManagementFragment
                    findNavController().navigate(R.id.action_homeFragment_to_exhibitionManagementFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
