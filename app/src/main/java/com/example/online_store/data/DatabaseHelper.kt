package com.example.online_store.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Helper de la base de datos SQLite para la tienda online
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "online_store.db"
        private const val DATABASE_VERSION = 2 // Incrementado para incluir la tabla de usuarios

        // Tabla Productos
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMAGE_RESOURCE = "image_resource"
        const val COLUMN_DESCRIPTION = "description"

        // Nueva tabla Usuarios
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_ROLE = "role"
        const val COLUMN_USER_PROFILE_PIC = "profile_pic_url"

        // Script para crear la tabla de productos
        private const val SQL_CREATE_PRODUCTS_TABLE = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_IMAGE_RESOURCE INTEGER,
                $COLUMN_DESCRIPTION TEXT
            )
        """

        // Script para crear la tabla de usuarios
        private const val SQL_CREATE_USERS_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_ROLE TEXT NOT NULL,
                $COLUMN_USER_PROFILE_PIC TEXT
            )
        """

        // Scripts para eliminar las tablas
        private const val SQL_DELETE_PRODUCTS_TABLE = "DROP TABLE IF EXISTS $TABLE_PRODUCTS"
        private const val SQL_DELETE_USERS_TABLE = "DROP TABLE IF EXISTS $TABLE_USERS"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear todas las tablas
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE)
        db.execSQL(SQL_CREATE_USERS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de actualización, eliminar las tablas y recrearlas
        if (oldVersion < 2) {
            // Si actualiza desde versión 1, solo crea la tabla de usuarios
            db.execSQL(SQL_CREATE_USERS_TABLE)
        } else {
            // De lo contrario, recrea todo
            db.execSQL(SQL_DELETE_PRODUCTS_TABLE)
            db.execSQL(SQL_DELETE_USERS_TABLE)
            onCreate(db)
        }
    }
}