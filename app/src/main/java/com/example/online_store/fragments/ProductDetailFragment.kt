package com.example.online_store.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.online_store.R
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product
import com.example.online_store.utils.CartManager
import com.example.online_store.utils.ImageUtils
import java.text.NumberFormat
import java.util.Locale
import android.widget.ImageView

class ProductDetailFragment : Fragment() {

    private lateinit var tvProductTitle: TextView
    private lateinit var ivProductImage: ImageView
    private lateinit var tvProductPrice: TextView
    private lateinit var tvProductCategory: TextView
    private lateinit var tvProductDescription: TextView
    private lateinit var cvDecrease: CardView
    private lateinit var cvIncrease: CardView
    private lateinit var btnDecrease: TextView
    private lateinit var btnIncrease: TextView
    private lateinit var etQuantity: EditText
    private lateinit var btnAddToCart: Button
    private lateinit var cvProductDescription: CardView

    private lateinit var productDao: ProductDao
    private lateinit var cartManager: CartManager
    private var productId: Int = 0
    private var quantity: Int = 1
    private var product: Product? = null

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: Int): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            val args = Bundle()
            args.putInt(ARG_PRODUCT_ID, productId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            productId = it.getInt(ARG_PRODUCT_ID, 0)
        }

        // Inicializar DAO y CartManager
        productDao = ProductDao(requireContext())
        cartManager = CartManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        initializeViews(view)

        // Configurar eventos
        setupListeners()

        // Cargar datos del producto
        loadProductData()
    }

    private fun initializeViews(view: View) {
        tvProductTitle = view.findViewById(R.id.tv_product_title)
        ivProductImage = view.findViewById(R.id.iv_product_image)
        tvProductPrice = view.findViewById(R.id.tv_product_price)
        tvProductCategory = view.findViewById(R.id.tv_product_category)
        tvProductDescription = view.findViewById(R.id.tv_product_description)
        cvDecrease = view.findViewById(R.id.cv_decrease)
        cvIncrease = view.findViewById(R.id.cv_increase)
        btnDecrease = view.findViewById(R.id.btn_decrease)
        btnIncrease = view.findViewById(R.id.btn_increase)
        etQuantity = view.findViewById(R.id.et_quantity)
        btnAddToCart = view.findViewById(R.id.btn_add_to_cart)
        cvProductDescription = view.findViewById(R.id.cv_product_description)
    }

    private fun setupListeners() {
        // Control de cantidad
        cvDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                etQuantity.setText(quantity.toString())
            }
        }

        cvIncrease.setOnClickListener {
            quantity++
            etQuantity.setText(quantity.toString())
        }

        // EditText de cantidad
        etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    try {
                        val newQuantity = s.toString().toInt()
                        if (newQuantity > 0) {
                            quantity = newQuantity
                        } else {
                            // Si es 0 o negativo, establecer a 1
                            etQuantity.setText("1")
                            quantity = 1
                        }
                    } catch (e: NumberFormatException) {
                        // Si no es un número válido, establecer a 1
                        etQuantity.setText("1")
                        quantity = 1
                    }
                } else {
                    // Si está vacío, establecer a 1
                    etQuantity.setText("1")
                    quantity = 1
                }
            }
        })

        // Botón de agregar al carrito
        btnAddToCart.setOnClickListener {
            addToCart()
        }
    }

    private fun loadProductData() {
        // Obtener el producto de la base de datos
        product = productDao.getProductById(productId)

        // Mostrar los datos del producto
        product?.let {
            tvProductTitle.text = it.name
            // Usar la unidad dinámica del producto
            tvProductPrice.text = "${currencyFormat.format(it.price)}/${it.unit}"
            tvProductCategory.text = "Categoría: ${it.category}"

            // Mostrar descripción si existe
            if (it.description.isNotEmpty()) {
                tvProductDescription.text = it.description
                cvProductDescription.visibility = View.VISIBLE
            } else {
                cvProductDescription.visibility = View.GONE
            }

            // Mostrar imagen del producto
            loadProductImage(it)
        } ?: run {
            // Si el producto no existe, mostrar un mensaje y cerrar el fragmento
            Toast.makeText(
                requireContext(),
                "Producto no encontrado",
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    /**
     * Carga la imagen del producto, priorizando imágenes personalizadas
     */
    private fun loadProductImage(product: Product) {
        when {
            // Priorizar imagen personalizada (tomada con cámara)
            !product.imagePath.isNullOrEmpty() && ImageUtils.imageFileExists(product.imagePath) -> {
                try {
                    val bitmap = BitmapFactory.decodeFile(product.imagePath)
                    if (bitmap != null) {
                        ivProductImage.setImageBitmap(bitmap)
                    } else {
                        // Si falla la carga del archivo, usar imagen predeterminada como respaldo
                        loadFallbackImage(product)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadFallbackImage(product)
                }
            }
            // Imagen predeterminada (de recursos)
            product.imageResource != null -> {
                ivProductImage.setImageResource(product.imageResource)
            }
            // Imagen por defecto si no hay ninguna
            else -> {
                ivProductImage.setImageResource(R.drawable.ic_search)
            }
        }
    }

    /**
     * Carga imagen de respaldo en caso de error
     */
    private fun loadFallbackImage(product: Product) {
        if (product.imageResource != null) {
            ivProductImage.setImageResource(product.imageResource)
        } else {
            ivProductImage.setImageResource(R.drawable.ic_search)
        }
    }

    private fun addToCart() {
        product?.let {
            cartManager.addToCart(it, quantity)

            // Mensaje personalizado según la unidad
            val unitDisplay = when (it.unit) {
                "unidad" -> if (quantity == 1) "unidad" else "unidades"
                else -> "${it.unit}${if (quantity > 1) "s" else ""}"
            }

            Toast.makeText(
                requireContext(),
                "${quantity} ${unitDisplay} de ${it.name} añadido al carrito",
                Toast.LENGTH_SHORT
            ).show()
            // Volver a la lista de productos
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}