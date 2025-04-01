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
import androidx.navigation.fragment.findNavController
import com.example.exhibitionapp.databinding.FragmentAccountBinding
import com.example.exhibitionapp.viewmodel.AccountViewModel
import androidx.appcompat.app.AppCompatDelegate


class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AccountViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwtToken", null)

        if (token != null) {
            val userId = sharedPreferences.getInt("userId", -1)
            if (userId != -1) {
                Log.d("AccountFragment", "Loading user with token: $token and userId: $userId")
                viewModel.loadUser(token, userId)
            } else {
                Log.e("AccountFragment", "User ID not found in SharedPreferences")
                Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("AccountFragment", "Token not found in SharedPreferences")
            Toast.makeText(requireContext(), "Token not found", Toast.LENGTH_SHORT).show()
        }

        // Подписка на изменения данных пользователя
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.userName.text = user.name
                binding.userEmail.text = user.email
                binding.userRole.text = "Роль: ${user.role}"

                // Управление видимостью кнопок в зависимости от роли
                when (user.role) {
                    "ARTIST" -> {
                        binding.btnApplyExhibition.visibility = View.VISIBLE
                        binding.btnInvestProject.visibility = View.GONE
                    }
                    "INVESTOR" -> {
                        binding.btnApplyExhibition.visibility = View.GONE
                        binding.btnInvestProject.visibility = View.VISIBLE
                    }
                    else -> {
                        // Для организатора и других ролей скрываем обе кнопки
                        binding.btnApplyExhibition.visibility = View.GONE
                        binding.btnInvestProject.visibility = View.GONE
                    }
                }
            } else {
                Log.e("AccountFragment", "User data is null")
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnApplyExhibition.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_accountFragment_to_appForArtistsFragment)
            } catch (e: Exception) {
                Log.e("AccountFragment", "Navigation error: ${e.message}")
                Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnInvestProject.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_accountFragment_to_appForInvestorsFragment2)
            } catch (e: Exception) {
                Log.e("AccountFragment", "Navigation error: ${e.message}")
                Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show()
            }
        }

        // Обработчик для кнопки выхода
        binding.logoutButton.setOnClickListener {
            logout()
        }


        // Настройка переключателя темной темы
        val themeSwitch = binding.themeSwitch
        val settingsPrefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val isDarkMode = settingsPrefs.getBoolean("dark_mode", false)

        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)

            // Сохраняем состояние темы
            settingsPrefs.edit().putBoolean("dark_mode", isChecked).apply()
        }
    }

    private fun logout() {
        // Очищаем SharedPreferences
        sharedPreferences.edit().apply {
            remove("jwtToken")
            remove("userId")
            apply()
        }

        // Перенаправляем на экран логина
        try {
            findNavController().navigate(R.id.action_accountFragment_to_loginFragment)
        } catch (e: Exception) {
            Log.e("AccountFragment", "Navigation error: ${e.message}")
            Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show()
        }

        // Уведомляем пользователя
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}