package com.example.exhibitionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_login).setOnClickListener {
            // Навигация к HomeFragment
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        view.findViewById<Button>(R.id.btn_register_now).setOnClickListener {
            // Навигация к RegistrationFragment
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }
    }
}