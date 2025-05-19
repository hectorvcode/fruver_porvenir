package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.UserDao
import com.example.online_store.model.User
import com.example.online_store.utils.SessionManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegistrarse: Button
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar UserDao y SessionManager
        userDao = UserDao(this)
        sessionManager = SessionManager(this)

        // Inicializar vistas
        edtFullName = findViewById(R.id.edt_full_name)
        edtEmail = findViewById(R.id.edt_email_login)
        edtPassword = findViewById(R.id.edt_password_login)
        btnRegistrarse = findViewById(R.id.btn_registrarse)

        // Configurar listener para el botón de registro
        btnRegistrarse.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        // Obtener datos del formulario
        val fullName = edtFullName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        // Validación básica
        if (fullName.isEmpty()) {
            edtFullName.error = "Por favor ingrese su nombre completo"
            return
        }

        if (email.isEmpty()) {
            edtEmail.error = "Por favor ingrese su email"
            return
        }

        if (password.isEmpty()) {
            edtPassword.error = "Por favor ingrese una contraseña"
            return
        }

        // Verificar si el email ya existe
        if (userDao.getUserByEmail(email) != null) {
            Toast.makeText(this, "El email ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear nuevo usuario (siempre con rol USER)
        val user = User(
            email = email,
            name = fullName,
            role = User.ROLE_USER
        )

        // Insertar en la base de datos
        val result = userDao.insertUser(user)

        if (result > 0) {
            // Registro exitoso
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

            // Crear sesión
            sessionManager.createLoginSession(user)

            // Redireccionar a la lista de productos
            startActivity(Intent(this, ListActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Error al registrar. Intente nuevamente", Toast.LENGTH_SHORT).show()
        }
    }
}