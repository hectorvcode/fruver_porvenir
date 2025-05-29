package com.example.online_store.model

data class Product(
    val id: Int = 0,                 // ID único del producto
    val name: String,                // Nombre del producto
    val price: Double,               // Precio por libra
    val category: String,            // Categoría (Frutas, Verduras, Bebidas, etc.)
    val imageResource: Int? = null,  // Recurso de imagen predeterminada (opcional)
    val imagePath: String? = null,   // Ruta de imagen personalizada tomada con cámara
    val description: String = "",    // Descripción del producto (opcional)
    var isFavorite: Boolean = false  // Estado de favorito (no se guarda en BD de productos)
) {
    /**
     * Función para determinar qué imagen usar
     * Prioriza imagePath sobre imageResource
     */
    fun getImageToUse(): Any? {
        return imagePath ?: imageResource
    }

    /**
     * Indica si el producto tiene una imagen personalizada (tomada con cámara)
     */
    fun hasCustomImage(): Boolean {
        return !imagePath.isNullOrEmpty()
    }
}