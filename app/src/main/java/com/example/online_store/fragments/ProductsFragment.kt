package com.example.online_store.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.online_store.R
import com.google.android.material.button.MaterialButton

class ProductsFragment : Fragment() {

    private lateinit var btnTodos: MaterialButton
    private lateinit var btnFrutas: MaterialButton
    private lateinit var btnVerduras: MaterialButton
    private lateinit var btnBebidas: MaterialButton
    private lateinit var hsvCategoryButtons: HorizontalScrollView
    private var currentCategory = "Todos"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize category buttons and scroll view
        btnTodos = view.findViewById(R.id.btn_todos)
        btnFrutas = view.findViewById(R.id.btn_frutas)
        btnVerduras = view.findViewById(R.id.btn_verduras)
        btnBebidas = view.findViewById(R.id.btn_bebidas)
        hsvCategoryButtons = view.findViewById(R.id.hsv_category_buttons)

        // Setup button click listeners
        setupCategoryButtons()

        // Load default category (Todos)
        loadCategoryFragment(currentCategory)
    }

    private fun setupCategoryButtons() {
        btnTodos.setOnClickListener {
            updateButtonStyles(btnTodos)
            scrollToButton(btnTodos)
            loadCategoryFragment("Todos")
        }

        btnFrutas.setOnClickListener {
            updateButtonStyles(btnFrutas)
            scrollToButton(btnFrutas)
            loadCategoryFragment("Frutas")
        }

        btnVerduras.setOnClickListener {
            updateButtonStyles(btnVerduras)
            scrollToButton(btnVerduras)
            loadCategoryFragment("Verduras")
        }

        btnBebidas.setOnClickListener {
            updateButtonStyles(btnBebidas)
            scrollToButton(btnBebidas)
            loadCategoryFragment("Bebidas")
        }
    }

    private fun updateButtonStyles(selectedButton: MaterialButton) {
        // Reset all buttons to gray
        val grayColor = ContextCompat.getColor(requireContext(), R.color.gray)
        val yellowColor = ContextCompat.getColor(requireContext(), R.color.amarillo)

        btnTodos.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray)
        btnFrutas.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray)
        btnVerduras.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray)
        btnBebidas.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray)

        // Set selected button to yellow
        selectedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.amarillo)
    }

    private fun scrollToButton(selectedButton: MaterialButton) {
        // Smooth scroll to center the selected button
        hsvCategoryButtons.post {
            val scrollViewWidth = hsvCategoryButtons.width
            val buttonLeft = selectedButton.left
            val buttonWidth = selectedButton.width
            val scrollX = buttonLeft - (scrollViewWidth / 2) + (buttonWidth / 2)

            hsvCategoryButtons.smoothScrollTo(scrollX, 0)
        }
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