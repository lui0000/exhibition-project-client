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
        val token = sharedPreferences.getString("jwt_token", null)

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
