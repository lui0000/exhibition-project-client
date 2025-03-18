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

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
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
    }

    private fun getTokenFromSharedPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }

    private fun loadExhibitions() {
        lifecycleScope.launch {
            try {
                val token = "Bearer ${getTokenFromSharedPreferences()}"
                Log.d("HomeFragment", "Запрос на получение выставок отправлен. Токен: $token")

                val response = exhibitionService.getExhibitions(token)

                Log.d("HomeFragment", "Ответ получен. Успех: ${response.isSuccessful}")
                Log.d("HomeFragment", "Код статуса: ${response.code()}")
                Log.d("HomeFragment", "Тело ответа: ${response.body()}")

                if (response.isSuccessful) {
                    val exhibitions = response.body() ?: emptyList()
                    Log.d("HomeFragment", "Получено ${exhibitions.size} выставок")

                    val adapter = ExhibitionAdapter(exhibitions) { exhibition ->
                        val bundle = Bundle().apply {
                            putParcelable("exhibition", exhibition)
                        }
                        findNavController().navigate(R.id.action_homeFragment_to_exhibitionsFragment, bundle)
                    }
                    binding.recyclerView.adapter = adapter

                } else {
                    Log.e("HomeFragment", "Ошибка загрузки: ${response.message()}")
                    response.errorBody()?.let { errorBody ->
                        Log.e("HomeFragment", "Тело ошибки: ${errorBody.string()}")
                    }
                    Toast.makeText(context, "Ошибка загрузки выставок", Toast.LENGTH_SHORT).show()
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
