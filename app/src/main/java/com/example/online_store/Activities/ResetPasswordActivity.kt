package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.UserDao

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmPassword: EditText
    private lateinit var btnCambiarContrasena: Button
    private lateinit var userDao: UserDao

    private var email: String = ""
    private var isVerified: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Verificar que el usuario haya pasado por la verificación
        email = intent.getStringExtra("email") ?: ""
        isVerified = intent.getBooleanExtra("verified", false)

        if (email.isEmpty() || !isVerified) {
            Toast.makeText(this, "Acceso no autorizado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar UserDao
        userDao = UserDao(this)

        // Inicializar vistas
        initializeViews()

        // Configurar listeners
        setupListeners()

        // Configurar ActionBar
        supportActionBar?.apply {
            title = "Nueva Contraseña"
            setDisplayHomeAsUpEnabled(false) // No permitir regresar
        }
    }

    private fun initializeViews() {
        edtNewPassword = findViewById(R.id.edt_new_password)
        edtConfirmPassword = findViewById(R.id.edt_confirm_password)
        btnCambiarContrasena = findViewById(R.id.btn_cambiar_contrasena)
    }

    private fun setupListeners() {
        btnCambiarContrasena.setOnClickListener {
            changePassword()
        }

        // Configurar listener para el botón "Next" en nueva contraseña
        edtNewPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtConfirmPassword.requestFocus()
                true
            } else {
                false
            }
        }

        // Configurar listener para el botón "Done" en confirmar contraseña
        edtConfirmPassword.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                changePassword()
                true
            } else {
                false
            }
        }
    }

    private fun changePassword() {
        val newPassword = edtNewPassword.text.toString().trim()
        val confirmPassword = edtConfirmPassword.text.toString().trim()

        // Validaciones
        if (newPassword.isEmpty()) {
            edtNewPassword.error = "Por favor ingrese la nueva contraseña"
            edtNewPassword.requestFocus()
            return
        }

        if (newPassword.length < 6) {
            edtNewPassword.error = "La contraseña debe tener al menos 6 caracteres"
            edtNewPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.error = "Por favor confirme la contraseña"
            edtConfirmPassword.requestFocus()
            return
        }

        if (newPassword != confirmPassword) {
            edtConfirmPassword.error = "Las contraseñas no coinciden"
            edtConfirmPassword.requestFocus()
            return
        }

        // Verificar que el usuario existe
        val user = userDao.getUserByEmail(email)
        if (user == null) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // En una implementación real, aquí guardarías la contraseña hasheada
        // Por simplicidad, solo mostramos un mensaje de éxito

        // Simular actualización de contraseña
        try {
            // Aquí iría la lógica para actualizar la contraseña en la base de datos
            // userDao.updatePassword(email, hashedPassword)

            Toast.makeText(this, "Contraseña actualizada exitosamente", Toast.LENGTH_LONG).show()

            // Redirigir al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("password_reset_success", true)
            intent.putExtra("email", email)
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        // No permitir regresar, el usuario debe completar el proceso
        Toast.makeText(this, "Debe completar el cambio de contraseña", Toast.LENGTH_SHORT).show()
    }
}