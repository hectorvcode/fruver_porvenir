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
        private const val DATABASE_VERSION = 7 // Incrementado para la nueva columna de unidad

        // Tabla Productos
        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_UNIT = "unit" // Nueva columna
        const val COLUMN_IMAGE_RESOURCE = "image_resource"
        const val COLUMN_IMAGE_PATH = "image_path"
        const val COLUMN_DESCRIPTION = "description"

        // Tabla Usuarios
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "id"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_ROLE = "role"
        const val COLUMN_USER_PROFILE_PIC = "profile_pic_url"
        const val COLUMN_USER_PROFILE_PIC_PATH = "profile_pic_path"

        // Tabla Favoritos
        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_FAVORITE_ID = "id"
        const val COLUMN_FAVORITE_USER_ID = "user_id"
        const val COLUMN_FAVORITE_PRODUCT_ID = "product_id"
        const val COLUMN_FAVORITE_DATE_ADDED = "date_added"

        // Script para crear la tabla de productos (actualizado)
        private const val SQL_CREATE_PRODUCTS_TABLE = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_UNIT TEXT NOT NULL DEFAULT 'lb',
                $COLUMN_IMAGE_RESOURCE INTEGER,
                $COLUMN_IMAGE_PATH TEXT,
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
                $COLUMN_USER_PROFILE_PIC TEXT,
                $COLUMN_USER_PROFILE_PIC_PATH TEXT
            )
        """

        // Script para crear la tabla de favoritos
        private const val SQL_CREATE_FAVORITES_TABLE = """
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_FAVORITE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FAVORITE_USER_ID TEXT NOT NULL,
                $COLUMN_FAVORITE_PRODUCT_ID INTEGER NOT NULL,
                $COLUMN_FAVORITE_DATE_ADDED INTEGER NOT NULL,
                UNIQUE($COLUMN_FAVORITE_USER_ID, $COLUMN_FAVORITE_PRODUCT_ID)
            )
        """

        // Scripts para eliminar las tablas
        private const val SQL_DELETE_PRODUCTS_TABLE = "DROP TABLE IF EXISTS $TABLE_PRODUCTS"
        private const val SQL_DELETE_USERS_TABLE = "DROP TABLE IF EXISTS $TABLE_USERS"
        private const val SQL_DELETE_FAVORITES_TABLE = "DROP TABLE IF EXISTS $TABLE_FAVORITES"

        // Scripts para agregar nuevas columnas
        private const val SQL_ADD_IMAGE_PATH_COLUMN = "ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_IMAGE_PATH TEXT"
        private const val SQL_ADD_PROFILE_PIC_PATH_COLUMN = "ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_PROFILE_PIC_PATH TEXT"
        private const val SQL_ADD_UNIT_COLUMN = "ALTER TABLE $TABLE_PRODUCTS ADD COLUMN $COLUMN_UNIT TEXT NOT NULL DEFAULT 'lb'"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear todas las tablas
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE)
        db.execSQL(SQL_CREATE_USERS_TABLE)
        db.execSQL(SQL_CREATE_FAVORITES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        when {
            oldVersion < 2 -> {
                // Actualización de versión 1 a 2: crear tabla de usuarios
                db.execSQL(SQL_CREATE_USERS_TABLE)
            }
            oldVersion < 3 -> {
                // Actualización de versión 2 a 3: agregar columna image_path
                try {
                    db.execSQL(SQL_ADD_IMAGE_PATH_COLUMN)
                } catch (e: Exception) {
                    // Si falla, recrear toda la tabla
                    db.execSQL(SQL_DELETE_PRODUCTS_TABLE)
                    db.execSQL(SQL_CREATE_PRODUCTS_TABLE)
                }

                // Si no existe la tabla de usuarios, crearla
                db.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_USERS ($COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_USER_EMAIL TEXT NOT NULL UNIQUE, $COLUMN_USER_NAME TEXT NOT NULL, $COLUMN_USER_ROLE TEXT NOT NULL, $COLUMN_USER_PROFILE_PIC TEXT)")
            }
            oldVersion < 4 -> {
                // Actualización de versión 3 a 4: crear tabla de favoritos
                db.execSQL(SQL_CREATE_FAVORITES_TABLE)
            }
            oldVersion < 5 -> {
                // Actualización de versión 4 a 5: mantener tablas existentes
            }
            oldVersion < 6 -> {
                // Actualización de versión 5 a 6: agregar columna profile_pic_path
                try {
                    db.execSQL(SQL_ADD_PROFILE_PIC_PATH_COLUMN)
                } catch (e: Exception) {
                    // Si falla, recrear la tabla de usuarios
                    db.execSQL(SQL_DELETE_USERS_TABLE)
                    db.execSQL(SQL_CREATE_USERS_TABLE)
                }
            }
            oldVersion < 7 -> {
                // Actualización de versión 6 a 7: agregar columna unit
                try {
                    db.execSQL(SQL_ADD_UNIT_COLUMN)
                } catch (e: Exception) {
                    // Si falla, recrear la tabla de productos
                    db.execSQL(SQL_DELETE_PRODUCTS_TABLE)
                    db.execSQL(SQL_CREATE_PRODUCTS_TABLE)
                }
            }
            else -> {
                // Para versiones futuras, recrear todo
                db.execSQL(SQL_DELETE_PRODUCTS_TABLE)
                db.execSQL(SQL_DELETE_USERS_TABLE)
                db.execSQL(SQL_DELETE_FAVORITES_TABLE)
                onCreate(db)
            }
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En lugar de fallar, recrear toda la base de datos
        db.execSQL(SQL_DELETE_PRODUCTS_TABLE)
        db.execSQL(SQL_DELETE_USERS_TABLE)
        db.execSQL(SQL_DELETE_FAVORITES_TABLE)
        onCreate(db)
    }
}