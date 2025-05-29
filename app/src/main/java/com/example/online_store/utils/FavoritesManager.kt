package com.example.online_store.utils

import android.content.Context
import com.example.online_store.data.FavoriteDao
import com.example.online_store.model.Product

/**
 * Gestor para manejar los productos favoritos
 */
class FavoritesManager(context: Context) {

    private val favoriteDao = FavoriteDao(context)
    private val sessionManager = SessionManager(context)

    /**
     * Agrega un producto a favoritos
     */
    fun addToFavorites(product: Product): Boolean {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            val result = favoriteDao.addToFavorites(userId, product.id)
            if (result > 0) {
                product.isFavorite = true
            }
            return result > 0
        }
        return false
    }

    /**
     * Elimina un producto de favoritos
     */
    fun removeFromFavorites(product: Product): Boolean {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            val result = favoriteDao.removeFromFavorites(userId, product.id)
            if (result > 0) {
                product.isFavorite = false
            }
            return result > 0
        }
        return false
    }

    /**
     * Alterna el estado de favorito de un producto
     * @return true si se agregó a favoritos, false si se eliminó
     */
    fun toggleFavorite(product: Product): Boolean {
        return if (product.isFavorite) {
            removeFromFavorites(product)
            false
        } else {
            addToFavorites(product)
            true
        }
    }

    /**
     * Verifica si un producto está en favoritos
     */
    fun isFavorite(productId: Int): Boolean {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            favoriteDao.isFavorite(userId, productId)
        } else {
            false
        }
    }

    /**
     * Obtiene todos los productos favoritos del usuario actual
     */
    fun getFavoriteProducts(): List<Product> {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            favoriteDao.getFavoriteProducts(userId)
        } else {
            emptyList()
        }
    }

    /**
     * Obtiene el conteo de productos favoritos
     */
    fun getFavoritesCount(): Int {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            favoriteDao.getFavoritesCount(userId)
        } else {
            0
        }
    }

    /**
     * Elimina todos los favoritos del usuario actual
     */
    fun clearAllFavorites(): Boolean {
        val userId = getCurrentUserId()
        return if (userId.isNotEmpty()) {
            favoriteDao.clearUserFavorites(userId) > 0
        } else {
            false
        }
    }

    /**
     * Actualiza el estado de favorito para una lista de productos
     */
    fun updateFavoriteStatus(products: List<Product>) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            products.forEach { product ->
                product.isFavorite = favoriteDao.isFavorite(userId, product.id)
            }
        }
    }

    /**
     * Obtiene el ID del usuario actual (email)
     */
    private fun getCurrentUserId(): String {
        val userDetails = sessionManager.getUserDetails()
        return userDetails["email"] ?: ""
    }
}