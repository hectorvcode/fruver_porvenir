package com.example.online_store.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.Product
import com.example.online_store.utils.CartManager
import com.example.online_store.utils.ImageUtils
import java.text.NumberFormat
import java.util.Locale

class FavoriteProductAdapter(
    private var products: List<Product>,
    private val onProductClickListener: (Product) -> Unit,
    private val onFavoriteClickListener: (Product) -> Unit,
    private val cartManager: CartManager
) : RecyclerView.Adapter<FavoriteProductAdapter.FavoriteViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvProductCategory: TextView = itemView.findViewById(R.id.tv_product_category)
        val ibFavorite: ImageButton = itemView.findViewById(R.id.ib_favorite)
        val ibAddToCart: ImageButton = itemView.findViewById(R.id.ib_add_to_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_product, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val product = products[position]

        // Configurar datos del producto
        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = currencyFormat.format(product.price)
        holder.tvProductCategory.text = product.category

        // Configurar imagen del producto
        loadProductImage(product, holder.ivProductImage)

        // Configurar estado del botón de favorito
        holder.ibFavorite.setImageResource(
            if (product.isFavorite) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )

        // Configurar color del corazón
        holder.ibFavorite.setColorFilter(
            if (product.isFavorite) holder.itemView.context.getColor(R.color.red_heart)
            else holder.itemView.context.getColor(android.R.color.darker_gray)
        )

        // Listeners
        holder.itemView.setOnClickListener {
            onProductClickListener(product)
        }

        holder.ibFavorite.setOnClickListener {
            onFavoriteClickListener(product)
        }

        holder.ibAddToCart.setOnClickListener {
            cartManager.addToCart(product, 1)
            Toast.makeText(
                holder.itemView.context,
                "${product.name} añadido al carrito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadProductImage(product: Product, imageView: ImageView) {
        when {
            // Priorizar imagen personalizada
            !product.imagePath.isNullOrEmpty() && ImageUtils.imageFileExists(product.imagePath) -> {
                try {
                    val bitmap = BitmapFactory.decodeFile(product.imagePath)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        loadFallbackImage(product, imageView)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadFallbackImage(product, imageView)
                }
            }
            // Imagen predeterminada
            product.imageResource != null -> {
                imageView.setImageResource(product.imageResource)
            }
            // Imagen por defecto
            else -> {
                imageView.setImageResource(R.drawable.ic_search)
            }
        }
    }

    private fun loadFallbackImage(product: Product, imageView: ImageView) {
        if (product.imageResource != null) {
            imageView.setImageResource(product.imageResource)
        } else {
            imageView.setImageResource(R.drawable.ic_search)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}