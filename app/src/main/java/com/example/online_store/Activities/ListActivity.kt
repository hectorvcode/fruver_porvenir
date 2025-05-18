package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.online_store.Activities.ProductAdminActivity

class ListActivity : AppCompatActivity() {

    private lateinit var productDao: ProductDao
    private lateinit var btnFrutas: Button
    private lateinit var btnVerduras: Button
    private lateinit var btnBebidas: Button

    // Referencias a los TextView para mostrar productos
    private lateinit var tvBanano: TextView
    private lateinit var tvPrecioBanano: TextView
    private lateinit var tvNaranja: TextView
    private lateinit var tvPrecioNaranja: TextView
    private lateinit var tvPina: TextView
    private lateinit var tvPrecioPina: TextView
    private lateinit var tvLimon: TextView
    private lateinit var tvPrecioLimon: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listado)

        // Inicializar el DAO
        productDao = ProductDao(this)

        // Inicializar botones
        btnFrutas = findViewById(R.id.btn_frutas)
        btnVerduras = findViewById(R.id.btn_verduras)
        btnBebidas = findViewById(R.id.btn_bebidas)

        // Inicializar TextViews
        tvBanano = findViewById(R.id.tv_banano)
        tvPrecioBanano = findViewById(R.id.tv_precio_banano)
        tvNaranja = findViewById(R.id.tv_naranja)
        tvPrecioNaranja = findViewById(R.id.tv_precio_naranja)
        tvPina = findViewById(R.id.tv_pina)
        tvPrecioPina = findViewById(R.id.tv_precio_pina)
        tvLimon = findViewById(R.id.tv_limon)
        tvPrecioLimon = findViewById(R.id.tv_precio_limon)

        // Configurar listeners para los botones de categoría
        setupCategoryButtons()

        // Inicializar la base de datos con algunos productos si está vacía
        checkAndInitializeDatabase()

        // Cargar productos de la categoría por defecto (Frutas)
        loadProductsByCategory("Frutas")

        val fabAdminProducts = findViewById<FloatingActionButton>(R.id.fab_admin_products)
        fabAdminProducts.setOnClickListener {
            val intent = Intent(this, ProductAdminActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCategoryButtons() {
        btnFrutas.setOnClickListener {
            btnFrutas.setBackgroundResource(R.color.fae61e)
            btnVerduras.setBackgroundResource(R.color.gray)
            btnBebidas.setBackgroundResource(R.color.gray)
            loadProductsByCategory("Frutas")
        }

        btnVerduras.setOnClickListener {
            btnFrutas.setBackgroundResource(R.color.gray)
            btnVerduras.setBackgroundResource(R.color.fae61e)
            btnBebidas.setBackgroundResource(R.color.gray)
            loadProductsByCategory("Verduras")
        }

        btnBebidas.setOnClickListener {
            btnFrutas.setBackgroundResource(R.color.gray)
            btnVerduras.setBackgroundResource(R.color.gray)
            btnBebidas.setBackgroundResource(R.color.fae61e)
            loadProductsByCategory("Bebidas")
        }
    }

    private fun checkAndInitializeDatabase() {
        // Verificar si ya hay productos en la base de datos
        val products = productDao.getAllProducts()

        if (products.isEmpty()) {
            // Si no hay productos, inicializar con datos predeterminados
            initializeDefaultProducts()
        }
    }

    private fun initializeDefaultProducts() {
        // Crear productos predeterminados
        val defaultProducts = listOf(
            // Frutas
            Product(
                name = "Banano",
                price = 1500.0,
                category = "Frutas",
                imageResource = R.drawable.banano,
                description = "Banano fresco por libra"
            ),
            Product(
                name = "Naranja",
                price = 2500.0,
                category = "Frutas",
                imageResource = R.drawable.naranja,
                description = "Naranja dulce por libra"
            )
        )

        // Insertar los productos en la base de datos
        for (product in defaultProducts) {
            productDao.insertProduct(product)
        }

        Toast.makeText(this, "Base de datos inicializada con productos", Toast.LENGTH_SHORT).show()
    }

    private fun loadProductsByCategory(category: String) {
        // Obtener productos de la categoría seleccionada
        val products = productDao.getProductsByCategory(category)

        // Si no hay productos en esta categoría, mostrar mensaje
        if (products.isEmpty()) {
            Toast.makeText(this, "No hay productos en la categoría $category", Toast.LENGTH_SHORT).show()
            return
        }

        // Para este ejemplo simple, solo actualizamos los TextViews existentes
        // En una aplicación real, usarías un RecyclerView

        // Para la categoría Frutas, actualizamos los TextViews existentes
        if (category == "Frutas") {
            // Buscar los productos específicos por nombre
            val banano = products.find { it.name == "Banano" }
            val naranja = products.find { it.name == "Naranja" }
            val pina = products.find { it.name == "Piña" }
            val limon = products.find { it.name == "Limón" }

            // Actualizar los TextViews con la información de la base de datos
            banano?.let {
                tvBanano.text = it.name
                tvPrecioBanano.text = "$${it.price}/lb"
            }

            naranja?.let {
                tvNaranja.text = it.name
                tvPrecioNaranja.text = "$${it.price}/lb"
            }

            pina?.let {
                tvPina.text = it.name
                tvPrecioPina.text = "$${it.price}/lb"
            }

            limon?.let {
                tvLimon.text = it.name
                tvPrecioLimon.text = "$${it.price}/lb"
            }
        } else {
            // Para otras categorías, mostrar un mensaje indicando que necesitas implementar vistas para ellas
            Toast.makeText(
                this,
                "Hay ${products.size} productos en la categoría $category. Implementa la vista para mostrarlos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Métodos de ejemplo para operaciones CRUD que podrías utilizar en tu aplicación

    /**
     * Ejemplo de cómo añadir un nuevo producto
     */
    private fun addProduct(name: String, price: Double, category: String, imageResource: Int? = null) {
        val newProduct = Product(
            name = name,
            price = price,
            category = category,
            imageResource = imageResource
        )

        val id = productDao.insertProduct(newProduct)

        if (id > 0) {
            Toast.makeText(this, "Producto agregado correctamente", Toast.LENGTH_SHORT).show()
            // Recargar los productos de la categoría actual
            loadProductsByCategory(category)
        } else {
            Toast.makeText(this, "Error al agregar el producto", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Ejemplo de cómo actualizar un producto existente
     */
    private fun updateProduct(id: Int, name: String, price: Double, category: String, imageResource: Int? = null) {
        val product = Product(
            id = id,
            name = name,
            price = price,
            category = category,
            imageResource = imageResource
        )

        val rowsAffected = productDao.updateProduct(product)

        if (rowsAffected > 0) {
            Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show()
            // Recargar los productos de la categoría actual
            loadProductsByCategory(category)
        } else {
            Toast.makeText(this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Ejemplo de cómo eliminar un producto
     */
    private fun deleteProduct(id: Int, category: String) {
        val rowsAffected = productDao.deleteProduct(id)

        if (rowsAffected > 0) {
            Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show()
            // Recargar los productos de la categoría actual
            loadProductsByCategory(category)
        } else {
            Toast.makeText(this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show()
        }
    }
}