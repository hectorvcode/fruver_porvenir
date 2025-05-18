package com.example.online_store.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.Product

class ProductAdapter(
    private var products: List<Product>,
    private val onEditClickListener: (Product) -> Unit,
    private val onDeleteClickListener: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

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
        holder.tvProductPrice.text = "$${product.price}/lb"
        holder.tvProductCategory.text = "Categoría: ${product.category}"

        // Configurar la imagen del producto si existe
        product.imageResource?.let {
            holder.ivProduct.setImageResource(it)
        } ?: run {
            // Imagen por defecto si no hay recurso
            holder.ivProduct.setImageResource(R.drawable.ic_search) // Cambiar por una imagen por defecto
        }

        // Configurar listeners para los botones de editar y eliminar
        holder.ivEdit.setOnClickListener {
            onEditClickListener(product)
        }

        holder.ivDelete.setOnClickListener {
            onDeleteClickListener(product)
        }
    }

    override fun getItemCount(): Int = products.size

    // Método para actualizar la lista de productos
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}