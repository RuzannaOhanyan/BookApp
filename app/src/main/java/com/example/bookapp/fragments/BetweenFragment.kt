package com.example.bookapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentBetweenBinding

class BetweenFragment : Fragment() {
    private lateinit var binding: FragmentBetweenBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentBetweenBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            view.findNavController().navigate(R.id.action_betweenFragment_to_loginFragment)
        }
    }
}