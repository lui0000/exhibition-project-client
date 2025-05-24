package com.example.exhibitionapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.exhibitionapp.databinding.FragmentAppForInvestorsBinding
import com.example.exhibitionapp.dataclass.ExhibitionResponse
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import com.example.exhibitionapp.dataclass.InvestmentExhibitionRequest
import com.example.exhibitionapp.dataclass.InvestmentRequest
import com.example.exhibitionapp.services.ExhibitionService
import com.example.exhibitionapp.services.InvestmentService
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URLEncoder
import android.util.Base64
import retrofit2.Response
import retrofit2.HttpException


@RequiresApi(Build.VERSION_CODES.O)
class AppForInvestorsFragment : Fragment() {

    private var _binding: FragmentAppForInvestorsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exhibitionService: ExhibitionService
    private lateinit var investmentService: InvestmentService

    private var exhibitionList: List<ExhibitionWithPaintingResponse> = emptyList()
    private var selectedExhibitionFull: ExhibitionResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppForInvestorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)
        investmentService = RetrofitClient.createService(InvestmentService::class.java)

        setupDropdownBehavior()
        loadExhibitionTitles()

        binding.btnInvest.setOnClickListener {
            val amount = binding.investmentAmount.text.toString().trim()
            if (selectedExhibitionFull == null) {
                Toast.makeText(requireContext(), "Выберите выставку", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (amount.isEmpty()) {
                Toast.makeText(requireContext(), "Введите сумму инвестиции", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendInvestment(amount)
        }
    }

    private fun setupDropdownBehavior() {
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

        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response: Response<List<ExhibitionWithPaintingResponse>> =
                    exhibitionService.getExhibitions("Bearer $token")

                if (response.isSuccessful) {
                    exhibitionList = response.body().orEmpty()
                    val titles = exhibitionList.map { it.title }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        titles
                    )
                    binding.exhibitionSpinner.setAdapter(adapter)

                    binding.exhibitionSpinner.setOnItemClickListener { parent, _, pos, _ ->
                        val title = parent.getItemAtPosition(pos) as String
                        Toast.makeText(requireContext(), "Вы выбрали: $title", Toast.LENGTH_SHORT).show()
                        loadFullExhibitionByTitle(title, token)
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

    private fun loadFullExhibitionByTitle(title: String, rawToken: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val encoded = URLEncoder.encode(title, "UTF-8")
                val resp: Response<ExhibitionResponse> =
                    exhibitionService.getExhibitionByTitle("Bearer $rawToken", encoded)

                if (resp.isSuccessful) {
                    selectedExhibitionFull = resp.body()
                    Log.d("AppForInvestors", "Loaded full exhibition: $selectedExhibitionFull")
                } else {
                    Toast.makeText(requireContext(), "Не удалось получить выставку", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendInvestment(amount: String) {
        val token = sharedPreferences.getString("jwtToken", null)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show()
            return
        }

        // ← HERE: read userId exactly as your other fragment does
        val userId = sharedPreferences.getInt("userId", -1)
        if (userId == -1) {
            Toast.makeText(requireContext(), "ID пользователя не найден", Toast.LENGTH_SHORT).show()
            Log.e("AppForInvestors", "userId missing in SharedPrefs")
            return
        }

        val ex = selectedExhibitionFull!!
        val request = InvestmentRequest(
            exhibition = InvestmentExhibitionRequest(
                id = ex.id,
                title = ex.title,
                description = ex.description,
                startDate = ex.startDate,
                endDate = ex.endDate
            ),
            amount = amount,
            investorId = userId
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp = investmentService.createInvestment("Bearer $token", request)
                if (resp.isSuccessful) {
                    Toast.makeText(requireContext(), "Инвестиция отправлена", Toast.LENGTH_SHORT).show()
                } else {
                    val err = resp.errorBody()?.string().orEmpty()
                    Toast.makeText(requireContext(), "Ошибка при отправке: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
