package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.utils.SessionManager
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var tvSuccessMessage: TextView
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvAmount: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDeliveryInfo: TextView
    private lateinit var btnContinueShopping: Button

    private lateinit var sessionManager: SessionManager

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Inicializar vistas
        initializeViews()

        // Obtener datos del intent
        val totalAmount = intent.getDoubleExtra("total_amount", 0.0)
        val itemCount = intent.getIntExtra("item_count", 0)

        // Configurar información de la compra
        setupPaymentInfo(totalAmount, itemCount)

        // Configurar listeners
        setupListeners()

        // Configurar ActionBar
        supportActionBar?.apply {
            title = "Compra Exitosa"
            setDisplayHomeAsUpEnabled(false) // No permitir regresar
        }

        // Auto-redirección después de 10 segundos (opcional)
        scheduleAutoRedirect()
    }

    private fun initializeViews() {
        tvSuccessMessage = findViewById(R.id.tv_success_message)
        tvOrderNumber = findViewById(R.id.tv_order_number)
        tvAmount = findViewById(R.id.tv_amount)
        tvDate = findViewById(R.id.tv_date)
        tvDeliveryInfo = findViewById(R.id.tv_delivery_info)
        btnContinueShopping = findViewById(R.id.btn_continue_shopping)
    }

    private fun setupPaymentInfo(totalAmount: Double, itemCount: Int) {
        // Obtener información del usuario
        val userDetails = sessionManager.getUserDetails()
        val userName = userDetails["name"] ?: "Cliente"

        // Configurar mensaje de éxito personalizado
        tvSuccessMessage.text = "¡Gracias $userName!\nTu compra se realizó exitosamente"

        // Generar número de orden único
        val orderNumber = generateOrderNumber()
        tvOrderNumber.text = "Número de orden: #$orderNumber"

        // Mostrar monto total
        tvAmount.text = "Total pagado: ${currencyFormat.format(totalAmount)}"

        // Mostrar fecha y hora actual
        val currentDate = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm", Locale("es", "CO")).format(Date())
        tvDate.text = "Fecha: $currentDate"

        // Información de entrega
        val deliveryText = if (itemCount == 1) {
            "Tu producto será entregado en las próximas 24-48 horas."
        } else {
            "Tus $itemCount productos serán entregados en las próximas 24-48 horas."
        }
        tvDeliveryInfo.text = deliveryText
    }

    private fun setupListeners() {
        btnContinueShopping.setOnClickListener {
            // Redirigir a la pantalla principal de productos
            val intent = Intent(this, MainContainerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun generateOrderNumber(): String {
        // Generar un número de orden único basado en timestamp y usuario
        val timestamp = System.currentTimeMillis()
        val userInitials = sessionManager.getUserDetails()["name"]?.take(2)?.uppercase() ?: "FP"
        return "$userInitials${timestamp.toString().takeLast(6)}"
    }

    private fun scheduleAutoRedirect() {
        // Auto-redirección después de 30 segundos (opcional)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                btnContinueShopping.performClick()
            }
        }, 30000) // 30 segundos
    }

    override fun onBackPressed() {
        // Redirigir a la pantalla principal en lugar de permitir regresar al carrito
        btnContinueShopping.performClick()
    }
}