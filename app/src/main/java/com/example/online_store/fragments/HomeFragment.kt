package com.example.online_store.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.online_store.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si necesitas cargar ProductsFragment dentro de HomeFragment
        if (childFragmentManager.findFragmentById(R.id.home_fragment_container) == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.home_fragment_container, ProductsFragment())
                .commit()
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}