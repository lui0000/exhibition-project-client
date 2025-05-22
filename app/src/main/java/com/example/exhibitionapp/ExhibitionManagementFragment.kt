package com.example.exhibitionapp

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exhibitionapp.databinding.FragmentExhibitionManagementBinding
import com.example.exhibitionapp.dataclass.ExhibitionRequest
import com.example.exhibitionapp.dataclass.OrganizerRequest
import com.example.exhibitionapp.services.ExhibitionService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.*

class ExhibitionManagementFragment : Fragment() {

    private var _binding: FragmentExhibitionManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exhibitionService: ExhibitionService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExhibitionManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)

        binding.startDateInput.setOnClickListener {
            showDatePicker { dateString ->
                binding.startDateInput.setText(dateString)
            }
        }
        binding.endDateInput.setOnClickListener {
            showDatePicker { dateString ->
                binding.endDateInput.setText(dateString)
            }
        }

        binding.btnCreateExhibition.setOnClickListener {
            saveExhibition()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // Формат ISO-8601, который корректно обрабатывается LocalDate.parse()
            val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(formattedDate)
        }, year, month, day)

        datePickerDialog.show()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveExhibition() {
        val token = sharedPreferences.getString("jwtToken", null)
        val organizerId = sharedPreferences.getInt("userId", -1)

        if (token.isNullOrEmpty() || organizerId == -1) {
            Toast.makeText(requireContext(), "Токен или ID организатора не найдены", Toast.LENGTH_SHORT).show()
            Log.e("ExhibitionFragment", "Token or organizerId not found. Token=$token, organizerId=$organizerId")
            return
        }

        val title = binding.exhibitionName.text.toString().trim()
        val description = binding.exhibitionDescription.text.toString().trim()
        val startDateStr = binding.startDateInput.text.toString().trim()
        val endDateStr = binding.endDateInput.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || startDateStr.isEmpty() || endDateStr.isEmpty()) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            Log.e("ExhibitionFragment", "Validation failed: some fields are empty")
            return
        }

        try {
            val startDate = LocalDate.parse(startDateStr)
            val endDate = LocalDate.parse(endDateStr)

            val request = ExhibitionRequest(
                title = title,
                description = description,
                startDateStr = startDate.toString(),
                endDateStr = endDate.toString(),
                organizer = OrganizerRequest(userId = organizerId)
            )


            val gson = Gson()
            val requestJson = gson.toJson(request)
            Log.d("ExhibitionFragment", "JSON to send: $requestJson")

            Log.d("ExhibitionFragment", "Sending request: $request")
            Log.d("ExhibitionFragment", "Using token: Bearer $token")

            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val response = exhibitionService.createExhibition("Bearer $token", request)

                    Log.d("ExhibitionFragment", "Response code: ${response.code()}")
                    Log.d("ExhibitionFragment", "Response message: ${response.message()}")
                    Log.d("ExhibitionFragment", "Response headers: ${response.headers()}")

                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ExhibitionFragment", "Error body: $errorBody")
                    }

                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Выставка успешно создана", Toast.LENGTH_SHORT).show()
                            clearFields()
                        } else {
                            Toast.makeText(requireContext(), "Ошибка: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Log.e("ExhibitionFragment", "Exception: ${e.message}", e)
                        Toast.makeText(requireContext(), "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: DateTimeParseException) {
            Log.e("ExhibitionFragment", "Date parsing error: ${e.message}")
            Toast.makeText(requireContext(), "Неверный формат даты. Используйте формат YYYY-MM-DD", Toast.LENGTH_SHORT).show()
        }
    }


    private fun clearFields() {
        binding.exhibitionName.text?.clear()
        binding.exhibitionDescription.text?.clear()
        binding.startDateInput.text?.clear()
        binding.endDateInput.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

