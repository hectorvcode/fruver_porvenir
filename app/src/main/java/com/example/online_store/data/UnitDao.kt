package com.example.online_store.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.online_store.model.Unit

/**
 * Data Access Object para operaciones CRUD de unidades de medida
 */
class UnitDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /**
     * Inserta una nueva unidad en la base de datos
     * @return ID de la unidad insertada o -1 si ya existe
     */
    fun insertUnit(unit: Unit): Long {
        val db = dbHelper.writableDatabase

        // Verificar si la abreviación ya existe
        if (getUnitByAbbreviation(unit.abbreviation) != null) {
            return -1 // Unidad ya existe
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_UNIT_NAME, unit.name)
            put(DatabaseHelper.COLUMN_UNIT_ABBREVIATION, unit.abbreviation)
            put(DatabaseHelper.COLUMN_UNIT_CATEGORY, unit.category)
            put(DatabaseHelper.COLUMN_UNIT_CONVERSION_FACTOR, unit.conversionFactor)
            put(DatabaseHelper.COLUMN_UNIT_IS_ACTIVE, if (unit.isActive) 1 else 0)
        }

        val id = db.insert(DatabaseHelper.TABLE_UNITS, null, values)
        return id
    }

    /**
     * Obtiene todas las unidades
     */
    fun getAllUnits(): List<Unit> {
        val unitsList = mutableListOf<Unit>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_UNITS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_UNIT_CATEGORY} ASC, ${DatabaseHelper.COLUMN_UNIT_NAME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val unit = cursorToUnit(cursor)
                unitsList.add(unit)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return unitsList
    }

    /**
     * Obtiene solo las unidades activas
     */
    fun getActiveUnits(): List<Unit> {
        val unitsList = mutableListOf<Unit>()
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_UNIT_IS_ACTIVE} = ?"
        val selectionArgs = arrayOf("1")

        val cursor = db.query(
            DatabaseHelper.TABLE_UNITS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_UNIT_CATEGORY} ASC, ${DatabaseHelper.COLUMN_UNIT_NAME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val unit = cursorToUnit(cursor)
                unitsList.add(unit)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return unitsList
    }

    /**
     * Obtiene unidades por categoría
     */
    fun getUnitsByCategory(category: String): List<Unit> {
        val unitsList = mutableListOf<Unit>()
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_UNIT_CATEGORY} = ? AND ${DatabaseHelper.COLUMN_UNIT_IS_ACTIVE} = ?"
        val selectionArgs = arrayOf(category, "1")

        val cursor = db.query(
            DatabaseHelper.TABLE_UNITS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_UNIT_NAME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val unit = cursorToUnit(cursor)
                unitsList.add(unit)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return unitsList
    }

    /**
     * Obtiene una unidad por su ID
     */
    fun getUnitById(id: Int): Unit? {
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_UNIT_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = db.query(
            DatabaseHelper.TABLE_UNITS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var unit: Unit? = null

        if (cursor.moveToFirst()) {
            unit = cursorToUnit(cursor)
        }

        cursor.close()
        return unit
    }

    /**
     * Obtiene una unidad por su abreviación
     */
    fun getUnitByAbbreviation(abbreviation: String): Unit? {
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_UNIT_ABBREVIATION} = ?"
        val selectionArgs = arrayOf(abbreviation)

        val cursor = db.query(
            DatabaseHelper.TABLE_UNITS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var unit: Unit? = null

        if (cursor.moveToFirst()) {
            unit = cursorToUnit(cursor)
        }

        cursor.close()
        return unit
    }

    /**
     * Actualiza una unidad existente
     * @return Número de filas afectadas
     */
    fun updateUnit(unit: Unit): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_UNIT_NAME, unit.name)
            put(DatabaseHelper.COLUMN_UNIT_ABBREVIATION, unit.abbreviation)
            put(DatabaseHelper.COLUMN_UNIT_CATEGORY, unit.category)
            put(DatabaseHelper.COLUMN_UNIT_CONVERSION_FACTOR, unit.conversionFactor)
            put(DatabaseHelper.COLUMN_UNIT_IS_ACTIVE, if (unit.isActive) 1 else 0)
        }

        val selection = "${DatabaseHelper.COLUMN_UNIT_ID} = ?"
        val selectionArgs = arrayOf(unit.id.toString())

        val count = db.update(
            DatabaseHelper.TABLE_UNITS,
            values,
            selection,
            selectionArgs
        )

        return count
    }

    /**
     * Marca una unidad como inactiva (borrado lógico)
     * @return Número de filas afectadas
     */
    fun deactivateUnit(id: Int): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_UNIT_IS_ACTIVE, 0)
        }

        val selection = "${DatabaseHelper.COLUMN_UNIT_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val count = db.update(
            DatabaseHelper.TABLE_UNITS,
            values,
            selection,
            selectionArgs
        )

        return count
    }

    /**
     * Elimina una unidad permanentemente
     * @return Número de filas afectadas
     */
    fun deleteUnit(id: Int): Int {
        val db = dbHelper.writableDatabase

        val selection = "${DatabaseHelper.COLUMN_UNIT_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        val count = db.delete(
            DatabaseHelper.TABLE_UNITS,
            selection,
            selectionArgs
        )

        return count
    }

    /**
     * Verifica si una unidad está siendo utilizada por productos
     */
    fun isUnitInUse(abbreviation: String): Boolean {
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_UNIT} = ?"
        val selectionArgs = arrayOf(abbreviation)

        val cursor = db.query(
            DatabaseHelper.TABLE_PRODUCTS,
            arrayOf(DatabaseHelper.COLUMN_ID),
            selection,
            selectionArgs,
            null,
            null,
            null,
            "1" // LIMIT 1
        )

        val inUse = cursor.count > 0
        cursor.close()
        return inUse
    }

    /**
     * Obtiene las abreviaciones de todas las unidades activas
     */
    fun getActiveUnitAbbreviations(): List<String> {
        val abbreviations = mutableListOf<String>()
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_UNIT_IS_ACTIVE} = ?"
        val selectionArgs = arrayOf("1")

        val cursor = db.query(
            DatabaseHelper.TABLE_UNITS,
            arrayOf(DatabaseHelper.COLUMN_UNIT_ABBREVIATION),
            selection,
            selectionArgs,
            null,
            null,
            "${DatabaseHelper.COLUMN_UNIT_NAME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val abbreviationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_UNIT_ABBREVIATION)
                if (abbreviationIndex >= 0) {
                    abbreviations.add(cursor.getString(abbreviationIndex))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return abbreviations
    }

    /**
     * Método para cerrar la base de datos explícitamente cuando sea necesario
     */
    fun closeDatabase() {
        dbHelper.close()
    }

    /**
     * Convierte un cursor a un objeto Unit
     */
    private fun cursorToUnit(cursor: Cursor): Unit {
        val idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_UNIT_ID)
        val nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_UNIT_NAME)
        val abbreviationIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_UNIT_ABBREVIATION)
        val categoryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_UNIT_CATEGORY)
        val conversionFactorIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_UNIT_CONVERSION_FACTOR)
        val isActiveIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_UNIT_IS_ACTIVE)

        val id = if (idIndex >= 0) cursor.getInt(idIndex) else 0
        val name = if (nameIndex >= 0) cursor.getString(nameIndex) else ""
        val abbreviation = if (abbreviationIndex >= 0) cursor.getString(abbreviationIndex) else ""
        val category = if (categoryIndex >= 0) cursor.getString(categoryIndex) else ""
        val conversionFactor = if (conversionFactorIndex >= 0) cursor.getDouble(conversionFactorIndex) else 1.0
        val isActive = if (isActiveIndex >= 0) cursor.getInt(isActiveIndex) == 1 else true

        return Unit(
            id = id,
            name = name,
            abbreviation = abbreviation,
            category = category,
            conversionFactor = conversionFactor,
            isActive = isActive
        )
    }
}