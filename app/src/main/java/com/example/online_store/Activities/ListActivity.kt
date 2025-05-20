package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.fragments.ProductsFragment
import com.example.online_store.fragments.StoresMapFragment
import com.example.online_store.utils.RoleHelper
import com.example.online_store.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class ListActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigationView: BottomNavigationView

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

        // Inicializar BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView2)
        setupBottomNavigation()

        // Load the ProductsFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, ProductsFragment())
                .commit()
        }

        // Mostrar botón de administración solo para administradores
        setupAdminAccess()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)

        // Mostrar opción de administrador solo si el usuario es admin
        menu.findItem(R.id.action_admin)?.isVisible = sessionManager.isAdmin()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_stores -> {
                // Cargar el fragmento del mapa
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, StoresMapFragment.newInstance())
                    .addToBackStack(null)
                    .commit()
                true
            }
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
        // Actualizamos la ActionBar con una opción para administradores
        supportActionBar?.let { actionBar ->
            // Si el usuario es admin, podemos actualizar el título o añadir un subtítulo
            if (sessionManager.isAdmin()) {
                actionBar.subtitle = "Acceso de Administrador"
            }
        }
    }
}