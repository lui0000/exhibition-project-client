package com.example.exhibitionapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exhibitionapp.databinding.FragmentAppForInvestorsBinding
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import com.example.exhibitionapp.services.ExhibitionService
import kotlinx.coroutines.launch
import retrofit2.HttpException

@RequiresApi(Build.VERSION_CODES.O)
class AppForInvestorsFragment : Fragment() {

    private var _binding: FragmentAppForInvestorsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exhibitionService: ExhibitionService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppForInvestorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        // Используем ваш RetrofitClient
        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)

        setupDropdownBehavior()
        loadExhibitionTitles()
    }

    private fun setupDropdownBehavior() {
        // чтобы при клике сразу выпадал список
        binding.exhibitionSpinner.apply {
            threshold = 0
            isFocusable = false
            isClickable = true
            setOnClickListener { showDropDown() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadExhibitionTitles() {
        val token = sharedPreferences.getString("jwtToken", null)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show()
            return
        }

        // Показываем ProgressBar
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = exhibitionService.getExhibitions("Bearer $token")
                if (response.isSuccessful) {
                    // Получаем тело ответа
                    val body: List<ExhibitionWithPaintingResponse> = response.body() ?: emptyList()
                    // Извлекаем только названия
                    val titles = body.map { it.title }

                    // Создаем адаптер и вешаем на AutoCompleteTextView
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        titles
                    )
                    binding.exhibitionSpinner.setAdapter(adapter)

                    // Обработка выбора
                    binding.exhibitionSpinner.setOnItemClickListener { parent, _, position, _ ->
                        val selectedTitle = parent.getItemAtPosition(position) as String
                        Toast.makeText(
                            requireContext(),
                            "Вы выбрали: $selectedTitle",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка ${response.code()}: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: HttpException) {
                Toast.makeText(
                    requireContext(),
                    "Сервер вернул ошибку ${e.code()}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
