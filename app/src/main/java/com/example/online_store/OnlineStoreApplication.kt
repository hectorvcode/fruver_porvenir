package com.example.online_store

import android.app.Application
import com.example.online_store.data.ProductDao
import com.example.online_store.model.Product

class OnlineStoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar la base de datos
        initializeDatabase()
    }

    private fun initializeDatabase() {
        val productDao = ProductDao(this)
        val products = productDao.getAllProducts()

        if (products.isEmpty()) {
            // Si no hay productos, añadir algunos productos predeterminados
            val defaultProducts = listOf(
                Product(
                    name = "Banano",
                    price = 1500.0,
                    category = "Frutas",
                    imageResource = R.drawable.banano,
                    description = "Banano fresco por libra"
                ),
                Product(
                    name = "Naranja",
                    price = 2500.0,
                    category = "Frutas",
                    imageResource = R.drawable.naranja,
                    description = "Naranja dulce por libra"
                )
            )

            // Insertar los productos predeterminados
            for (product in defaultProducts) {
                productDao.insertProduct(product)
            }
        }
    }
}