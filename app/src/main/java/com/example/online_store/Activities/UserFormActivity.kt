package com.example.online_store.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.online_store.R
import com.example.online_store.data.UserDao
import com.example.online_store.model.User
import com.example.online_store.utils.ImageUtils
import com.example.online_store.utils.RoleHelper
import com.example.online_store.utils.SessionManager
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class UserFormActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    // Vistas existentes
    private lateinit var tvTituloUsuario: TextView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tvPasswordHelp: TextView
    private lateinit var rgRol: RadioGroup
    private lateinit var rbUser: RadioButton
    private lateinit var rbAdmin: RadioButton
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    // Nuevas vistas para imagen de perfil
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnSelectProfileImage: Button
    private lateinit var btnTakeProfilePhoto: Button

    private var isEditMode = false
    private var originalEmail = ""
    private var currentProfileImagePath: String? = null

    // Activity Result Launchers
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_form)

        // Verificar permisos de administrador
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Configurar la ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializar el DAO y SessionManager
        userDao = UserDao(this)
        sessionManager = SessionManager(this)

        // Inicializar vistas
        initializeViews()

        // Configurar Activity Result Launchers
        setupActivityResultLaunchers()

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
            tvPasswordHelp.text = "Ingrese una contraseña para el nuevo usuario"
        }

        // Configurar listeners
        setupListeners()
    }

    private fun initializeViews() {
        // Vistas existentes
        tvTituloUsuario = findViewById(R.id.tv_titulo_usuario)
        etNombre = findViewById(R.id.et_nombre)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        tvPasswordHelp = findViewById(R.id.tv_password_help)
        rgRol = findViewById(R.id.rg_rol)
        rbUser = findViewById(R.id.rb_user)
        rbAdmin = findViewById(R.id.rb_admin)
        btnGuardar = findViewById(R.id.btn_guardar)
        btnCancelar = findViewById(R.id.btn_cancelar)

        // Nuevas vistas para imagen de perfil
        ivProfileImage = findViewById(R.id.iv_profile_image)
        btnSelectProfileImage = findViewById(R.id.btn_select_profile_image)
        btnTakeProfilePhoto = findViewById(R.id.btn_take_profile_photo)
    }

    private fun setupActivityResultLaunchers() {
        // Launcher para la cámara
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                handleCameraResult()
            }
        }

        // Launcher para la galería
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    handleGalleryResult(uri)
                }
            }
        }

        // Launcher para permisos
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para tomar fotos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        // Listeners existentes
        btnGuardar.setOnClickListener {
            saveUser()
        }

        btnCancelar.setOnClickListener {
            finish()
        }

        // Nuevos listeners para imagen de perfil
        btnSelectProfileImage.setOnClickListener {
            openGallery()
        }

        btnTakeProfilePhoto.setOnClickListener {
            checkCameraPermissionAndOpen()
        }
    }

    private fun openGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"

            if (galleryIntent.resolveActivity(packageManager) != null) {
                galleryLauncher.launch(galleryIntent)
            } else {
                val alternativeIntent = Intent(Intent.ACTION_GET_CONTENT)
                alternativeIntent.type = "image/*"
                if (alternativeIntent.resolveActivity(packageManager) != null) {
                    galleryLauncher.launch(alternativeIntent)
                } else {
                    Toast.makeText(this, "No se encontró una aplicación de galería", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al abrir la galería: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleGalleryResult(imageUri: Uri) {
        try {
            if (!ImageUtils.isUriAccessible(this, imageUri)) {
                Toast.makeText(this, "No se puede acceder a la imagen seleccionada", Toast.LENGTH_SHORT).show()
                return
            }

            val processedImagePath = ImageUtils.processImageFromUri(this, imageUri)

            if (processedImagePath != null) {
                currentProfileImagePath = processedImagePath
                val bitmap = ImageUtils.compressAndRotateImage(processedImagePath)
                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap)
                    ivProfileImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    Toast.makeText(this, "Imagen de perfil seleccionada exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                    currentProfileImagePath = null
                }
            } else {
                Toast.makeText(this, "Error al copiar la imagen", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = ImageUtils.createImageFile(this)
            currentProfileImagePath = photoFile.absolutePath

            val photoURI = ImageUtils.getUriForFile(this, photoFile)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(takePictureIntent)
            } else {
                Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCameraResult() {
        currentProfileImagePath?.let { imagePath ->
            try {
                val bitmap = ImageUtils.compressAndRotateImage(imagePath)

                if (bitmap != null) {
                    val compressedFile = File(imagePath)
                    if (ImageUtils.saveBitmapToFile(bitmap, compressedFile)) {
                        ivProfileImage.setImageBitmap(bitmap)
                        ivProfileImage.scaleType = ImageView.ScaleType.CENTER_CROP
                        Toast.makeText(this, "Foto de perfil capturada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                        currentProfileImagePath = null
                    }
                } else {
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                    currentProfileImagePath = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                currentProfileImagePath = null
            }
        }
    }

    private fun loadUserData() {
        val user = userDao.getUserByEmail(originalEmail)

        user?.let {
            etNombre.setText(it.name)
            etEmail.setText(it.email)

            // Cargar imagen de perfil
            loadUserProfileImage(it)

            if (it.role == User.ROLE_ADMIN) {
                rbAdmin.isChecked = true
            } else {
                rbUser.isChecked = true
            }

            if (it.email == "admin@example.com") {
                etEmail.isEnabled = false
                rgRol.isEnabled = false
                rbAdmin.isEnabled = false
                rbUser.isEnabled = false
                etPassword.isEnabled = false
                tvPasswordHelp.text = "No se puede modificar la contraseña del administrador principal"
            } else {
                tvPasswordHelp.text = "Deje en blanco para mantener la contraseña actual"
            }
        } ?: run {
            Toast.makeText(this, "No se encontró el usuario", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadUserProfileImage(user: User) {
        when {
            // Priorizar imagen personalizada
            !user.profilePicPath.isNullOrEmpty() && ImageUtils.imageFileExists(user.profilePicPath) -> {
                try {
                    currentProfileImagePath = user.profilePicPath
                    val bitmap = BitmapFactory.decodeFile(user.profilePicPath)
                    if (bitmap != null) {
                        ivProfileImage.setImageBitmap(bitmap)
                        ivProfileImage.scaleType = ImageView.ScaleType.CENTER_CROP
                    } else {
                        ivProfileImage.setImageResource(R.drawable.ic_usuario)
                        ivProfileImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ivProfileImage.setImageResource(R.drawable.ic_usuario)
                    ivProfileImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
            }
            // Imagen por defecto
            else -> {
                ivProfileImage.setImageResource(R.drawable.ic_usuario)
                ivProfileImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
        }
    }

    private fun saveUser() {
        if (!validateForm()) {
            return
        }

        val nombre = etNombre.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val role = if (rbAdmin.isChecked) User.ROLE_ADMIN else User.ROLE_USER

        // Crear objeto usuario
        val user = User(
            id = 0,
            email = email,
            name = nombre,
            role = role,
            password = password, // La contraseña se procesará adecuadamente en UserDao
            profilePicPath = currentProfileImagePath
        )

        if (isEditMode) {
            if (email != originalEmail) {
                // Si cambió el email, eliminar el usuario antiguo y crear uno nuevo
                userDao.deleteUser(originalEmail)
                val newUserId = userDao.insertUser(user)

                if (newUserId > 0) {
                    if (password.isNotEmpty()) {
                        // Si se proporcionó una contraseña nueva, actualizarla
                        userDao.updatePassword(email, password)
                    }
                    Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Si no cambió el email, actualizar los datos existentes
                val rowsAffected = userDao.updateUserComplete(email, nombre, role, currentProfileImagePath)

                if (rowsAffected > 0) {
                    if (password.isNotEmpty()) {
                        // Si se proporcionó una contraseña nueva, actualizarla
                        userDao.updatePassword(email, password)
                    }
                    Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar el usuario", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Crear nuevo usuario
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

        if (etNombre.text.toString().trim().isEmpty()) {
            etNombre.error = "El nombre es obligatorio"
            isValid = false
        }

        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = "El correo electrónico es obligatorio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Ingrese un correo electrónico válido"
            isValid = false
        }

        val password = etPassword.text.toString().trim()
        if (!isEditMode && password.isEmpty()) {
            etPassword.error = "La contraseña es obligatoria para nuevos usuarios"
            isValid = false
        } else if (password.isNotEmpty() && password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        return isValid
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

    override fun onDestroy() {
        super.onDestroy()
        // No eliminamos la imagen temporal porque podría estar siendo usada
    }
}