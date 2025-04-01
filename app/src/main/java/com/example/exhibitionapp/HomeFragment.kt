package com.example.exhibitionapp

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.exhibitionapp.databinding.FragmentHomeBinding
import com.example.exhibitionapp.services.ExhibitionService
import com.example.exhibitionapp.viewmodel.HomeViewModel
import androidx.appcompat.widget.SearchView
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var exhibitionService: ExhibitionService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchHistoryPreferences: SharedPreferences
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: ExhibitionAdapter
    private lateinit var suggestionsAdapter: SimpleCursorAdapter
    private var allExhibitions: List<ExhibitionWithPaintingResponse> = emptyList()
    private var lastSearchQuery: String? = null

    private val HISTORY_KEY = "search_history"
    private val MAX_HISTORY_ITEMS = 10



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
        searchHistoryPreferences = requireContext().getSharedPreferences("search_history", Context.MODE_PRIVATE)
        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        // Инициализация адаптера для выставок
        adapter = ExhibitionAdapter(emptyList()) { exhibition ->
            addToSearchHistory(exhibition.title)
            val bundle = Bundle().apply {
                putParcelable("exhibition", exhibition)
            }
            findNavController().navigate(R.id.action_homeFragment_to_exhibitionsFragment, bundle)
        }
        binding.recyclerView.adapter = adapter

        // Инициализация адаптера для подсказок поиска
        setupSearchSuggestionsAdapter()

        // Загружаем выставки
        loadExhibitions()

        // Настройка BottomNavigationView
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
            filterExhibitions(query)
        }

        // Обработка кнопок
        binding.retryButton.setOnClickListener {
            lastSearchQuery?.let { query -> filterExhibitions(query) }
        }

        binding.retrySearchButton.setOnClickListener {
            lastSearchQuery?.let { query -> filterExhibitions(query) }
        }

        binding.clearHistoryButton.setOnClickListener {
            clearSearchHistory()
        }

        // Загрузка данных пользователя
        val token = sharedPreferences.getString("jwtToken", null)
        val userId = sharedPreferences.getInt("userId", -1)

        if (token != null && userId != -1) {
            viewModel.loadUser(token, userId)
        } else {
            Log.e("HomeFragment", "Token or User ID not found")
            Toast.makeText(requireContext(), "Token or User ID not found", Toast.LENGTH_SHORT).show()
        }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.bottomNavigationView.menu.findItem(R.id.navigation_create_exhibition).isVisible =
                    user.role == "ORGANIZER"
            } else {
                Log.e("HomeFragment", "User data is null")
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearchSuggestionsAdapter() {
        val from = arrayOf("suggestion")
        val to = intArrayOf(android.R.id.text1)

        suggestionsAdapter = SimpleCursorAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            null,
            from,
            to,
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )

        binding.searchView.suggestionsAdapter = suggestionsAdapter
    }

    private fun createSuggestionsCursor(suggestions: List<String>): Cursor {
        val matrixCursor = MatrixCursor(arrayOf("_id", "suggestion"))
        suggestions.forEachIndexed { index, suggestion ->
            matrixCursor.addRow(arrayOf(index, suggestion))
        }
        return matrixCursor
    }

    private fun showSearchHistory() {
        val history = getSearchHistory()
        if (history.isNotEmpty()) {
            binding.clearHistoryButton.visibility = View.VISIBLE
            suggestionsAdapter.changeCursor(createSuggestionsCursor(history))
        } else {
            binding.clearHistoryButton.visibility = View.GONE
            suggestionsAdapter.changeCursor(null)
        }
    }

    private fun addToSearchHistory(query: String) {
        if (query.isBlank()) return

        val history = getSearchHistory().toMutableList()
        history.removeAll { it.equals(query, ignoreCase = true) }
        history.add(0, query)

        if (history.size > MAX_HISTORY_ITEMS) {
            history.subList(MAX_HISTORY_ITEMS, history.size).clear()
        }

        searchHistoryPreferences.edit()
            .putStringSet(HISTORY_KEY, history.toSet())
            .apply()

        showSearchHistory()
    }

    private fun getSearchHistory(): List<String> {
        return searchHistoryPreferences.getStringSet(HISTORY_KEY, emptySet())?.toList() ?: emptyList()
    }

    private fun clearSearchHistory() {
        searchHistoryPreferences.edit()
            .remove(HISTORY_KEY)
            .apply()
        suggestionsAdapter.changeCursor(null)
        binding.clearHistoryButton.visibility = View.GONE
    }

    private fun setupSearchView() {
        val searchView = binding.searchView

        searchView.queryHint = "Поиск по названию выставки"
        searchView.isIconified = false
        searchView.requestFocus()

        searchView.setOnSearchClickListener {
            showSearchHistory()
        }

        searchView.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean = false

            override fun onSuggestionClick(position: Int): Boolean {
                suggestionsAdapter.cursor?.let { cursor ->
                    if (cursor.moveToPosition(position)) {
                        val suggestion = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"))
                        searchView.setQuery(suggestion, true)
                    }
                }
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    addToSearchHistory(it)
                    viewModel.setSearchQuery(it)
                    filterExhibitions(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.setSearchQuery(it) }
                filterExhibitions(newText)
                return true
            }
        })

        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            searchView.clearFocus()
            viewModel.setSearchQuery("")
            filterExhibitions("")
            true
        }
    }

    private fun filterExhibitions(query: String?) {
        lastSearchQuery = query
        val filteredExhibitions = if (query.isNullOrEmpty()) {
            allExhibitions
        } else {
            allExhibitions.filter { it.title.contains(query, ignoreCase = true) }
        }

        if (filteredExhibitions.isEmpty()) {
            showNoResultsPlaceholder()
        } else {
            hidePlaceholders()
            adapter.updateData(filteredExhibitions)
        }
    }

    private fun loadExhibitions() {
        lifecycleScope.launch {
            try {
                val token = getTokenFromSharedPreferences()
                if (token.isEmpty()) {
                    showError("Токен отсутствует!")
                    return@launch
                }

                val response = exhibitionService.getExhibitions("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let {
                        allExhibitions = it
                        adapter.updateData(it)
                        hidePlaceholders()
                    }
                } else {
                    showError("Ошибка загрузки: ${response.message()}")
                }
            } catch (e: Exception) {
                showError("Ошибка сети: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Log.e("HomeFragment", message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        showErrorPlaceholder()
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