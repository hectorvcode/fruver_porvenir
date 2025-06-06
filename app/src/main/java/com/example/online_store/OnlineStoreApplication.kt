package com.example.online_store

import android.app.Application
import com.example.online_store.data.ProductDao
import com.example.online_store.data.UnitDao
import com.example.online_store.data.UserDao
import com.example.online_store.model.Product
import com.example.online_store.model.Unit
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
        val unitDao = UnitDao(this) // Nuevo DAO

        // Inicializar datos con los mismos DAOs
        initializeUnits(unitDao) // Inicializar unidades primero
        initializeProducts(productDao)
        initializeUsers(userDao)

        // Cerrar la base de datos al final
        productDao.closeDatabase()
        unitDao.closeDatabase() // Cerrar nuevo DAO
    }

    private fun initializeUnits(unitDao: UnitDao) {
        val units = unitDao.getAllUnits()

        if (units.isEmpty()) {
            // Si no hay unidades, añadir unidades predeterminadas
            val defaultUnits = Unit.getDefaultUnits()

            // Insertar las unidades predeterminadas
            for (unit in defaultUnits) {
                unitDao.insertUnit(unit)
            }
        }
    }

    private fun initializeProducts(productDao: ProductDao) {
        val products = productDao.getAllProducts()

        if (products.isEmpty()) {
            // Si no hay productos, añadir productos predeterminados con diferentes unidades
            val defaultProducts = listOf(
                Product(
                    name = "Banano",
                    price = 1500.0,
                    category = "Frutas",
                    unit = "lb",
                    imageResource = R.drawable.banano,
                    description = "Banano fresco por libra"
                ),
                Product(
                    name = "Naranja",
                    price = 2500.0,
                    category = "Frutas",
                    unit = "lb",
                    imageResource = R.drawable.naranja,
                    description = "Naranja dulce por libra"
                ),
                Product(
                    name = "Agua Mineral",
                    price = 1200.0,
                    category = "Bebidas",
                    unit = "l",
                    imageResource = null,
                    description = "Agua mineral natural"
                ),
                Product(
                    name = "Jugo de Naranja",
                    price = 3500.0,
                    category = "Bebidas",
                    unit = "unidad",
                    imageResource = null,
                    description = "Jugo de naranja natural 500ml"
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
                    role = User.ROLE_ADMIN,
                    password = "admin123" // Contraseña por defecto para el admin
                ),
                User(
                    email = "user@example.com",
                    name = "Usuario Regular",
                    role = User.ROLE_USER,
                    password = "user123" // Contraseña por defecto para el usuario regular
                )
            )

            // Insertar los usuarios predeterminados
            for (user in defaultUsers) {
                userDao.insertUser(user)
            }
        }
    }
}