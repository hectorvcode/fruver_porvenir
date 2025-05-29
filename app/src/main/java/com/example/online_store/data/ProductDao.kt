package com.example.online_store.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.online_store.model.Product

/**
 * Data Access Object para operaciones CRUD de productos
 */
class ProductDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /**
     * Inserta un nuevo producto en la base de datos
     * @return ID del producto insertado
     */
    fun insertProduct(product: Product): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, product.name)
            put(DatabaseHelper.COLUMN_PRICE, product.price)
            put(DatabaseHelper.COLUMN_CATEGORY, product.category)
            product.imageResource?.let { put(DatabaseHelper.COLUMN_IMAGE_RESOURCE, it) }
            put(DatabaseHelper.COLUMN_IMAGE_PATH, product.imagePath) // Nueva columna
            put(DatabaseHelper.COLUMN_DESCRIPTION, product.description)
        }

        val id = db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values)
        return id
    }

    /**
     * Obtiene todos los productos
     */
    fun getAllProducts(): List<Product> {
        val productsList = mutableListOf<Product>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_PRODUCTS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_NAME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val product = cursorToProduct(cursor)
                productsList.add(product)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return productsList
    }

    /**
     * Obtiene los productos por categoría
     */
    fun getProductsByCategory(category: String): List<Product> {
        val productsList = mutableListOf<Product>()
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_CATEGORY} = ?"
        val selectionArgs = arrayOf(category)

        val cursor = db.query(
            DatabaseHelper.TABLE_PRODUCTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_NAME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val product = cursorToProduct(cursor)
                productsList.add(product)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return productsList
    }

    /**
     * Obtiene un producto por su ID
     */
    fun getProductById(id: Int): Product? {
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = db.query(
            DatabaseHelper.TABLE_PRODUCTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var product: Product? = null

        if (cursor.moveToFirst()) {
            product = cursorToProduct(cursor)
        }

        cursor.close()
        return product
    }

    /**
     * Actualiza un producto existente
     * @return Número de filas afectadas
     */
    fun updateProduct(product: Product): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, product.name)
            put(DatabaseHelper.COLUMN_PRICE, product.price)
            put(DatabaseHelper.COLUMN_CATEGORY, product.category)
            product.imageResource?.let { put(DatabaseHelper.COLUMN_IMAGE_RESOURCE, it) }
            put(DatabaseHelper.COLUMN_IMAGE_PATH, product.imagePath) // Nueva columna
            put(DatabaseHelper.COLUMN_DESCRIPTION, product.description)
        }

        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(product.id.toString())

        val count = db.update(
            DatabaseHelper.TABLE_PRODUCTS,
            values,
            selection,
            selectionArgs
        )

        return count
    }

    /**
     * Elimina un producto por su ID
     * @return Número de filas afectadas
     */
    fun deleteProduct(id: Int): Int {
        val db = dbHelper.writableDatabase

        val selection = "${DatabaseHelper.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val count = db.delete(
            DatabaseHelper.TABLE_PRODUCTS,
            selection,
            selectionArgs
        )

        return count
    }

    /**
     * Elimina todos los productos
     * @return Número de filas afectadas
     */
    fun deleteAllProducts(): Int {
        val db = dbHelper.writableDatabase
        val count = db.delete(DatabaseHelper.TABLE_PRODUCTS, null, null)
        return count
    }

    /**
     * Método para cerrar la base de datos explícitamente cuando sea necesario
     */
    fun closeDatabase() {
        dbHelper.close()
    }

    /**
     * Convierte un cursor a un objeto Product
     */
    private fun cursorToProduct(cursor: Cursor): Product {
        val idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)
        val nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)
        val priceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PRICE)
        val categoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY)
        val imageResourceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_RESOURCE)
        val imagePathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE_PATH) // Nueva columna
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
            imagePath = imagePath, // Nueva propiedad
            description = description
        )
    }
}