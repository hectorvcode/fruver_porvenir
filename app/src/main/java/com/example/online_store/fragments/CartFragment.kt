package com.example.online_store.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.Activities.PaymentSuccessActivity
import com.example.online_store.R
import com.example.online_store.adapter.CartAdapter
import com.example.online_store.utils.CartManager
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var cartManager: CartManager
    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvEmptyCart: TextView
    private lateinit var cvCartSummary: CardView
    private lateinit var tvTotalValue: TextView
    private lateinit var btnPagar: Button
    private lateinit var cartAdapter: CartAdapter

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar CartManager
        cartManager = CartManager(requireContext())

        // Inicializar vistas
        rvCartItems = view.findViewById(R.id.rv_cart_items)
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart)
        cvCartSummary = view.findViewById(R.id.cv_cart_summary)
        tvTotalValue = view.findViewById(R.id.tv_total_value)
        btnPagar = view.findViewById(R.id.btn_pagar)

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar botón de pago
        btnPagar.setOnClickListener {
            processPayment()
        }

        // Cargar datos del carrito
        loadCartData()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar datos del carrito cuando se vuelve al fragmento
        loadCartData()
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
            }
        )

        // Configurar RecyclerView
        rvCartItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
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
            Toast.makeText(requireContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar mensaje de procesamiento
        Toast.makeText(requireContext(), "Procesando pago de ${currencyFormat.format(totalAmount)}", Toast.LENGTH_SHORT).show()

        // Simular un pequeño delay para el procesamiento
        btnPagar.isEnabled = false
        btnPagar.text = "Procesando..."

        // Procesar pago después de un breve delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Limpiar el carrito
            cartManager.clearCart()

            // Crear intent para la pantalla de éxito
            val intent = Intent(requireContext(), PaymentSuccessActivity::class.java)
            intent.putExtra("total_amount", totalAmount)
            intent.putExtra("item_count", itemCount)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Iniciar la actividad de éxito
            startActivity(intent)
            requireActivity().finish()
        }, 1500) // 1.5 segundos de delay para simular procesamiento
    }

    companion object {
        fun newInstance() = CartFragment()
    }
}