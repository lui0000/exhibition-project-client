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
import com.example.exhibitionapp.databinding.FragmentAppForArtistsBinding
import com.example.exhibitionapp.dataclass.ArtistRequest
import com.example.exhibitionapp.dataclass.ExhibitionInPaintingRequest
import com.example.exhibitionapp.dataclass.PaintingRequest
import com.example.exhibitionapp.dataclass.ExhibitionWithPaintingResponse
import com.example.exhibitionapp.dataclass.ExhibitionResponse
import com.example.exhibitionapp.services.ExhibitionService
import com.example.exhibitionapp.services.PaintingService
import com.example.exhibitionapp.services.UserService
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class AppForArtistsFragment : Fragment() {

    private var _binding: FragmentAppForArtistsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exhibitionService: ExhibitionService
    private lateinit var paintingService: PaintingService
    private lateinit var userService: UserService

    private var exhibitionList: List<ExhibitionWithPaintingResponse> = emptyList()
    private var selectedExhibition: ExhibitionResponse? = null

    companion object {
        private const val TAG = "AppForArtists"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppForArtistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ИСПРАВЛЕНО: используем правильный action для AppForArtistsFragment
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_appForArtistsFragment_to_accountFragment)
        }

        Log.d(TAG, "onViewCreated()")
        sharedPreferences = requireContext()
            .getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)
        paintingService   = RetrofitClient.createService(PaintingService::class.java)
        userService       = RetrofitClient.createService(UserService::class.java)

        setupDropdownBehavior()
        loadExhibitionTitles()

        binding.btnSubmitApplication.setOnClickListener {
            Log.d(TAG, "Submit button clicked")
            submitPainting()
        }
    }


    private fun setupDropdownBehavior() {
        Log.d(TAG, "setupDropdownBehavior()")
        binding.exhibitionSpinner.apply {
            threshold = 0
            isFocusable = false
            isClickable = true
            setOnClickListener { showDropDown() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadExhibitionTitles() {
        Log.d(TAG, "loadExhibitionTitles()")
        val token = sharedPreferences.getString("jwtToken", null)
        Log.d(TAG, "Token from prefs: $token")
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "JWT token is null or blank")
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp: Response<List<ExhibitionWithPaintingResponse>> =
                    exhibitionService.getExhibitions("Bearer $token")
                Log.d(TAG, "getExhibitions() returned code=${resp.code()}")

                if (resp.isSuccessful) {
                    exhibitionList = resp.body().orEmpty()
                    Log.d(TAG, "Loaded ${exhibitionList.size} exhibitions")
                    val titles = exhibitionList.map { it.title }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        titles
                    )
                    binding.exhibitionSpinner.setAdapter(adapter)

                    binding.exhibitionSpinner.setOnItemClickListener { parent, _, pos, _ ->
                        val title = parent.getItemAtPosition(pos) as String
                        Log.d(TAG, "Spinner item clicked: position=$pos, title=$title")
                        Toast.makeText(requireContext(), "Вы выбрали: $title", Toast.LENGTH_SHORT).show()
                        loadFullExhibitionByTitle(title, token)
                    }

                } else {
                    Log.e(TAG, "getExhibitions() failed: code=${resp.code()}, message=${resp.message()}")
                    Toast.makeText(
                        requireContext(),
                        "Ошибка ${resp.code()}: ${resp.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException loading exhibitions", e)
                Toast.makeText(
                    requireContext(),
                    "Сервер вернул ошибку ${e.code()}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading exhibitions", e)
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "loadExhibitionTitles() done, hiding progressBar")
            }
        }
    }

    private fun loadFullExhibitionByTitle(title: String, rawToken: String) {
        Log.d(TAG, "loadFullExhibitionByTitle(title=$title)")
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resp: Response<ExhibitionResponse> =
                    exhibitionService.getExhibitionByTitle("Bearer $rawToken", title)
                Log.d(TAG, "getExhibitionByTitle() returned code=${resp.code()}")

                if (resp.isSuccessful) {
                    selectedExhibition = resp.body()
                    Log.d(TAG, "Loaded full exhibition: $selectedExhibition")
                } else {
                    val err = resp.errorBody()?.string().orEmpty()
                    Log.e(TAG, "getExhibitionByTitle() failed: code=${resp.code()}, body=$err")
                    Toast.makeText(
                        requireContext(),
                        "Не удалось получить выставку: $err",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadFullExhibitionByTitle()", e)
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "loadFullExhibitionByTitle() done, hiding progressBar")
            }
        }
    }

    private fun submitPainting() {
        Log.d(TAG, "submitPainting()")
        val token = sharedPreferences.getString("jwtToken", null)
        Log.d(TAG, "Token: $token")
        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "JWT token missing")
            return
        }

        val userId = sharedPreferences.getInt("userId", -1)
        Log.d(TAG, "User ID: $userId")
        if (userId == -1) {
            Toast.makeText(requireContext(), "ID пользователя не найден", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "User ID missing in SharedPreferences")
            return
        }

        val ex = selectedExhibition
        Log.d(TAG, "SelectedExhibition: $ex")
        if (ex == null) {
            Toast.makeText(requireContext(), "Выберите выставку", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "No exhibition selected")
            return
        }

        val style       = binding.artistStyle.text.toString().trim().ifEmpty { null }
        val title       = binding.artistPaintingTitle.text.toString().trim()
        val description = binding.artistPaintingDescription.text.toString().trim().ifEmpty { null }
        val photoUrl    = binding.artistPainting.text.toString().trim()

        Log.d(TAG, "Form data: style=$style, title=$title, description=$description, photoUrl=$photoUrl")
        if (title.isEmpty() || photoUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название и URL", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Validation failed: title or photoUrl empty")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userData = userService.getUserInfo("Bearer $token", userId)
                Log.d(TAG, "getUserInfo() completed successfully")

                if (userData == null) {
                    Log.e(TAG, "User data is null")
                    Toast.makeText(requireContext(), "Данные пользователя недоступны", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = PaintingRequest(
                    title       = title,
                    style       = style,
                    description = description,
                    photoData   = photoUrl,
                    artist      = ArtistRequest(
                        name = userData.name,
                        email = userData.email,
                        role = "ARTIST"
                    ),
                    exhibition  = ExhibitionInPaintingRequest(
                        id = ex.id,
                        title = ex.title,
                        description = ex.description,
                        startDate = ex.startDate,
                        endDate = ex.endDate
                    )
                )
                Log.d(TAG, "PaintingRequest: $request")

                val resp = paintingService.createPainting("Bearer $token", request)
                Log.d(TAG, "createPainting() returned code=${resp.code()}")
                if (resp.isSuccessful) {
                    Toast.makeText(requireContext(), "Заявка отправлена", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Painting submission successful")
                } else {
                    val err = resp.errorBody()?.string().orEmpty()
                    Log.e(TAG, "createPainting() failed: $err")
                    Toast.makeText(
                        requireContext(),
                        "Ошибка при отправке: $err",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in submitPainting()", e)
                Toast.makeText(
                    requireContext(),
                    "Ошибка: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                Log.d(TAG, "submitPainting() done, hiding progressBar")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
