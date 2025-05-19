package com.example.online_store.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.online_store.R

class ProductsFragment : Fragment() {

    private lateinit var btnFrutas: Button
    private lateinit var btnVerduras: Button
    private lateinit var btnBebidas: Button
    private var currentCategory = "Frutas"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize category buttons
        btnFrutas = view.findViewById(R.id.btn_frutas)
        btnVerduras = view.findViewById(R.id.btn_verduras)
        btnBebidas = view.findViewById(R.id.btn_bebidas)

        // Setup button click listeners
        setupCategoryButtons()

        // Load default category (Frutas)
        loadCategoryFragment(currentCategory)
    }

    private fun setupCategoryButtons() {
        btnFrutas.setOnClickListener {
            updateButtonStyles(btnFrutas)
            loadCategoryFragment("Frutas")
        }

        btnVerduras.setOnClickListener {
            updateButtonStyles(btnVerduras)
            loadCategoryFragment("Verduras")
        }

        btnBebidas.setOnClickListener {
            updateButtonStyles(btnBebidas)
            loadCategoryFragment("Bebidas")
        }
    }

    private fun updateButtonStyles(selectedButton: Button) {
        // Reset all buttons to gray
        btnFrutas.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_category_gray)
        btnVerduras.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_category_gray)
        btnBebidas.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_category_gray)

        // Set selected button to yellow
        selectedButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.btn_category_yellow)
    }

    private fun loadCategoryFragment(category: String) {
        currentCategory = category

        // Create new fragment for the selected category
        val fragment = ProductCategoryFragment.newInstance(category)

        // Replace current category fragment with the new one
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_category_container, fragment)
            .commit()
    }
}