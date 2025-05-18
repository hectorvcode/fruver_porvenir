package com.example.online_store.model

data class Product(
    val id: Int = 0,                 // ID único del producto
    val name: String,                // Nombre del producto
    val price: Double,               // Precio por libra
    val category: String,            // Categoría (Frutas, Verduras, Bebidas, etc.)
    val imageResource: Int? = null,  // Recurso de imagen (opcional)
    val description: String = ""     // Descripción del producto (opcional)
)