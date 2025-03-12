package com.example.exhibitionapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.exhibitionapp.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настройка кнопки для перехода к форме для художников
        binding.btnApplyExhibition.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_appForArtistsFragment)
        }

        // Настройка кнопки для перехода к форме для инвесторов
        binding.btnInvestProject.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_appForInvestorsFragment2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
