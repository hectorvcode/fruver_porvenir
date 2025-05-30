package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.utils.EmailHelper

class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var tvInstructions: TextView
    private lateinit var edtVerificationCode: EditText
    private lateinit var btnVerificarCodigo: Button
    private lateinit var btnReenviarCodigo: Button
    private lateinit var tvVolverAtras: TextView
    private lateinit var tvTiempoRestante: TextView

    private var email: String = ""
    private var recoveryCode: String = ""
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        // Obtener datos del intent
        email = intent.getStringExtra("email") ?: ""
        recoveryCode = intent.getStringExtra("recovery_code") ?: ""

        if (email.isEmpty() || recoveryCode.isEmpty()) {
            Toast.makeText(this, "Error: Datos de recuperación inválidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar vistas
        initializeViews()

        // Configurar listeners
        setupListeners()

        // Configurar ActionBar
        supportActionBar?.apply {
            title = "Verificar Código"
            setDisplayHomeAsUpEnabled(true)
        }

        // Iniciar contador regresivo
        startCountdownTimer()

        // Mostrar instrucciones personalizadas
        tvInstructions.text = "Hemos enviado un código de verificación a:\n$email"
    }

    private fun initializeViews() {
        tvInstructions = findViewById(R.id.tv_instructions)
        edtVerificationCode = findViewById(R.id.edt_verification_code)
        btnVerificarCodigo = findViewById(R.id.btn_verificar_codigo)
        btnReenviarCodigo = findViewById(R.id.btn_reenviar_codigo)
        tvVolverAtras = findViewById(R.id.tv_volver_atras)
        tvTiempoRestante = findViewById(R.id.tv_tiempo_restante)
    }

    private fun setupListeners() {
        btnVerificarCodigo.setOnClickListener {
            verifyCode()
        }

        btnReenviarCodigo.setOnClickListener {
            resendCode()
        }

        tvVolverAtras.setOnClickListener {
            finish()
        }

        // Configurar listener para el botón "Done" del teclado
        edtVerificationCode.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                verifyCode()
                true
            } else {
                false
            }
        }
    }

    private fun verifyCode() {
        val enteredCode = edtVerificationCode.text.toString().trim()

        if (enteredCode.isEmpty()) {
            edtVerificationCode.error = "Por favor ingrese el código"
            edtVerificationCode.requestFocus()
            return
        }

        if (enteredCode.length != 6) {
            edtVerificationCode.error = "El código debe tener 6 dígitos"
            edtVerificationCode.requestFocus()
            return
        }

        if (enteredCode == recoveryCode) {
            // Código correcto, ir a la pantalla de nueva contraseña
            countDownTimer?.cancel()

            val intent = Intent(this, ResetPasswordActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("verified", true)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Código incorrecto. Intente nuevamente.", Toast.LENGTH_SHORT).show()
            edtVerificationCode.text.clear()
            edtVerificationCode.requestFocus()
        }
    }

    private fun resendCode() {
        // Generar nuevo código
        recoveryCode = generateRecoveryCode()

        // Enviar nuevo email
        val emailSent = EmailHelper.sendRecoveryEmail(this, email, recoveryCode)

        if (emailSent) {
            Toast.makeText(this, "Código reenviado exitosamente", Toast.LENGTH_SHORT).show()
            edtVerificationCode.text.clear()

            // Reiniciar contador
            startCountdownTimer()
        } else {
            Toast.makeText(this, "Error al reenviar el código", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateRecoveryCode(): String {
        return (100000..999999).random().toString()
    }

    private fun startCountdownTimer() {
        // Deshabilitar botón de reenvío temporalmente
        btnReenviarCodigo.isEnabled = false

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) { // 1 minuto
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                tvTiempoRestante.text = "Reenviar código en ${secondsRemaining}s"
            }

            override fun onFinish() {
                tvTiempoRestante.text = "Puedes reenviar el código ahora"
                btnReenviarCodigo.isEnabled = true
            }
        }
        countDownTimer?.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}