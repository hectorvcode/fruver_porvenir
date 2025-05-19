package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var btnLogout: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicializar el SessionManager
        sessionManager = SessionManager(this)

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Inicializar vistas
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvUserRole = findViewById(R.id.tv_user_role)
        btnLogout = findViewById(R.id.btn_logout)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Configurar navegación inferior
        setupBottomNavigation()

        // Cargar datos del usuario
        loadUserData()

        // Configurar botón de cierre de sesión
        btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadUserData() {
        // Obtener datos del usuario desde SessionManager
        val userDetails = sessionManager.getUserDetails()

        // Mostrar información del usuario
        tvUserName.text = userDetails["name"] ?: "Usuario"
        tvUserEmail.text = userDetails["email"] ?: "No disponible"

        // Mostrar rol del usuario
        val role = userDetails["role"]
        tvUserRole.text = if (role == "ADMIN") "Rol: Administrador" else "Rol: Usuario"
    }

    private fun setupBottomNavigation() {
        // Marcar el item de perfil como seleccionado
        bottomNavigationView.selectedItemId = R.id.ic_profile

        // Configurar el listener para la navegación
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    startActivity(Intent(this, ListActivity::class.java))
                    true
                }
                R.id.ic_favorites -> {
                    // Aquí iría la navegación a Favoritos cuando se implemente
                    true
                }
                R.id.ic_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.ic_profile -> {
                    // Ya estamos en Profile
                    true
                }
                else -> false
            }
        }
    }
}