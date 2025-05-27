package com.example.exhibitionapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.exhibitionapp.databinding.FragmentExhibitionsBinding
import com.example.exhibitionapp.dataclass.ExhibitionWithAdditionalInfoDto
import com.example.exhibitionapp.services.ExhibitionService
import kotlinx.coroutines.launch

class ExhibitionsFragment : Fragment() {

    private var _binding: FragmentExhibitionsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var exhibitionService: ExhibitionService
    private var exhibitionTitle: String? = null

    companion object {
        private const val TAG = "ExhibitionDetails"
        private const val ARG_EXHIBITION_TITLE = "exhibition_title"

        fun newInstance(exhibitionTitle: String) = ExhibitionsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_EXHIBITION_TITLE, exhibitionTitle)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExhibitionsBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Добавляем обработчик для кнопки "Назад"
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_exhibitionsFragment_to_homeFragment2)
        }

        Log.d(TAG, "Arguments received: $arguments")
        exhibitionTitle = arguments?.getString(ARG_EXHIBITION_TITLE)
        Log.d(TAG, "Exhibition title: '$exhibitionTitle'")

        if (exhibitionTitle.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Ошибка: название выставки не найдено", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Exhibition title is null or blank!")
            return
        }

        sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        exhibitionService = RetrofitClient.createService(ExhibitionService::class.java)

        loadExhibitionDetails()
    }

    private fun loadExhibitionDetails() {
        val token = sharedPreferences.getString("jwtToken", null)
        Log.d(TAG, "Token: ${token?.take(20)}...") // показываем только начало токена

        if (token.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Making API call for exhibition: '$exhibitionTitle'")
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = exhibitionService.getExhibitionDetails("Bearer $token", exhibitionTitle!!)
                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d(TAG, "Response data: $data")
                    if (data != null) {
                        setupUI(data)
                    } else {
                        showError("Данные не получены")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error response: $errorBody")
                    showError("Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading exhibition details", e)
                showError("Ошибка загрузки: ${e.localizedMessage}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    private fun setupUI(data: ExhibitionWithAdditionalInfoDto) {
        // Устанавливаем заголовок и описание
        binding.exhibitionTitle.text = data.exhibitionDto.title
        binding.exhibitionDescription.text = data.exhibitionDto.description

        // Загружаем главное изображение
        data.photoData?.let { imageUrl ->
            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.exhibitionImage)
        }

        // Настраиваем список художников
        if (data.artists.isNotEmpty()) {
            binding.artistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.artistsRecyclerView.adapter = StringListAdapter(data.artists)
        }

        // Настраиваем карусель картин
        if (data.paintingImages.isNotEmpty()) {
            binding.paintingsViewPager.adapter = PaintingsAdapter(data.paintingImages)
        }

        // Настраиваем список инвесторов
        if (data.investors.isNotEmpty()) {
            binding.investorsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.investorsRecyclerView.adapter = StringListAdapter(data.investors)
            binding.noInvestorsText.visibility = View.GONE
        } else {
            binding.investorsRecyclerView.visibility = View.GONE
            binding.noInvestorsText.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        Log.e(TAG, message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Адаптер для карусели картин
    inner class PaintingsAdapter(private val paintingUrls: List<String>) :
        RecyclerView.Adapter<PaintingsAdapter.PaintingViewHolder>() {

        inner class PaintingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.painting_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaintingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_painting, parent, false)
            return PaintingViewHolder(view)
        }

        override fun onBindViewHolder(holder: PaintingViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                .load(paintingUrls[position])
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.imageView)
        }

        override fun getItemCount() = paintingUrls.size
    }

    // Адаптер для списков текста
    inner class StringListAdapter(private val items: List<String>) :
        RecyclerView.Adapter<StringListAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.item_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_text, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position]
        }

        override fun getItemCount() = items.size
    }
}
