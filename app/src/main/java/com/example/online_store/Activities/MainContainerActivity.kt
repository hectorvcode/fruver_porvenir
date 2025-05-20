package com.example.online_store.Activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.online_store.R
import com.example.online_store.fragments.CartFragment
import com.example.online_store.fragments.FavoritesFragment
import com.example.online_store.fragments.HomeFragment
import com.example.online_store.fragments.ProfileFragment
import com.example.online_store.fragments.StoresMapFragment
import com.example.online_store.utils.RoleHelper
import com.example.online_store.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainContainerActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sessionManager: SessionManager

    // Fragmentos
    private val homeFragment by lazy { HomeFragment.newInstance() }
    private val favoritesFragment by lazy { FavoritesFragment.newInstance() }
    private val cartFragment by lazy { CartFragment.newInstance() }
    private val storesMapFragment by lazy { StoresMapFragment.newInstance() }
    private val profileFragment by lazy { ProfileFragment.newInstance() }

    // Fragmento actualmente mostrado
    private var currentFragment: Fragment? = null

    // Tag para guardar el ítem seleccionado en savedInstanceState
    private val SELECTED_ITEM = "selected_item"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        // Verificar si el usuario está logueado
        if (!RoleHelper.checkUserLoggedIn(this)) {
            return
        }

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Configurar información del usuario en la ActionBar
        val userDetails = sessionManager.getUserDetails()
        supportActionBar?.title = "Fruver Porvenir - ${userDetails["name"]}"

        // Inicializar BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        setupBottomNavigation()

        // Restaurar el estado si existe, de lo contrario cargar el fragmento inicial
        if (savedInstanceState != null) {
            // Recuperar el ítem seleccionado
            val selectedItemId = savedInstanceState.getInt(SELECTED_ITEM, R.id.ic_home)
            // Seleccionar ese ítem en la barra de navegación
            bottomNavigationView.selectedItemId = selectedItemId
        } else {
            // Cargar el fragmento inicial (Home)
            loadFragment(homeFragment)
            currentFragment = homeFragment
        }

        // Configurar acceso de administrador si es necesario
        setupAdminAccess()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Guardar el ítem seleccionado actual
        outState.putInt(SELECTED_ITEM, bottomNavigationView.selectedItemId)
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    loadFragment(HomeFragment.newInstance())
                    true
                }
                R.id.ic_favorites -> {
                    loadFragment(FavoritesFragment.newInstance())
                    true
                }
                R.id.ic_cart -> {
                    loadFragment(CartFragment.newInstance())
                    true
                }
                R.id.ic_stores -> {
                    loadFragment(StoresMapFragment.newInstance())
                    true
                }
                R.id.ic_profile -> {
                    loadFragment(ProfileFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupAdminAccess() {
        // Actualizar la ActionBar para administradores
        supportActionBar?.let { actionBar ->
            if (sessionManager.isAdmin()) {
                actionBar.subtitle = "Acceso de Administrador"
            }
        }
    }

    override fun onBackPressed() {
        // Si el fragmento actual no es Home, navega a Home
        if (bottomNavigationView.selectedItemId != R.id.ic_home) {
            bottomNavigationView.selectedItemId = R.id.ic_home
        } else {
            // Si ya estamos en Home, comportamiento normal de retroceso
            super.onBackPressed()
        }
    }
}