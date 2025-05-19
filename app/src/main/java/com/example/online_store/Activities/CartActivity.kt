package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class CartActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val ivBack: ImageView = findViewById(R.id.iv_ic_back)
        ivBack.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        // Inicializar el BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottomNavigationView2)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        // Marcar el item del carrito como seleccionado
        bottomNavigationView.selectedItemId = R.id.ic_cart

        // Configurar el listener para la navegación
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    startActivity(Intent(this, ListActivity::class.java))
                    true
                }
                R.id.ic_favorites -> {
                    Toast.makeText(this, "Favoritos - Funcionalidad pendiente", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.ic_cart -> {
                    // Ya estamos en el carrito
                    true
                }
                R.id.ic_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}