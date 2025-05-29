package com.example.online_store.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.FavoriteProductAdapter
import com.example.online_store.model.Product
import com.example.online_store.utils.CartManager
import com.example.online_store.utils.FavoritesManager

class FavoritesFragment : Fragment() {

    private lateinit var rvFavorites: RecyclerView
    private lateinit var tvEmptyFavorites: TextView
    private lateinit var favoriteAdapter: FavoriteProductAdapter
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var cartManager: CartManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar managers
        favoritesManager = FavoritesManager(requireContext())
        cartManager = CartManager(requireContext())

        // Inicializar vistas
        rvFavorites = view.findViewById(R.id.rv_favorites)
        tvEmptyFavorites = view.findViewById(R.id.tv_empty_favorites)

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar favoritos
        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        // Recargar favoritos al volver al fragmento
        loadFavorites()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteProductAdapter(
            products = emptyList(),
            onProductClickListener = { product ->
                // Abrir detalles del producto
                val detailFragment = ProductDetailFragment.newInstance(product.id)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onFavoriteClickListener = { product ->
                // Eliminar de favoritos
                if (favoritesManager.removeFromFavorites(product)) {
                    // Recargar la lista
                    loadFavorites()
                }
            },
            cartManager = cartManager
        )

        rvFavorites.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = favoriteAdapter
        }
    }

    private fun loadFavorites() {
        val favoriteProducts = favoritesManager.getFavoriteProducts()

        if (favoriteProducts.isEmpty()) {
            tvEmptyFavorites.visibility = View.VISIBLE
            rvFavorites.visibility = View.GONE
        } else {
            tvEmptyFavorites.visibility = View.GONE
            rvFavorites.visibility = View.VISIBLE
            favoriteAdapter.updateProducts(favoriteProducts)
        }
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}