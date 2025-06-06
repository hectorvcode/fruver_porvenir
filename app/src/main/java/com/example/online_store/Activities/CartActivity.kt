package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.CartAdapter
import com.example.online_store.utils.CartManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var cartManager: CartManager
    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvEmptyCart: TextView
    private lateinit var cvCartSummary: CardView
    private lateinit var tvTotalValue: TextView
    private lateinit var btnPagar: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var cartAdapter: CartAdapter

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Inicializar CartManager
        cartManager = CartManager(this)

        // Inicializar vistas
        rvCartItems = findViewById(R.id.rv_cart_items)
        tvEmptyCart = findViewById(R.id.tv_empty_cart)
        cvCartSummary = findViewById(R.id.cv_cart_summary)
        tvTotalValue = findViewById(R.id.tv_total_value)
        btnPagar = findViewById(R.id.btn_pagar)
        bottomNavigationView = findViewById(R.id.bottomNavigationView2)

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar BottomNavigationView
        setupBottomNavigation()

        // Configurar botón de pago
        btnPagar.setOnClickListener {
            processPayment()
        }

        // Cargar datos del carrito
        loadCartData()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar datos del carrito cuando se vuelve a la actividad
        loadCartData()
    }

    override fun onBackPressed() {
        // Navegar al MainContainerActivity con el fragmento de carrito
        val intent = Intent(this, MainContainerActivity::class.java)
        intent.putExtra("fragment", "home")
        startActivity(intent)
        finish()
    }

    private fun setupRecyclerView() {
        // Crear adaptador
        cartAdapter = CartAdapter(
            cartItems = emptyList(),
            onQuantityChangedListener = { cartItem ->
                // Actualizar cantidad en el gestor de carrito
                cartManager.updateQuantity(cartItem.product.id, cartItem.quantity)
                // Actualizar resumen
                updateCartSummary()
            },
            onItemRemovedListener = { cartItem ->
                // Eliminar ítem del carrito
                cartManager.removeFromCart(cartItem.product.id)
                // Recargar datos
                loadCartData()
                // Mostrar mensaje
                Toast.makeText(this, "${cartItem.product.name} eliminado del carrito", Toast.LENGTH_SHORT).show()
            }
        )

        // Configurar RecyclerView
        rvCartItems.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun setupBottomNavigation() {
        // Seleccionar el ítem del carrito
        bottomNavigationView.selectedItemId = R.id.ic_cart

        // Configurar listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    val intent = Intent(this, MainContainerActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_favorites -> {
                    val intent = Intent(this, MainContainerActivity::class.java)
                    intent.putExtra("fragment", "favorites")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_cart -> {
                    // Ya estamos en el carrito
                    true
                }
                R.id.ic_stores -> {
                    val intent = Intent(this, MainContainerActivity::class.java)
                    intent.putExtra("fragment", "stores")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_profile -> {
                    val intent = Intent(this, MainContainerActivity::class.java)
                    intent.putExtra("fragment", "profile")
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadCartData() {
        // Obtener ítems del carrito
        val cartItems = cartManager.getCartItems()

        // Actualizar adaptador
        cartAdapter.updateCartItems(cartItems)

        // Mostrar mensaje de carrito vacío si no hay ítems
        if (cartItems.isEmpty()) {
            tvEmptyCart.visibility = View.VISIBLE
            cvCartSummary.visibility = View.GONE
            btnPagar.isEnabled = false
            btnPagar.alpha = 0.5f
        } else {
            tvEmptyCart.visibility = View.GONE
            cvCartSummary.visibility = View.VISIBLE
            btnPagar.isEnabled = true
            btnPagar.alpha = 1.0f

            // Actualizar resumen del carrito
            updateCartSummary()
        }
    }

    private fun updateCartSummary() {
        // Calcular y mostrar total
        val total = cartManager.getTotal()
        tvTotalValue.text = currencyFormat.format(total)
    }

    private fun processPayment() {
        // Obtener información del carrito antes de limpiarlo
        val cartItems = cartManager.getCartItems()
        val totalAmount = cartManager.getTotal()
        val itemCount = cartItems.size

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar mensaje de procesamiento
        Toast.makeText(this, "Procesando pago de ${currencyFormat.format(totalAmount)}", Toast.LENGTH_SHORT).show()

        // Simular un pequeño delay para el procesamiento
        btnPagar.isEnabled = false
        btnPagar.text = "Procesando..."

        // Procesar pago después de un breve delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Limpiar el carrito
            cartManager.clearCart()

            // Crear intent para la pantalla de éxito
            val intent = Intent(this, PaymentSuccessActivity::class.java)
            intent.putExtra("total_amount", totalAmount)
            intent.putExtra("item_count", itemCount)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Iniciar la actividad de éxito
            startActivity(intent)
            finish()
        }, 1500) // 1.5 segundos de delay para simular procesamiento
    }
}