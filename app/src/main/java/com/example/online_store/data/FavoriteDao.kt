package com.example.online_store.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.online_store.model.Favorite
import com.example.online_store.model.Product

/**
 * Data Access Object para operaciones CRUD de favoritos
 */
class FavoriteDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val productDao = ProductDao(context)

    /**
     * Agrega un producto a favoritos
     */
    fun addToFavorites(userId: String, productId: Int): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_FAVORITE_USER_ID, userId)
            put(DatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID, productId)
            put(DatabaseHelper.COLUMN_FAVORITE_DATE_ADDED, System.currentTimeMillis())
        }

        return try {
            db.insertWithOnConflict(
                DatabaseHelper.TABLE_FAVORITES,
                null,
                values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Elimina un producto de favoritos
     */
    fun removeFromFavorites(userId: String, productId: Int): Int {
        val db = dbHelper.writableDatabase

        val selection = "${DatabaseHelper.COLUMN_FAVORITE_USER_ID} = ? AND ${DatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID} = ?"
        val selectionArgs = arrayOf(userId, productId.toString())

        return try {
            db.delete(DatabaseHelper.TABLE_FAVORITES, selection, selectionArgs)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Verifica si un producto está en favoritos
     */
    fun isFavorite(userId: String, productId: Int): Boolean {
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_FAVORITE_USER_ID} = ? AND ${DatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID} = ?"
        val selectionArgs = arrayOf(userId, productId.toString())

        val cursor = db.query(
            DatabaseHelper.TABLE_FAVORITES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    /**
     * Obtiene todos los productos favoritos de un usuario
     */
    fun getFavoriteProducts(userId: String): List<Product> {
        val favoritesList = mutableListOf<Product>()
        val db = dbHelper.readableDatabase

        val query = """
            SELECT p.* FROM ${DatabaseHelper.TABLE_PRODUCTS} p
            INNER JOIN ${DatabaseHelper.TABLE_FAVORITES} f ON p.${DatabaseHelper.COLUMN_ID} = f.${DatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID}
            WHERE f.${DatabaseHelper.COLUMN_FAVORITE_USER_ID} = ?
            ORDER BY f.${DatabaseHelper.COLUMN_FAVORITE_DATE_ADDED} DESC
        """

        val cursor = db.rawQuery(query, arrayOf(userId))

        if (cursor.moveToFirst()) {
            do {
                val product = cursorToProduct(cursor)
                product.isFavorite = true // Marcar como favorito
                favoritesList.add(product)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return favoritesList
    }

    /**
     * Obtiene todos los favoritos de un usuario (objetos Favorite)
     */
    fun getUserFavorites(userId: String): List<Favorite> {
        val favoritesList = mutableListOf<Favorite>()
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_FAVORITE_USER_ID} = ?"
        val selectionArgs = arrayOf(userId)

        val cursor = db.query(
            DatabaseHelper.TABLE_FAVORITES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_FAVORITE_DATE_ADDED} DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                val favorite = cursorToFavorite(cursor)
                favoritesList.add(favorite)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return favoritesList
    }

    /**
     * Elimina todos los favoritos de un usuario
     */
    fun clearUserFavorites(userId: String): Int {
        val db = dbHelper.writableDatabase

        val selection = "${DatabaseHelper.COLUMN_FAVORITE_USER_ID} = ?"
        val selectionArgs = arrayOf(userId)

        return try {
            db.delete(DatabaseHelper.TABLE_FAVORITES, selection, selectionArgs)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    /**
     * Obtiene el conteo de favoritos de un usuario
     */
    fun getFavoritesCount(userId: String): Int {
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_FAVORITE_USER_ID} = ?"
        val selectionArgs = arrayOf(userId)

        val cursor = db.query(
            DatabaseHelper.TABLE_FAVORITES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        val count = cursor.count
        cursor.close()
        return count
    }

    /**
     * Convierte un cursor a un objeto Favorite
     */
    private fun cursorToFavorite(cursor: Cursor): Favorite {
        val idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE_ID)
        val userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE_USER_ID)
        val productIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID)
        val dateAddedIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_FAVORITE_DATE_ADDED)

        val id = if (idIndex >= 0) cursor.getInt(idIndex) else 0
        val userId = if (userIdIndex >= 0) cursor.getString(userIdIndex) else ""
        val productId = if (productIdIndex >= 0) cursor.getInt(productIdIndex) else 0
        val dateAdded = if (dateAddedIndex >= 0) cursor.getLong(dateAddedIndex) else System.currentTimeMillis()

        return Favorite(
            id = id,
            userId = userId,
            productId = productId,
            dateAdded = dateAdded
        )
    }

    /**
     * Convierte un cursor a un objeto Product (usando la lógica del ProductDao)
     */
    private fun cursorToProduct(cursor: Cursor): Product {
        val idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)
        val nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)
        val priceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRICE)
        val categoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY)
        val imageResourceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_RESOURCE)
        val imagePathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_PATH)
        val descriptionIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION)

        val id = if (idIndex >= 0) cursor.getInt(idIndex) else 0
        val name = if (nameIndex >= 0) cursor.getString(nameIndex) else ""
        val price = if (priceIndex >= 0) cursor.getDouble(priceIndex) else 0.0
        val category = if (categoryIndex >= 0) cursor.getString(categoryIndex) else ""
        val imageResource = if (imageResourceIndex >= 0 && !cursor.isNull(imageResourceIndex))
            cursor.getInt(imageResourceIndex) else null
        val imagePath = if (imagePathIndex >= 0 && !cursor.isNull(imagePathIndex))
            cursor.getString(imagePathIndex) else null
        val description = if (descriptionIndex >= 0) cursor.getString(descriptionIndex) else ""

        return Product(
            id = id,
            name = name,
            price = price,
            category = category,
            imageResource = imageResource,
            imagePath = imagePath,
            description = description
        )
    }

    /**
     * Método para cerrar la base de datos explícitamente cuando sea necesario
     */
    fun closeDatabase() {
        dbHelper.close()
    }
}