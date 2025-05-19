package com.example.online_store

import android.app.Application
import com.example.online_store.data.ProductDao
import com.example.online_store.data.UserDao
import com.example.online_store.model.Product
import com.example.online_store.model.User

class OnlineStoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar la base de datos
        initializeDatabase()
    }

    private fun initializeDatabase() {
        // Crear una única instancia de los DAOs
        val productDao = ProductDao(this)
        val userDao = UserDao(this)

        // Inicializar productos y usuarios con los mismos DAOs
        initializeProducts(productDao)
        initializeUsers(userDao)

        // Cerrar la base de datos al final
        productDao.closeDatabase()
    }

    private fun initializeProducts(productDao: ProductDao) {
        val products = productDao.getAllProducts()

        if (products.isEmpty()) {
            // Si no hay productos, añadir solo dos productos predeterminados
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

    private fun initializeUsers(userDao: UserDao) {
        val users = userDao.getAllUsers()

        if (users.isEmpty()) {
            // Crear usuarios predeterminados: un admin y un usuario regular
            val defaultUsers = listOf(
                User(
                    email = "admin@example.com",
                    name = "Administrador",
                    role = User.ROLE_ADMIN
                ),
                User(
                    email = "user@example.com",
                    name = "Usuario Regular",
                    role = User.ROLE_USER
                )
            )

            // Insertar los usuarios predeterminados
            for (user in defaultUsers) {
                userDao.insertUser(user)
            }
        }
    }
}