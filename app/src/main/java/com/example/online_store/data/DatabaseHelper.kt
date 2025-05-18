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
        private const val DATABASE_VERSION = 1

        // Tabla Productos
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMAGE_RESOURCE = "image_resource"
        const val COLUMN_DESCRIPTION = "description"

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

        // Script para eliminar la tabla
        private const val SQL_DELETE_PRODUCTS_TABLE = "DROP TABLE IF EXISTS $TABLE_PRODUCTS"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de actualización, eliminamos la tabla y la recreamos
        db.execSQL(SQL_DELETE_PRODUCTS_TABLE)
        onCreate(db)
    }
}