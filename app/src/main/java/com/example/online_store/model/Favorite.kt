package com.example.online_store.model

/**
 * Modelo para representar un producto favorito
 */
data class Favorite(
    val id: Int = 0,              // ID único del favorito
    val userId: String,           // Email del usuario (identificador único)
    val productId: Int,           // ID del producto favorito
    val dateAdded: Long = System.currentTimeMillis() // Fecha cuando se agregó a favoritos
)