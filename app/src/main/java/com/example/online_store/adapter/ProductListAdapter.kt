package com.example.online_store.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.Product
import com.example.online_store.utils.CartManager

class ProductListAdapter(
    private var products: List<Product>,
    private val onProductClickListener: (Product) -> Unit,
    private val cartManager: CartManager
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    // Mapa para guardar las cantidades seleccionadas por producto
    private val quantityMap = mutableMapOf<Int, Int>()

    init {
        // Inicializar todas las cantidades a 1
        products.forEach { product ->
            quantityMap[product.id] = 1
        }
    }

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvProductCategory: TextView = itemView.findViewById(R.id.tv_product_category)
        val btnDecrease: Button = itemView.findViewById(R.id.btn_decrease)
        val etQuantity: EditText = itemView.findViewById(R.id.et_quantity)
        val btnIncrease: Button = itemView.findViewById(R.id.btn_increase)
        val ibAddToCart: ImageButton = itemView.findViewById(R.id.ib_add_to_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_list, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val quantity = quantityMap[product.id] ?: 1

        // Configurar los datos del producto en la vista
        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = "$${product.price}/lb"
        holder.tvProductCategory.text = "Categoría: ${product.category}"
        holder.etQuantity.setText(quantity.toString())

        // Configurar la imagen del producto si existe
        product.imageResource?.let {
            holder.ivProductImage.setImageResource(it)
        } ?: run {
            // Imagen por defecto si no hay recurso
            holder.ivProductImage.setImageResource(R.drawable.ic_search)
        }

        // Configurar listeners para el producto
        holder.itemView.setOnClickListener {
            onProductClickListener(product)
        }

        // Configurar listeners para los controles de cantidad
        holder.btnDecrease.setOnClickListener {
            updateQuantity(product.id, holder, -1)
        }

        holder.btnIncrease.setOnClickListener {
            updateQuantity(product.id, holder, 1)
        }

        // Listener para cambios manuales en el EditText
        holder.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    try {
                        val newQuantity = s.toString().toInt()
                        if (newQuantity > 0) {
                            quantityMap[product.id] = newQuantity
                        } else {
                            // Si es 0 o negativo, establecer a 1
                            holder.etQuantity.setText("1")
                            quantityMap[product.id] = 1
                        }
                    } catch (e: NumberFormatException) {
                        // Si no es un número válido, establecer a 1
                        holder.etQuantity.setText("1")
                        quantityMap[product.id] = 1
                    }
                } else {
                    // Si está vacío, establecer a 1
                    holder.etQuantity.setText("1")
                    quantityMap[product.id] = 1
                }
            }
        })

        // Listener para añadir al carrito
        holder.ibAddToCart.setOnClickListener {
            val quantity = quantityMap[product.id] ?: 1
            cartManager.addToCart(product, quantity)
            Toast.makeText(
                holder.itemView.context,
                "${quantity} ${product.name} añadido al carrito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateQuantity(productId: Int, holder: ProductViewHolder, change: Int) {
        val currentQuantity = quantityMap[productId] ?: 1
        val newQuantity = (currentQuantity + change).coerceAtLeast(1) // Mínimo 1

        quantityMap[productId] = newQuantity
        holder.etQuantity.setText(newQuantity.toString())
    }

    override fun getItemCount(): Int = products.size

    // Método para actualizar la lista de productos
    fun updateProducts(newProducts: List<Product>) {
        // Inicializar cantidades para nuevos productos
        newProducts.forEach { product ->
            if (!quantityMap.containsKey(product.id)) {
                quantityMap[product.id] = 1
            }
        }

        products = newProducts
        notifyDataSetChanged()
    }
}