package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.ProductListAdapter
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product
import com.example.online_store.utils.RoleHelper
import com.example.online_store.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListActivity : AppCompatActivity() {

    private lateinit var productDao: ProductDao
    private lateinit var sessionManager: SessionManager
    private lateinit var btnFrutas: Button
    private lateinit var btnVerduras: Button
    private lateinit var btnBebidas: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var rvProductsList: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        // Verificar si el usuario está logueado
        if (!RoleHelper.checkUserLoggedIn(this)) {
            return
        }

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Mostrar información del usuario en la barra de acción
        val userDetails = sessionManager.getUserDetails()
        supportActionBar?.title = "Productos - ${userDetails["name"]}"

        // Inicializar el DAO
        productDao = ProductDao(this)

        // Inicializar botones
        btnFrutas = findViewById(R.id.btn_frutas)
        btnVerduras = findViewById(R.id.btn_verduras)
        btnBebidas = findViewById(R.id.btn_bebidas)

        // Inicializar RecyclerView
        rvProductsList = findViewById(R.id.rv_products_list)
        setupRecyclerView()

        // Inicializar BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView2)
        setupBottomNavigation()

        // Configurar listeners para los botones de categoría
        setupCategoryButtons()

        // Mostrar botón de administración solo para administradores
        setupAdminAccess()

        // Cargar productos de la categoría por defecto (Frutas)
        loadProductsByCategory("Frutas")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)

        // Mostrar opción de administrador solo si el usuario es admin
        menu.findItem(R.id.action_admin)?.isVisible = sessionManager.isAdmin()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_admin -> {
                startActivity(Intent(this, ProductAdminActivity::class.java))
                true
            }
            R.id.action_logout -> {
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        // Crear el adaptador
        productListAdapter = ProductListAdapter(
            products = emptyList(),
            onProductClickListener = { product ->
                // Mostrar detalles del producto (implementar en el futuro)
                Toast.makeText(this, "Seleccionado: ${product.name}", Toast.LENGTH_SHORT).show()
            },
            onAddToCartClickListener = { product, quantity ->
                // Añadir al carrito con la cantidad seleccionada
                Toast.makeText(this, "${quantity} ${product.name} añadido al carrito", Toast.LENGTH_SHORT).show()
            }
        )

        // Configurar el RecyclerView con un GridLayoutManager para mostrar 2 columnas
        rvProductsList.apply {
            layoutManager = GridLayoutManager(this@ListActivity, 1) // 1 columna (puedes cambiar a 2 si deseas)
            adapter = productListAdapter
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    // Ya estamos en la pantalla de inicio
                    true
                }
                R.id.ic_favorites -> {
                    Toast.makeText(this, "Favoritos - Funcionalidad pendiente", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.ic_cart -> {
                    // Ir a la pantalla del carrito
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.ic_profile -> {
                    // Navegar a la pantalla de perfil
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAdminAccess() {
        // Actualizamos la ActionBar con una opción para administradores
        supportActionBar?.let { actionBar ->
            // Si el usuario es admin, podemos actualizar el título o añadir un subtítulo
            if (sessionManager.isAdmin()) {
                actionBar.subtitle = "Acceso de Administrador"
            }
        }
    }

    private fun setupCategoryButtons() {
        btnFrutas.setOnClickListener {
            btnFrutas.setBackgroundResource(R.color.fae61e)
            btnVerduras.setBackgroundResource(R.color.gray)
            btnBebidas.setBackgroundResource(R.color.gray)
            loadProductsByCategory("Frutas")
        }

        btnVerduras.setOnClickListener {
            btnFrutas.setBackgroundResource(R.color.gray)
            btnVerduras.setBackgroundResource(R.color.fae61e)
            btnBebidas.setBackgroundResource(R.color.gray)
            loadProductsByCategory("Verduras")
        }

        btnBebidas.setOnClickListener {
            btnFrutas.setBackgroundResource(R.color.gray)
            btnVerduras.setBackgroundResource(R.color.gray)
            btnBebidas.setBackgroundResource(R.color.fae61e)
            loadProductsByCategory("Bebidas")
        }
    }

    private fun loadProductsByCategory(category: String) {
        // Obtener productos de la categoría seleccionada
        val products = productDao.getProductsByCategory(category)

        // Actualizar el adaptador con los productos filtrados
        productListAdapter.updateProducts(products)

        // Si no hay productos en esta categoría, mostrar mensaje
        if (products.isEmpty()) {
            Toast.makeText(this, "No hay productos en la categoría $category", Toast.LENGTH_SHORT).show()
        }
    }
}