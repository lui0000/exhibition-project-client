package com.example.exhibitionapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exhibitionapp.databinding.FragmentHomeBinding
import com.example.exhibitionapp.services.ExhibitionService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var exhibitionService: ExhibitionService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)

        loadExhibitions()

        // Настраиваем BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_account -> {
                    findNavController().navigate(R.id.action_homeFragment_to_accountFragment)
                    true
                }
                R.id.navigation_create_exhibition -> {
                    findNavController().navigate(R.id.action_homeFragment_to_exhibitionManagementFragment)
                    true
                }
                else -> false
            }
        }

        // Скрываем кнопку создания выставки, если пользователь не "ORGANIZER"
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.navigation_create_exhibition).isVisible =
            getUserRoleFromSharedPreferences() == "ORGANIZER"
    }

    private fun getUserRoleFromSharedPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("userRole", "") ?: ""
        Log.d("HomeFragment", "Загружена роль пользователя: $role")
        return role
    }


    private fun getTokenFromSharedPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwtToken", null) ?: ""
    }

    private fun loadExhibitions() {
        lifecycleScope.launch {
            try {
                val token = getTokenFromSharedPreferences()
                if (token.isEmpty()) {
                    Log.e("HomeFragment", "Токен отсутствует!")
                    Toast.makeText(context, "Ошибка: Токен не найден", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = exhibitionService.getExhibitions("Bearer $token")

                if (response.isSuccessful) {
                    val exhibitions = response.body()
                    binding.recyclerView.adapter = exhibitions?.let {
                        ExhibitionAdapter(it) { exhibition ->
                            val bundle = Bundle().apply {
                                putParcelable("exhibition", exhibition)
                            }
                            findNavController().navigate(R.id.action_homeFragment_to_exhibitionsFragment, bundle)
                        }
                    }
                } else {
                    Log.e("HomeFragment", "Ошибка загрузки: ${response.message()}")
                    Toast.makeText(context, "Ошибка загрузки: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Ошибка сети: ${e.message}", e)
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

