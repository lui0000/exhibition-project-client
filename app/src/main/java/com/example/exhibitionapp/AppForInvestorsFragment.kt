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
import androidx.navigation.fragment.findNavController
import com.example.exhibitionapp.databinding.FragmentAppForInvestorsBinding
import com.example.exhibitionapp.dataclass.ExhibitionResponse
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import com.example.exhibitionapp.dataclass.InvestmentExhibitionRequest
import com.example.exhibitionapp.dataclass.InvestmentRequest
import com.example.exhibitionapp.services.ExhibitionService
import com.example.exhibitionapp.services.InvestmentService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class AppForInvestorsFragment : Fragment() {

    private var _binding: FragmentAppForInvestorsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exhibitionService: ExhibitionService
    private lateinit var investmentService: InvestmentService

    // full list so we can pull out id/title/etc.
    private var exhibitionList: List<ExhibitionWithPaintingResponse> = emptyList()
    private var selectedExhibitionFull: ExhibitionResponse? = null

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

        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)
        investmentService = RetrofitClient.createService(InvestmentService::class.java)


        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_appForInvestorsFragment2_to_accountFragment)
        }

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
        // make AutoCompleteTextView drop down on click
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
                        // grab the raw title and id
                        val selected = exhibitionList[pos]
                        Toast.makeText(
                            requireContext(),
                            "Вы выбрали: ${selected.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // load full by ID or by-title; here we keep by-title but pass raw
                        loadFullExhibitionByTitle(selected.title, token)
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
        // **NO** URLEncoder.encode(…) here—pass `title` raw
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp: Response<ExhibitionResponse> =
                    exhibitionService.getExhibitionByTitle("Bearer $rawToken", title)

                if (resp.isSuccessful) {
                    selectedExhibitionFull = resp.body()
                    Log.d("AppForInvestors", "Loaded full exhibition: $selectedExhibitionFull")
                } else {
                    val err = resp.errorBody()?.string().orEmpty()
                    Toast.makeText(
                        requireContext(),
                        "Не удалось получить выставку: $err",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("AppForInvestors", "by-title failed: $err")
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("AppForInvestors", "Exception loading by-title", e)
            }
        }
    }

    private fun sendInvestment(amount: String) {
        val token = sharedPreferences.getString("jwtToken", null)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show()
            return
        }

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
                    Toast.makeText(
                        requireContext(),
                        "Ошибка при отправке: $err",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
