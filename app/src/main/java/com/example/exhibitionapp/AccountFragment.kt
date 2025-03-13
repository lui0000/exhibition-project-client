package com.example.exhibitionapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val token = sharedPreferences.getString("jwtToken", null)

        if (token != null) {
            val userId = sharedPreferences.getInt("userId", -1)
            if (userId != -1) {
                viewModel.loadUser(token, userId)
            }
        }


        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.userName.text = user.name
                binding.userEmail.text = user.email
                binding.userRole.text = "Роль: ${user.role}"
            }
        }

        // Настройка кнопок переходов
        binding.btnApplyExhibition.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_appForArtistsFragment)
        }

        binding.btnInvestProject.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_appForInvestorsFragment2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

