package com.example.online_store.model

/**
 * Modelo para representar un ítem en el carrito de compras
 */
data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    /**
     * Calcula el subtotal del ítem (precio * cantidad)
     */
    fun getSubtotal(): Double {
        return product.price * quantity
    }
}