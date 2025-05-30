package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.UserDao
import com.example.online_store.utils.EmailHelper

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var btnEnviarCodigo: Button
    private lateinit var tvVolverLogin: TextView
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Inicializar UserDao
        userDao = UserDao(this)

        // Inicializar vistas
        initializeViews()

        // Configurar listeners
        setupListeners()

        // Configurar ActionBar
        supportActionBar?.apply {
            title = "Recuperar Contraseña"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun initializeViews() {
        edtEmail = findViewById(R.id.edt_email_recovery)
        btnEnviarCodigo = findViewById(R.id.btn_enviar_codigo)
        tvVolverLogin = findViewById(R.id.tv_volver_login)
    }

    private fun setupListeners() {
        btnEnviarCodigo.setOnClickListener {
            sendRecoveryCode()
        }

        tvVolverLogin.setOnClickListener {
            finish() // Volver a la actividad anterior (LoginActivity)
        }

        // Configurar listener para el botón "Done" del teclado
        edtEmail.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                sendRecoveryCode()
                true
            } else {
                false
            }
        }
    }

    private fun sendRecoveryCode() {
        val email = edtEmail.text.toString().trim()

        // Validación básica
        if (email.isEmpty()) {
            edtEmail.error = "Por favor ingrese su email"
            edtEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.error = "Por favor ingrese un email válido"
            edtEmail.requestFocus()
            return
        }

        // Verificar si el email existe en la base de datos
        val user = userDao.getUserByEmail(email)

        if (user == null) {
            Toast.makeText(this, "No encontramos una cuenta con este email", Toast.LENGTH_LONG).show()
            return
        }

        // Generar código de recuperación
        val recoveryCode = generateRecoveryCode()

        // Enviar email con el código
        val emailSent = EmailHelper.sendRecoveryEmail(this, email, recoveryCode)

        if (emailSent) {
            // Ir a la actividad de verificación de código
            val intent = Intent(this, VerifyCodeActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("recovery_code", recoveryCode)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(
                this,
                "Error al enviar el email. Verifique su conexión a internet e intente nuevamente.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun generateRecoveryCode(): String {
        // Generar un código de 6 dígitos
        return (100000..999999).random().toString()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}