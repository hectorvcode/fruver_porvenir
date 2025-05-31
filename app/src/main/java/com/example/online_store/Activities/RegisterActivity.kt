package com.example.online_store.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.online_store.R
import com.example.online_store.data.UserDao
import com.example.online_store.model.User
import com.example.online_store.utils.ImageUtils
import com.example.online_store.utils.SessionManager
import java.io.File

class RegisterActivity : AppCompatActivity() {

    // Vistas existentes
    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegistrarse: Button
    private lateinit var userDao: UserDao
    private lateinit var sessionManager: SessionManager

    // Nuevas vistas para imagen de perfil
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnSelectProfileImage: Button
    private lateinit var btnTakeProfilePhoto: Button

    private var currentProfileImagePath: String? = null

    // Activity Result Launchers
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar UserDao y SessionManager
        userDao = UserDao(this)
        sessionManager = SessionManager(this)

        // Inicializar vistas
        initializeViews()

        // Configurar Activity Result Launchers
        setupActivityResultLaunchers()

        // Configurar listeners
        setupListeners()
    }

    private fun initializeViews() {
        // Vistas existentes
        edtFullName = findViewById(R.id.edt_full_name)
        edtEmail = findViewById(R.id.edt_email_login)
        edtPassword = findViewById(R.id.edt_password_login)
        btnRegistrarse = findViewById(R.id.btn_registrarse)

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
        // Listener existente para el botón de registro
        btnRegistrarse.setOnClickListener {
            registerUser()
        }

        // Nuevos listeners para imagen de perfil
        btnSelectProfileImage.setOnClickListener {
            openGallery()
        }

        btnTakeProfilePhoto.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        // Listeners existentes para navegación con teclado
        edtFullName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtEmail.requestFocus()
                true
            } else {
                false
            }
        }

        edtEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtPassword.requestFocus()
                true
            } else {
                false
            }
        }

        edtPassword.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                registerUser()
                true
            } else {
                false
            }
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

    private fun registerUser() {
        // Obtener datos del formulario
        val fullName = edtFullName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        // Validación básica
        if (fullName.isEmpty()) {
            edtFullName.error = "Por favor ingrese su nombre completo"
            edtFullName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            edtEmail.error = "Por favor ingrese su email"
            edtEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.error = "Por favor ingrese un email válido"
            edtEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            edtPassword.error = "Por favor ingrese una contraseña"
            edtPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            edtPassword.error = "La contraseña debe tener al menos 6 caracteres"
            edtPassword.requestFocus()
            return
        }

        // Verificar si el email ya existe
        if (userDao.getUserByEmail(email) != null) {
            Toast.makeText(this, "El email ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear nuevo usuario (siempre con rol USER) incluyendo imagen de perfil y contraseña
        val user = User(
            email = email,
            name = fullName,
            role = User.ROLE_USER,
            password = password, // La contraseña se hasheará en UserDao.insertUser
            profilePicPath = currentProfileImagePath
        )

        // Insertar en la base de datos
        val result = userDao.insertUser(user)

        if (result > 0) {
            // Registro exitoso
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

            // Crear sesión
            sessionManager.createLoginSession(user)

            // Redireccionar a la pantalla principal
            startActivity(Intent(this, MainContainerActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Error al registrar. Intente nuevamente", Toast.LENGTH_SHORT).show()
        }
    }
}