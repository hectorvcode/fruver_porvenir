package com.example.online_store.Activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product
import com.example.online_store.utils.RoleHelper
import com.google.android.material.textfield.TextInputEditText

class ProductFormActivity : AppCompatActivity() {

    private lateinit var productDao: ProductDao

    private lateinit var ivProductImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPrecio: TextInputEditText
    private lateinit var rgCategoria: RadioGroup
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    private var productId = 0 // 0 indica nuevo producto, otro valor indica edición
    private var isEditMode = false
    private var selectedImageResource: Int? = null

    // Lista de imágenes disponibles, asociadas con sus IDs de recurso
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
        ivProductImage = findViewById(R.id.iv_product_image)
        btnSelectImage = findViewById(R.id.btn_select_image)
        etNombre = findViewById(R.id.et_nombre)
        etPrecio = findViewById(R.id.et_precio)
        rgCategoria = findViewById(R.id.rg_categoria)
        etDescripcion = findViewById(R.id.et_descripcion)
        btnGuardar = findViewById(R.id.btn_guardar)
        btnCancelar = findViewById(R.id.btn_cancelar)

        // Verificar si estamos en modo edición
        if (intent.hasExtra("PRODUCT_ID")) {
            productId = intent.getIntExtra("PRODUCT_ID", 0)
            isEditMode = true
            supportActionBar?.title = "Editar Producto"
            loadProductData(productId)
        } else {
            supportActionBar?.title = "Nuevo Producto"
        }

        // Configurar listeners
        setupListeners()
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

    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            showImageSelectionDialog()
        }

        btnGuardar.setOnClickListener {
            saveProduct()
        }

        btnCancelar.setOnClickListener {
            finish() // Volver a la actividad anterior
        }
    }

    private fun showImageSelectionDialog() {
        // Obtener los nombres de las imágenes disponibles
        val imageNames = availableImages.keys.toTypedArray()

        // Crear y mostrar el diálogo de selección
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Imagen")
            .setItems(imageNames) { _, which ->
                // Obtener el nombre de la imagen seleccionada
                val selectedImageName = imageNames[which]

                // Obtener el ID del recurso asociado
                selectedImageResource = availableImages[selectedImageName]

                // Actualizar la ImageView con la imagen seleccionada
                selectedImageResource?.let {
                    ivProductImage.setImageResource(it)
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

            // Establecer la imagen si existe
            it.imageResource?.let { imageResource ->
                ivProductImage.setImageResource(imageResource)
                selectedImageResource = imageResource
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
}