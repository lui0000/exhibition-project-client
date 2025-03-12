package com.example.exhibitionapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.exhibitionapp.dataclass.LoginRequest
import com.example.exhibitionapp.services.AuthService
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<TextInputEditText>(R.id.email)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val registerButton = view.findViewById<Button>(R.id.btn_register_now)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            loginUser(email, password, progressBar)
        }

        registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }
    }

    private fun loginUser(email: String, password: String, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        val request = LoginRequest(email, password)
        val authService = RetrofitClient.createService(AuthService::class.java)

        lifecycleScope.launch {
            try {
                Log.d("LoginUser", "Attempting to login with email: $email")
                val response = authService.login(request)
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val token = response.body()?.jwtToken
                    token?.let {
                        saveToken(it)
                        Log.d("LoginUser", "Login successful! Token saved.")
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } else {
                    Log.e("LoginUser", "Invalid credentials. Response code: ${response.code()}")
                    Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e("LoginUser", "Error during login: ${e.message}", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveToken(token: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("jwt_token", token).apply()
        Log.d("SaveToken", "Token saved to SharedPreferences")
    }
}