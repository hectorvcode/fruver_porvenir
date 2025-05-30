package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.ProductAdapter
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product
import com.example.online_store.utils.RoleHelper
import com.example.online_store.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProductAdminActivity : AppCompatActivity() {

    private lateinit var productDao: ProductDao
    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var spCategoryFilter: Spinner
    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigationView: BottomNavigationView

    private val categories = listOf("Todos", "Frutas", "Verduras", "Bebidas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_admin)

        // Verificar permisos de administrador
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Inicializar el DAO
        productDao = ProductDao(this)

        // Configurar la ActionBar sin botón de retroceso
        supportActionBar?.title = "Administración de Productos"

        // Inicializar vistas
        rvProducts = findViewById(R.id.rv_products)
        fabAdd = findViewById(R.id.fab_add)
        spCategoryFilter = findViewById(R.id.sp_category_filter)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Configurar BottomNavigationView
        setupBottomNavigation()

        // Configurar spinner de categorías primero
        setupCategorySpinner()

        // Configurar RecyclerView después
        setupRecyclerView()

        // Configurar FAB para añadir productos
        fabAdd.setOnClickListener {
            val intent = Intent(this, ProductFormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Navegar al MainContainerActivity con el fragmento de profile
        val intent = Intent(this, MainContainerActivity::class.java)
        intent.putExtra("fragment", "profile")
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Verificar permisos de administrador cada vez que se vuelve a la actividad
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Actualizar la lista de productos al volver a la actividad
        if (::productAdapter.isInitialized) {
            loadProducts()
        }
    }

    private fun setupBottomNavigation() {
        // Configurar el listener para la navegación
        bottomNavigationView.setOnItemSelectedListener { item ->
            val intent = Intent(this, MainContainerActivity::class.java)
            when (item.itemId) {
                R.id.ic_home -> {
                    // No necesita extra, va al home por defecto
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_favorites -> {
                    intent.putExtra("fragment", "favorites")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_cart -> {
                    intent.putExtra("fragment", "cart")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_stores -> {
                    intent.putExtra("fragment", "stores")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_profile -> {
                    intent.putExtra("fragment", "profile")
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        // Crear el adaptador
        productAdapter = ProductAdapter(
            products = emptyList(),
            onEditClickListener = { product ->
                // Abrir la actividad de edición
                val intent = Intent(this, ProductFormActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.id)
                startActivity(intent)
            },
            onDeleteClickListener = { product ->
                // Mostrar diálogo de confirmación
                showDeleteConfirmationDialog(product)
            }
        )

        // Configurar el RecyclerView
        rvProducts.apply {
            layoutManager = LinearLayoutManager(this@ProductAdminActivity)
            adapter = productAdapter
        }

        // Cargar los productos
        loadProducts()
    }

    private fun setupCategorySpinner() {
        // Crear adaptador para el spinner
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoryFilter.adapter = spinnerAdapter

        // Configurar listener
        spCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Filtrar por la categoría seleccionada
                if (::productAdapter.isInitialized) {
                    loadProducts()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun loadProducts() {
        // Verificar si el spinner está inicializado y tiene un elemento seleccionado
        val selectedCategory = if (::spCategoryFilter.isInitialized && spCategoryFilter.selectedItem != null) {
            spCategoryFilter.selectedItem.toString()
        } else {
            "Todos" // Valor predeterminado si no hay selección
        }

        // Obtener los productos según la categoría seleccionada
        val products = if (selectedCategory == "Todos") {
            productDao.getAllProducts()
        } else {
            productDao.getProductsByCategory(selectedCategory)
        }

        // Actualizar el adaptador
        if (::productAdapter.isInitialized) {
            productAdapter.updateProducts(products)

            // Mostrar mensaje si no hay productos
            if (products.isEmpty()) {
                Toast.makeText(
                    this,
                    if (selectedCategory == "Todos") "No hay productos registrados"
                    else "No hay productos en la categoría $selectedCategory",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar el producto ${product.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Eliminar el producto
                val result = productDao.deleteProduct(product.id)

                if (result > 0) {
                    Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show()
                    // Recargar la lista
                    loadProducts()
                } else {
                    Toast.makeText(this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}