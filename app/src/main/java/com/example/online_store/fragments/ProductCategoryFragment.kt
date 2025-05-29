package com.example.online_store.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.ProductListAdapter
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product
import com.example.online_store.utils.CartManager
import com.example.online_store.utils.FavoritesManager

class ProductCategoryFragment : Fragment() {

    private lateinit var rvCategoryProducts: RecyclerView
    private lateinit var tvCategoryTitle: TextView
    private lateinit var tvEmptyCategory: TextView
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var productDao: ProductDao
    private lateinit var cartManager: CartManager
    private lateinit var favoritesManager: FavoritesManager
    private var category: String = "Frutas" // Default category

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): ProductCategoryFragment {
            val fragment = ProductCategoryFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString(ARG_CATEGORY, "Frutas")
        }

        // Initialize DAOs and managers
        productDao = ProductDao(requireContext())
        cartManager = CartManager(requireContext())
        favoritesManager = FavoritesManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        rvCategoryProducts = view.findViewById(R.id.rv_category_products)
        tvCategoryTitle = view.findViewById(R.id.tv_category_title)
        tvEmptyCategory = view.findViewById(R.id.tv_empty_category)

        // Set category title
        tvCategoryTitle.text = category

        // Setup RecyclerView
        setupRecyclerView()

        // Load products
        loadProducts()
    }

    private fun setupRecyclerView() {
        productListAdapter = ProductListAdapter(
            products = emptyList(),
            onProductClickListener = { product ->
                // Abrir el fragmento de detalles del producto
                val detailFragment = ProductDetailFragment.newInstance(product.id)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            cartManager = cartManager,
            favoritesManager = favoritesManager
        )

        rvCategoryProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productListAdapter
        }
    }

    private fun loadProducts() {
        val products = productDao.getProductsByCategory(category)

        if (products.isEmpty()) {
            tvEmptyCategory.visibility = View.VISIBLE
            rvCategoryProducts.visibility = View.GONE
        } else {
            tvEmptyCategory.visibility = View.GONE
            rvCategoryProducts.visibility = View.VISIBLE
            productListAdapter.updateProducts(products)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh products when returning to the fragment
        loadProducts()
    }
}