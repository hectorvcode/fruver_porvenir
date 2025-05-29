package com.example.online_store.Activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.online_store.R
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product
import com.example.online_store.utils.ImageUtils
import com.example.online_store.utils.RoleHelper
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class ProductFormActivity : AppCompatActivity() {

    private lateinit var productDao: ProductDao

    private lateinit var ivProductImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPrecio: TextInputEditText
    private lateinit var rgCategoria: RadioGroup
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private var productId = 0 // 0 indica nuevo producto, otro valor indica edición
    private var isEditMode = false
    private var selectedImageResource: Int? = null
    private var currentImagePath: String? = null

    // Activity Result Launchers
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    // Lista de imágenes disponibles predeterminadas
    private val availableImages = mapOf(
        "Banano" to R.drawable.banano,
        "Naranja" to R.drawable.naranja,
        "Piña" to R.drawable.pina,
        "Limón" to R.drawable.limon,
        "Tomate" to R.drawable.ic_search, // Reemplazar con recurso real cuando exista
        "Cebolla" to R.drawable.ic_search, // Reemplazar con recurso real cuando exista
        "Agua" to R.drawable.ic_search,    // Reemplazar con recurso real cuando exista
        "Jugo" to R.drawable.ic_search     // Reemplazar con recurso real cuando exista
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_form)

        // Verificar permisos de administrador
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Configurar la ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializar el DAO
        productDao = ProductDao(this)

        // Inicializar vistas
        initializeViews()

        // Configurar Activity Result Launchers
        setupActivityResultLaunchers()

        // Verificar si estamos en modo edición
        checkEditMode()

        // Configurar listeners
        setupListeners()
    }

    private fun initializeViews() {
        ivProductImage = findViewById(R.id.iv_product_image)
        btnSelectImage = findViewById(R.id.btn_select_image)
        btnTakePhoto = findViewById(R.id.btn_take_photo)
        etNombre = findViewById(R.id.et_nombre)
        etPrecio = findViewById(R.id.et_precio)
        rgCategoria = findViewById(R.id.rg_categoria)
        etDescripcion = findViewById(R.id.et_descripcion)
        btnGuardar = findViewById(R.id.btn_guardar)
        btnCancelar = findViewById(R.id.btn_cancelar)
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

    private fun checkEditMode() {
        if (intent.hasExtra("PRODUCT_ID")) {
            productId = intent.getIntExtra("PRODUCT_ID", 0)
            isEditMode = true
            supportActionBar?.title = "Editar Producto"
            loadProductData(productId)
        } else {
            supportActionBar?.title = "Nuevo Producto"
        }
    }

    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            showImageSelectionDialog()
        }

        btnTakePhoto.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        btnGuardar.setOnClickListener {
            saveProduct()
        }

        btnCancelar.setOnClickListener {
            finish() // Volver a la actividad anterior
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
            // Crear archivo temporal para la foto
            val photoFile = ImageUtils.createImageFile(this)
            currentImagePath = photoFile.absolutePath

            // Crear URI usando FileProvider
            val photoURI = ImageUtils.getUriForFile(this, photoFile)

            // Intent para abrir la cámara
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            // Verificar que hay una app de cámara disponible
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
        currentImagePath?.let { imagePath ->
            try {
                // Comprimir y rotar la imagen
                val bitmap = ImageUtils.compressAndRotateImage(imagePath)

                if (bitmap != null) {
                    // Guardar la imagen comprimida
                    val compressedFile = File(imagePath)
                    if (ImageUtils.saveBitmapToFile(bitmap, compressedFile)) {
                        // Mostrar la imagen en el ImageView
                        ivProductImage.setImageBitmap(bitmap)

                        // Limpiar imagen predeterminada si había una seleccionada
                        selectedImageResource = null

                        Toast.makeText(this, "Foto capturada exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
                        currentImagePath = null
                    }
                } else {
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                    currentImagePath = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                currentImagePath = null
            }
        }
    }

    private fun showImageSelectionDialog() {
        // Obtener los nombres de las imágenes disponibles
        val imageNames = availableImages.keys.toTypedArray()

        // Crear y mostrar el diálogo de selección
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Imagen Predeterminada")
            .setItems(imageNames) { _, which ->
                // Obtener el nombre de la imagen seleccionada
                val selectedImageName = imageNames[which]

                // Obtener el ID del recurso asociado
                selectedImageResource = availableImages[selectedImageName]

                // Actualizar la ImageView con la imagen seleccionada
                selectedImageResource?.let {
                    ivProductImage.setImageResource(it)
                    // Limpiar imagen personalizada
                    currentImagePath = null
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadProductData(productId: Int) {
        // Cargar los datos del producto para edición
        val product = productDao.getProductById(productId)

        product?.let {
            // Llenar el formulario con los datos del producto
            etNombre.setText(it.name)
            etPrecio.setText(it.price.toString())
            etDescripcion.setText(it.description)

            // Establecer la imagen
            when {
                // Priorizar imagen personalizada
                !it.imagePath.isNullOrEmpty() && ImageUtils.imageFileExists(it.imagePath) -> {
                    currentImagePath = it.imagePath
                    val bitmap = ImageUtils.compressAndRotateImage(it.imagePath!!)
                    bitmap?.let { bmp ->
                        ivProductImage.setImageBitmap(bmp)
                    }
                }
                // Imagen predeterminada como respaldo
                it.imageResource != null -> {
                    selectedImageResource = it.imageResource
                    ivProductImage.setImageResource(it.imageResource)
                }
            }

            // Seleccionar la categoría
            when (it.category) {
                "Frutas" -> findViewById<RadioButton>(R.id.rb_frutas).isChecked = true
                "Verduras" -> findViewById<RadioButton>(R.id.rb_verduras).isChecked = true
                "Bebidas" -> findViewById<RadioButton>(R.id.rb_bebidas).isChecked = true
            }
        } ?: run {
            Toast.makeText(this, "No se encontró el producto", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveProduct() {
        // Validar el formulario
        if (!validateForm()) {
            return
        }

        // Obtener los valores del formulario
        val nombre = etNombre.text.toString().trim()
        val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

        // Obtener la categoría seleccionada
        val categoriaId = rgCategoria.checkedRadioButtonId
        val rbCategoria = findViewById<RadioButton>(categoriaId)
        val categoria = rbCategoria.text.toString()

        val descripcion = etDescripcion.text.toString().trim()

        // Crear el objeto producto
        val product = Product(
            id = if (isEditMode) productId else 0,
            name = nombre,
            price = precio,
            category = categoria,
            imageResource = selectedImageResource,
            imagePath = currentImagePath, // Nueva propiedad
            description = descripcion
        )

        // Guardar en la base de datos
        if (isEditMode) {
            // Actualizar
            val rowsAffected = productDao.updateProduct(product)

            if (rowsAffected > 0) {
                Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Insertar nuevo
            val id = productDao.insertProduct(product)

            if (id > 0) {
                Toast.makeText(this, "Producto agregado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
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

        // Validar precio
        val precioStr = etPrecio.text.toString().trim()
        if (precioStr.isEmpty()) {
            etPrecio.error = "El precio es obligatorio"
            isValid = false
        } else {
            val precio = precioStr.toDoubleOrNull()
            if (precio == null || precio <= 0) {
                etPrecio.error = "Ingrese un precio válido"
                isValid = false
            }
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
        // Si se cancela la actividad y hay una imagen temporal, eliminarla
        if (!isEditMode && currentImagePath != null) {
            // Solo eliminar si es una imagen nueva y no se guardó
            // ImageUtils.deleteImageFile(currentImagePath)
        }
    }
}