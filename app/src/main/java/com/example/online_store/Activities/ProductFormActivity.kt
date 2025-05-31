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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import com.example.online_store.data.UnitDao
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class ProductFormActivity : AppCompatActivity() {

    private lateinit var productDao: ProductDao

    private lateinit var ivProductImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPrecio: TextInputEditText
    private lateinit var actUnidad: AutoCompleteTextView // Nuevo campo
    private lateinit var rgCategoria: RadioGroup
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private var productId = 0 // 0 indica nuevo producto, otro valor indica edición
    private var isEditMode = false
    private var currentImagePath: String? = null

    // Activity Result Launchers
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

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

        // Configurar el selector de unidades
        setupUnitSelector()

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
        actUnidad = findViewById(R.id.act_unidad) // Nueva vista
        rgCategoria = findViewById(R.id.rg_categoria)
        etDescripcion = findViewById(R.id.et_descripcion)
        btnGuardar = findViewById(R.id.btn_guardar)
        btnCancelar = findViewById(R.id.btn_cancelar)
    }

    private fun setupUnitSelector() {
        // Obtener unidades activas de la base de datos
        val unitDao = UnitDao(this)
        val activeUnits = unitDao.getActiveUnitAbbreviations()

        // Si no hay unidades activas, usar las predeterminadas como respaldo
        val units = if (activeUnits.isNotEmpty()) {
            activeUnits
        } else {
            Product.AVAILABLE_UNITS
        }

        // Configurar el adaptador para el selector de unidades
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, units)
        actUnidad.setAdapter(adapter)

        // Establecer unidad por defecto
        if (units.contains("lb")) {
            actUnidad.setText("lb", false)
        } else if (units.isNotEmpty()) {
            actUnidad.setText(units[0], false)
        }

        // Cerrar el DAO
        unitDao.closeDatabase()
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
            openGallery()
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

    private fun openGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"

            // Verificar que hay una app de galería disponible
            if (galleryIntent.resolveActivity(packageManager) != null) {
                galleryLauncher.launch(galleryIntent)
            } else {
                // Intentar con otro método si el primero falla
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
            // Verificar que la URI es accesible
            if (!ImageUtils.isUriAccessible(this, imageUri)) {
                Toast.makeText(this, "No se puede acceder a la imagen seleccionada", Toast.LENGTH_SHORT).show()
                return
            }

            // Procesar la imagen desde URI
            val processedImagePath = ImageUtils.processImageFromUri(this, imageUri)

            if (processedImagePath != null) {
                currentImagePath = processedImagePath

                // Mostrar la imagen en el ImageView
                val bitmap = ImageUtils.compressAndRotateImage(processedImagePath)
                if (bitmap != null) {
                    ivProductImage.setImageBitmap(bitmap)

                    Toast.makeText(this, "Imagen seleccionada exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                    currentImagePath = null
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

    private fun loadProductData(productId: Int) {
        // Cargar los datos del producto para edición
        val product = productDao.getProductById(productId)

        product?.let {
            // Llenar el formulario con los datos del producto
            etNombre.setText(it.name)
            etPrecio.setText(it.price.toString())
            actUnidad.setText(it.unit, false) // Establecer la unidad
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
                    ivProductImage.setImageResource(it.imageResource)
                }
                // Imagen por defecto
                else -> {
                    ivProductImage.setImageResource(R.drawable.ic_search)
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
        val unidad = actUnidad.text.toString().trim().ifEmpty { "lb" } // Nueva línea

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
            unit = unidad, // Nueva propiedad
            imageResource = null, // Ya no usamos imágenes predeterminadas
            imagePath = currentImagePath, // Solo imágenes personalizadas
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

        // Validar unidad
        val unidad = actUnidad.text.toString().trim()
        if (unidad.isEmpty()) {
            actUnidad.error = "Seleccione una unidad de medida"
            isValid = false
        }

        return isValid
    }

    // ACTUALIZACIÓN PARCIAL PARA ProductFormActivity.kt
// Solo mostrar la parte que cambia en el método setupUnitSelector()



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
        // Si se cancela la actividad y hay una imagen temporal, no la eliminamos
        // porque podría estar siendo usada
    }
}