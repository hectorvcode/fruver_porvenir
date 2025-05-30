package com.example.online_store.Activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.online_store.R
import com.example.online_store.data.UserDao
import com.example.online_store.model.User
import com.example.online_store.utils.ImageUtils
import com.example.online_store.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var switchAdminMode: SwitchCompat
    private lateinit var tvAdminModeExplanation: TextView
    private lateinit var btnAdminProducts: Button
    private lateinit var btnLogout: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sessionManager: SessionManager
    private lateinit var userDao: UserDao
    private lateinit var btnAdminUsers: Button

    private var isAdmin = false
    private var canBecomeAdmin = false
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicializar SessionManager y UserDao
        sessionManager = SessionManager(this)
        userDao = UserDao(this)

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Inicializar vistas
        ivProfilePhoto = findViewById(R.id.iv_profile_photo)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvUserRole = findViewById(R.id.tv_user_role)
        switchAdminMode = findViewById(R.id.switch_admin_mode)
        tvAdminModeExplanation = findViewById(R.id.tv_admin_mode_explanation)
        btnAdminProducts = findViewById(R.id.btn_admin_products)
        btnLogout = findViewById(R.id.btn_logout)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        btnAdminUsers = findViewById(R.id.btn_admin_users)

        // Configurar navegación inferior
        setupBottomNavigation()

        // Cargar datos del usuario
        loadUserData()

        // Configurar switch de modo administrador
        setupAdminModeSwitch()

        // Configurar botón de administración de productos
        setupAdminProductsButton()

        btnAdminUsers.setOnClickListener {
            startActivity(Intent(this, UserAdminActivity::class.java))
        }

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

        // Guardar email para futuras operaciones
        userEmail = userDetails["email"] ?: ""

        // Mostrar información del usuario
        tvUserName.text = userDetails["name"] ?: "Usuario"
        tvUserEmail.text = userEmail

        // Cargar imagen de perfil
        loadUserProfileImage(userDetails)

        // Verificar si el usuario puede ser administrador
        val user = userDao.getUserByEmail(userEmail)
        isAdmin = sessionManager.isAdmin()
        canBecomeAdmin = user?.role == User.ROLE_ADMIN

        // Mostrar rol del usuario
        updateRoleText()

        // Configurar visibilidad del switch según si puede ser admin
        switchAdminMode.visibility = if (canBecomeAdmin) View.VISIBLE else View.GONE
        tvAdminModeExplanation.visibility = if (canBecomeAdmin) View.VISIBLE else View.GONE

        // Configurar estado inicial del switch
        switchAdminMode.isChecked = isAdmin

        // Actualizar visibilidad del botón de administración
        updateAdminFeaturesVisibility()
    }

    private fun loadUserProfileImage(userDetails: HashMap<String, String?>) {
        val profilePicPath = userDetails["profilePicPath"]

        when {
            // Priorizar imagen personalizada
            !profilePicPath.isNullOrEmpty() && ImageUtils.imageFileExists(profilePicPath) -> {
                try {
                    val bitmap = BitmapFactory.decodeFile(profilePicPath)
                    if (bitmap != null) {
                        ivProfilePhoto.setImageBitmap(bitmap)
                        ivProfilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                    } else {
                        // Imagen por defecto si falla la carga
                        ivProfilePhoto.setImageResource(R.drawable.ic_usuario)
                        ivProfilePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ivProfilePhoto.setImageResource(R.drawable.ic_usuario)
                    ivProfilePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            }
            // Imagen por defecto
            else -> {
                ivProfilePhoto.setImageResource(R.drawable.ic_usuario)
                ivProfilePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }
    }

    private fun updateRoleText() {
        tvUserRole.text = if (isAdmin) "Rol: Administrador" else "Rol: Usuario"
    }

    private fun updateAdminFeaturesVisibility() {
        // Mostrar u ocultar los botones de administración según el rol
        btnAdminProducts.visibility = if (isAdmin) View.VISIBLE else View.GONE
        btnAdminUsers.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun setupAdminModeSwitch() {
        switchAdminMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == isAdmin) {
                return@setOnCheckedChangeListener
            }

            val newRole = if (isChecked) User.ROLE_ADMIN else User.ROLE_USER
            sessionManager.updateUserRole(newRole)

            isAdmin = isChecked
            updateRoleText()
            updateAdminFeaturesVisibility()

            val message = if (isChecked)
                "Modo administrador activado"
            else
                "Modo usuario regular activado"

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdminProductsButton() {
        btnAdminProducts.setOnClickListener {
            startActivity(Intent(this, ProductAdminActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.selectedItemId = R.id.ic_profile

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
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.ic_profile -> {
                    true
                }
                else -> false
            }
        }
    }
}