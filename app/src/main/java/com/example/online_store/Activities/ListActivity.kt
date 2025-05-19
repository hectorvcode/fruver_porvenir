package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
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

    // Referencias a los TextView para mostrar productos
    private lateinit var tvBanano: TextView
    private lateinit var tvPrecioBanano: TextView
    private lateinit var tvNaranja: TextView
    private lateinit var tvPrecioNaranja: TextView
    private lateinit var tvPina: TextView
    private lateinit var tvPrecioPina: TextView
    private lateinit var tvLimon: TextView
    private lateinit var tvPrecioLimon: TextView

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

        // Inicializar TextViews
        tvBanano = findViewById(R.id.tv_banano)
        tvPrecioBanano = findViewById(R.id.tv_precio_banano)
        tvNaranja = findViewById(R.id.tv_naranja)
        tvPrecioNaranja = findViewById(R.id.tv_precio_naranja)
        tvPina = findViewById(R.id.tv_pina)
        tvPrecioPina = findViewById(R.id.tv_precio_pina)
        tvLimon = findViewById(R.id.tv_limon)
        tvPrecioLimon = findViewById(R.id.tv_precio_limon)

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
        // Si el usuario es administrador, agregar un botón flotante para acceder a la administración
        if (sessionManager.isAdmin()) {
            val fabAdmin = FloatingActionButton(this).apply {
                setImageResource(android.R.drawable.ic_menu_manage)
                contentDescription = "Administrar productos"
            }

            fabAdmin.setOnClickListener {
                startActivity(Intent(this@ListActivity, ProductAdminActivity::class.java))
            }

            // Nota: Este código es ilustrativo. En una implementación real,
            // deberías añadir el FAB al layout XML y referenciarlo aquí.
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

        // Si no hay productos en esta categoría, mostrar mensaje
        if (products.isEmpty()) {
            Toast.makeText(this, "No hay productos en la categoría $category", Toast.LENGTH_SHORT).show()
            return
        }

        // Para este ejemplo simple, solo actualizamos los TextViews existentes
        // En una aplicación real, usarías un RecyclerView

        // Para la categoría Frutas, actualizamos los TextViews existentes
        if (category == "Frutas") {
            // Buscar los productos específicos por nombre
            val banano = products.find { it.name == "Banano" }
            val naranja = products.find { it.name == "Naranja" }
            val pina = products.find { it.name == "Piña" }
            val limon = products.find { it.name == "Limón" }

            // Actualizar los TextViews con la información de la base de datos
            banano?.let {
                tvBanano.text = it.name
                tvPrecioBanano.text = "$${it.price}/lb"
            }

            naranja?.let {
                tvNaranja.text = it.name
                tvPrecioNaranja.text = "$${it.price}/lb"
            }

            pina?.let {
                tvPina.text = it.name
                tvPrecioPina.text = "$${it.price}/lb"
            }

            limon?.let {
                tvLimon.text = it.name
                tvPrecioLimon.text = "$${it.price}/lb"
            }
        } else {
            // Para otras categorías, mostrar un mensaje indicando que necesitas implementar vistas para ellas
            Toast.makeText(
                this,
                "Hay ${products.size} productos en la categoría $category. Implementa la vista para mostrarlos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}