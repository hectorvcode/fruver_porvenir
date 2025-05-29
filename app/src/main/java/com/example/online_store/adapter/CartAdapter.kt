package com.example.online_store.adapter

import android.graphics.BitmapFactory
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
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.CartItem
import com.example.online_store.utils.ImageUtils
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChangedListener: (CartItem) -> Unit,
    private val onItemRemovedListener: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val btnDecrease: Button = itemView.findViewById(R.id.btn_decrease)
        val etQuantity: EditText = itemView.findViewById(R.id.et_quantity)
        val btnIncrease: Button = itemView.findViewById(R.id.btn_increase)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tv_subtotal)
        val ibDeleteItem: ImageButton = itemView.findViewById(R.id.ib_delete_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val product = cartItem.product

        // Configurar vistas con datos del producto
        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = "${currencyFormat.format(product.price)}/lb"
        holder.etQuantity.setText(cartItem.quantity.toString())
        holder.tvSubtotal.text = currencyFormat.format(cartItem.getSubtotal())

        // Establecer imagen del producto (aquí está el cambio importante)
        loadProductImage(product, holder.ivProductImage)

        // Configurar listeners para controles de cantidad
        holder.btnDecrease.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                holder.etQuantity.setText(cartItem.quantity.toString())
                updateSubtotal(holder, cartItem)
                onQuantityChangedListener(cartItem)
            }
        }

        holder.btnIncrease.setOnClickListener {
            cartItem.quantity++
            holder.etQuantity.setText(cartItem.quantity.toString())
            updateSubtotal(holder, cartItem)
            onQuantityChangedListener(cartItem)
        }

        // Listener para cambios en el campo de texto de cantidad
        holder.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    try {
                        val newQuantity = s.toString().toInt()
                        if (newQuantity > 0 && newQuantity != cartItem.quantity) {
                            cartItem.quantity = newQuantity
                            updateSubtotal(holder, cartItem)
                            onQuantityChangedListener(cartItem)
                        }
                    } catch (e: NumberFormatException) {
                        // Si no es un número válido, reestablecer
                        holder.etQuantity.setText(cartItem.quantity.toString())
                    }
                } else {
                    // Si está vacío, establecer a 1
                    cartItem.quantity = 1
                    holder.etQuantity.setText("1")
                    updateSubtotal(holder, cartItem)
                    onQuantityChangedListener(cartItem)
                }
            }
        })

        // Listener para eliminar ítem
        holder.ibDeleteItem.setOnClickListener {
            onItemRemovedListener(cartItem)
        }
    }

    /**
     * Carga la imagen del producto, priorizando imágenes personalizadas
     */
    private fun loadProductImage(product: com.example.online_store.model.Product, imageView: ImageView) {
        when {
            // Priorizar imagen personalizada (tomada con cámara)
            !product.imagePath.isNullOrEmpty() && ImageUtils.imageFileExists(product.imagePath) -> {
                try {
                    val bitmap = BitmapFactory.decodeFile(product.imagePath)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        // Si falla la carga del archivo, usar imagen predeterminada como respaldo
                        loadFallbackImage(product, imageView)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadFallbackImage(product, imageView)
                }
            }
            // Imagen predeterminada (de recursos)
            product.imageResource != null -> {
                imageView.setImageResource(product.imageResource)
            }
            // Imagen por defecto si no hay ninguna
            else -> {
                imageView.setImageResource(R.drawable.ic_search)
            }
        }
    }

    /**
     * Carga imagen de respaldo en caso de error
     */
    private fun loadFallbackImage(product: com.example.online_store.model.Product, imageView: ImageView) {
        if (product.imageResource != null) {
            imageView.setImageResource(product.imageResource)
        } else {
            imageView.setImageResource(R.drawable.ic_search)
        }
    }

    private fun updateSubtotal(holder: CartViewHolder, cartItem: CartItem) {
        holder.tvSubtotal.text = currencyFormat.format(cartItem.getSubtotal())
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newCartItems: List<CartItem>) {
        this.cartItems = newCartItems
        notifyDataSetChanged()
    }
}