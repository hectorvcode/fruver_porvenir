package com.example.online_store.Activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.UserDao
import com.example.online_store.model.User
import com.example.online_store.utils.RoleHelper
import com.google.android.material.textfield.TextInputEditText

class UserFormActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    private lateinit var tvTituloUsuario: TextView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var rgRol: RadioGroup
    private lateinit var rbUser: RadioButton
    private lateinit var rbAdmin: RadioButton
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private var isEditMode = false
    private var originalEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_form)

        // Verificar permisos de administrador
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Configurar la ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializar el DAO
        userDao = UserDao(this)

        // Inicializar vistas
        tvTituloUsuario = findViewById(R.id.tv_titulo_usuario)
        etNombre = findViewById(R.id.et_nombre)
        etEmail = findViewById(R.id.et_email)
        rgRol = findViewById(R.id.rg_rol)
        rbUser = findViewById(R.id.rb_user)
        rbAdmin = findViewById(R.id.rb_admin)
        btnGuardar = findViewById(R.id.btn_guardar)
        btnCancelar = findViewById(R.id.btn_cancelar)

        // Verificar si estamos en modo edición
        if (intent.hasExtra("USER_EMAIL")) {
            originalEmail = intent.getStringExtra("USER_EMAIL") ?: ""
            isEditMode = true
            supportActionBar?.title = "Editar Usuario"
            tvTituloUsuario.text = "Editar Usuario"
            loadUserData()
        } else {
            supportActionBar?.title = "Nuevo Usuario"
            tvTituloUsuario.text = "Nuevo Usuario"
        }

        // Configurar listeners
        btnGuardar.setOnClickListener {
            saveUser()
        }

        btnCancelar.setOnClickListener {
            finish() // Volver a la actividad anterior
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUserData() {
        // Cargar los datos del usuario para edición
        val user = userDao.getUserByEmail(originalEmail)

        user?.let {
            // Llenar el formulario con los datos del usuario
            etNombre.setText(it.name)
            etEmail.setText(it.email)

            // Establecer el rol
            if (it.role == User.ROLE_ADMIN) {
                rbAdmin.isChecked = true
            } else {
                rbUser.isChecked = true
            }

            // No permitir editar el correo de admin@example.com
            if (it.email == "admin@example.com") {
                etEmail.isEnabled = false
                // No permitir cambiar el rol del admin principal
                rgRol.isEnabled = false
                rbAdmin.isEnabled = false
                rbUser.isEnabled = false
            }
        } ?: run {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveUser() {
        // Validar el formulario
        if (!validateForm()) {
            return
        }

        // Obtener los valores del formulario
        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()

        // Obtener el rol seleccionado
        val role = if (rbAdmin.isChecked) User.ROLE_ADMIN else User.ROLE_USER

        // Crear el objeto usuario
        val user = User(
            id = 0, // El ID se asigna automáticamente en la base de datos
            email = email,
            name = nombre,
            role = role
        )

        if (isEditMode) {
            // Si estamos modificando el email, necesitamos borrar el usuario antiguo y crear uno nuevo
            if (email != originalEmail) {
                userDao.deleteUser(originalEmail)
                val newUserId = userDao.insertUser(user)

                if (newUserId > 0) {
                    Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Actualizar solo el nombre y el rol
                val rowsAffected = userDao.updateUserNameAndRole(email, nombre, role)

                if (rowsAffected > 0) {
                    Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Insertar nuevo usuario
            val id = userDao.insertUser(user)

            if (id > 0) {
                Toast.makeText(this, "Usuario agregado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else if (id == -1L) {
                Toast.makeText(this, "El correo electrónico ya está registrado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al agregar el usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validar nombre
        if (etNombre.text.toString().trim().isEmpty()) {
            etNombre.error = "El nombre es obligatorio"
            isValid = false
        }

        // Validar email
        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = "El correo electrónico es obligatorio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Ingrese un correo electrónico válido"
            isValid = false
        }

        return isValid
    }
}