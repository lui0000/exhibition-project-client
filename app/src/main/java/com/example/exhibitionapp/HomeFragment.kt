package com.example.exhibitionapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exhibitionapp.databinding.FragmentHomeBinding
import com.example.exhibitionapp.services.ExhibitionService
import com.example.exhibitionapp.viewmodel.HomeViewModel
import androidx.appcompat.widget.SearchView
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var exhibitionService: ExhibitionService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: ExhibitionAdapter
    private var allExhibitions: List<ExhibitionWithPaintingResponse> = emptyList()
    private var lastSearchQuery: String? = null // Переменная для хранения последнего запроса

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        // Инициализация адаптера
        adapter = ExhibitionAdapter(emptyList()) { exhibition ->
            val bundle = Bundle().apply {
                putParcelable("exhibition", exhibition)
            }
            findNavController().navigate(R.id.action_homeFragment_to_exhibitionsFragment, bundle)
        }
        binding.recyclerView.adapter = adapter

        // Загружаем выставки
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

        // Настройка SearchView
        setupSearchView()

        // Восстановление текста поискового запроса
        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            binding.searchView.setQuery(query, false)
            filterExhibitions(query) // Фильтруем список при восстановлении текста
        }

        // Обработка нажатия на кнопку "Обновить" в плейсхолдере с ошибкой
        binding.retryButton.setOnClickListener {
            lastSearchQuery?.let { query ->
                filterExhibitions(query) // Повторно отправляем последний запрос
            }
        }

        // Обработка нажатия на кнопку "Обновить" в плейсхолдере для пустого результата
        binding.retrySearchButton.setOnClickListener {
            lastSearchQuery?.let { query ->
                filterExhibitions(query) // Повторно отправляем последний запрос
            }
        }

        // Загружаем данные пользователя для определения роли
        val token = sharedPreferences.getString("jwtToken", null)
        val userId = sharedPreferences.getInt("userId", -1)

        if (token != null && userId != -1) {
            viewModel.loadUser(token, userId)
        } else {
            Log.e("HomeFragment", "Token or User ID not found in SharedPreferences")
            Toast.makeText(requireContext(), "Token or User ID not found", Toast.LENGTH_SHORT).show()
        }

        // Подписка на изменения данных пользователя
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                // Управление видимостью кнопки создания выставки в зависимости от роли
                binding.bottomNavigationView.menu.findItem(R.id.navigation_create_exhibition).isVisible =
                    user.role == "ORGANIZER"
            } else {
                Log.e("HomeFragment", "User data is null")
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchView() {
        val searchView = binding.searchView

        // 1. Подсказка в пустом поле
        searchView.queryHint = "Поиск по названию выставки"

        // 2. Показ клавиатуры при нажатии на поле ввода
        searchView.isIconified = false
        searchView.requestFocus()

        // 3. Отображение кнопки "Очистить" при вводе текста
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.setSearchQuery(it) }
                filterExhibitions(newText)
                return true
            }
        })

        // 4. Очистка текста и скрытие клавиатуры при нажатии на кнопку "Очистить"
        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            viewModel.setSearchQuery("")
            filterExhibitions("")
            true
        }
    }

    private fun filterExhibitions(query: String?) {
        lastSearchQuery = query // Сохраняем последний запрос
        val filteredExhibitions = if (query.isNullOrEmpty()) {
            allExhibitions
        } else {
            allExhibitions.filter { exhibition ->
                exhibition.title.contains(query, ignoreCase = true)
            }
        }

        if (filteredExhibitions.isEmpty()) {
            // Показываем плейсхолдер с кнопкой "Обновить", если нет результатов
            showNoResultsPlaceholder()
        } else {
            // Скрываем плейсхолдер и показываем список
            hidePlaceholders()
            adapter.updateData(filteredExhibitions)
        }
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
                    if (exhibitions != null) {
                        allExhibitions = exhibitions
                        adapter.updateData(exhibitions)
                        hidePlaceholders() // Скрываем плейсхолдеры, если данные загружены
                    }
                } else {
                    // Показываем плейсхолдер с ошибкой
                    showErrorPlaceholder()
                    Log.e("HomeFragment", "Ошибка загрузки: ${response.message()}")
                    Toast.makeText(context, "Ошибка загрузки: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Показываем плейсхолдер с ошибкой
                showErrorPlaceholder()
                Log.e("HomeFragment", "Ошибка сети: ${e.message}", e)
                Toast.makeText(context, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNoResultsPlaceholder() {
        binding.noResultsPlaceholder.visibility = View.VISIBLE
        binding.errorPlaceholder.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    private fun showErrorPlaceholder() {
        binding.errorPlaceholder.visibility = View.VISIBLE
        binding.noResultsPlaceholder.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    private fun hidePlaceholders() {
        binding.noResultsPlaceholder.visibility = View.GONE
        binding.errorPlaceholder.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun getTokenFromSharedPreferences(): String {
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}