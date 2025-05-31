package com.example.online_store.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.Product
import com.example.online_store.utils.ImageUtils
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product>,
    private val onEditClickListener: (Product) -> Unit,
    private val onDeleteClickListener: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.iv_product)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val tvProductCategory: TextView = itemView.findViewById(R.id.tv_product_category)
        val ivEdit: ImageView = itemView.findViewById(R.id.iv_edit)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Configurar los datos del producto en la vista
        holder.tvProductName.text = product.name
        // Usar la unidad dinámica del producto
        holder.tvProductPrice.text = "${currencyFormat.format(product.price)}/${product.unit}"
        holder.tvProductCategory.text = "Categoría: ${product.category}"

        // Configurar la imagen del producto
        loadProductImage(product, holder.ivProduct)

        // Configurar listeners para los botones de editar y eliminar
        holder.ivEdit.setOnClickListener {
            onEditClickListener(product)
        }

        holder.ivDelete.setOnClickListener {
            onDeleteClickListener(product)
        }
    }

    private fun loadProductImage(product: Product, imageView: ImageView) {
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

    private fun loadFallbackImage(product: Product, imageView: ImageView) {
        if (product.imageResource != null) {
            imageView.setImageResource(product.imageResource)
        } else {
            imageView.setImageResource(R.drawable.ic_search)
        }
    }

    override fun getItemCount(): Int = products.size

    // Método para actualizar la lista de productos
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}