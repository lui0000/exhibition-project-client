package com.example.exhibitionapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.exhibitionapp.dataclass.RegisterRequest
import com.example.exhibitionapp.services.AuthService
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class RegistrationFragment : Fragment() {

    private lateinit var authService: AuthService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authService = RetrofitClient.createService(AuthService::class.java)

        val nameInput = view.findViewById<TextInputEditText>(R.id.user_name)
        val emailInput = view.findViewById<TextInputEditText>(R.id.email)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.password)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)

        val organizerBtn = view.findViewById<RadioButton>(R.id.org_btn)
        val artistBtn = view.findViewById<RadioButton>(R.id.artist_btn)
        val investorBtn = view.findViewById<RadioButton>(R.id.inv_btn)

        view.findViewById<Button>(R.id.btn_login_now).setOnClickListener {
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }

        btnRegister.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            val role = when {
                organizerBtn.isChecked -> "ORGANIZER"
                artistBtn.isChecked -> "ARTIST"
                investorBtn.isChecked -> "INVESTOR"
                else -> {
                    Toast.makeText(requireContext(), "Select a role", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, password, role, progressBar, btnRegister)
        }
    }

    private fun registerUser(
        name: String, email: String, password: String, role: String,
        progressBar: ProgressBar, btnRegister: Button
    ) {
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        val request = RegisterRequest(name, email, password, role)

        lifecycleScope.launch {
            try {
                val response = authService.register(request)
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegistrationError", "Failed response: $errorBody")
                    Toast.makeText(requireContext(), "Registration failed: $errorBody", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("RegistrationException", "Exception during registration", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
            }
        }
    }
}
