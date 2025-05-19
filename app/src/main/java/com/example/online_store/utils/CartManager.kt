package com.example.online_store.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.online_store.data.ProductDao
import com.example.online_store.model.CartItem
import com.example.online_store.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Gestor del carrito de compras que utiliza SharedPreferences para persistencia
 */
class CartManager(context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()
    private val gson = Gson()
    private val productDao = ProductDao(context)

    companion object {
        private const val PREF_NAME = "CartPreferences"
        private const val KEY_CART_ITEMS = "cart_items"
        // Eliminamos la constante SHIPPING_COST
    }

    /**
     * Añade un producto al carrito o incrementa su cantidad si ya existe
     */
    fun addToCart(product: Product, quantity: Int = 1) {
        val cartItems = getCartItems().toMutableList()

        // Buscar si el producto ya está en el carrito
        val existingItem = cartItems.find { it.product.id == product.id }

        if (existingItem != null) {
            // Incrementar la cantidad si ya existe
            existingItem.quantity += quantity
        } else {
            // Añadir nuevo ítem al carrito
            cartItems.add(CartItem(product, quantity))
        }

        // Guardar cambios
        saveCartItems(cartItems)
    }

    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        val cartItems = getCartItems().toMutableList()
        val item = cartItems.find { it.product.id == productId }

        item?.let {
            it.quantity = quantity
            saveCartItems(cartItems)
        }
    }

    /**
     * Elimina un producto del carrito
     */
    fun removeFromCart(productId: Int) {
        val cartItems = getCartItems().toMutableList()
        cartItems.removeIf { it.product.id == productId }
        saveCartItems(cartItems)
    }

    /**
     * Obtiene todos los ítems del carrito
     */
    fun getCartItems(): List<CartItem> {
        val json = pref.getString(KEY_CART_ITEMS, null) ?: return emptyList()

        try {
            // Definir el tipo para deserializar
            val type: Type = object : TypeToken<List<CartItemDto>>() {}.type

            // Deserializar a DTO primero
            val cartItemDtos: List<CartItemDto> = gson.fromJson(json, type)

            // Convertir DTOs a CartItems completos
            return cartItemDtos.mapNotNull { dto ->
                val product = productDao.getProductById(dto.productId)
                product?.let { CartItem(it, dto.quantity) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Calcula el total del carrito (suma de subtotales de todos los ítems)
     */
    fun getTotal(): Double {
        return getCartItems().sumOf { it.getSubtotal() }
    }

    /**
     * Vacía el carrito
     */
    fun clearCart() {
        editor.remove(KEY_CART_ITEMS).apply()
    }

    /**
     * Guarda los ítems del carrito en SharedPreferences
     */
    private fun saveCartItems(cartItems: List<CartItem>) {
        // Convertir a DTO para serializacion
        val cartItemDtos = cartItems.map {
            CartItemDto(it.product.id, it.quantity)
        }

        val json = gson.toJson(cartItemDtos)
        editor.putString(KEY_CART_ITEMS, json).apply()
    }

    /**
     * Clase DTO para serialización (evita guardar el objeto Product completo)
     */
    private data class CartItemDto(
        val productId: Int,
        val quantity: Int
    )
}