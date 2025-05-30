package com.example.online_store.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.online_store.model.User

/**
 * Data Access Object para operaciones relacionadas con usuarios
 */
class UserDao(context: Context) {

    private val dbHelper = DatabaseHelper(context)

    /**
     * Inserta un nuevo usuario en la base de datos
     * @return ID del usuario insertado o -1 si ya existe
     */
    fun insertUser(user: User): Long {
        val db = dbHelper.writableDatabase

        // Verificar si el email ya existe
        if (getUserByEmail(user.email) != null) {
            return -1 // Usuario ya existe
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_EMAIL, user.email)
            put(DatabaseHelper.COLUMN_USER_NAME, user.name)
            put(DatabaseHelper.COLUMN_USER_ROLE, user.role)
            put(DatabaseHelper.COLUMN_USER_PROFILE_PIC, user.profilePicUrl)
            put(DatabaseHelper.COLUMN_USER_PROFILE_PIC_PATH, user.profilePicPath) // Nueva columna
        }

        val id = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        return id
    }

    /**
     * Obtiene un usuario por su email
     */
    fun getUserByEmail(email: String): User? {
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseHelper.COLUMN_USER_EMAIL} = ?"
        val selectionArgs = arrayOf(email)

        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var user: User? = null

        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor)
        }

        cursor.close()
        return user
    }

    /**
     * Verifica si un usuario es administrador
     */
    fun isUserAdmin(email: String): Boolean {
        val user = getUserByEmail(email)
        return user?.role == User.ROLE_ADMIN
    }

    /**
     * Actualiza el rol de un usuario
     * @return Número de filas afectadas
     */
    fun updateUserRole(email: String, newRole: String): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_ROLE, newRole)
        }

        val selection = "${DatabaseHelper.COLUMN_USER_EMAIL} = ?"
        val selectionArgs = arrayOf(email)

        val count = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs)
        return count
    }

    /**
     * Actualiza el nombre y el rol de un usuario
     * @return Número de filas afectadas
     */
    fun updateUserNameAndRole(email: String, newName: String, newRole: String): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, newName)
            put(DatabaseHelper.COLUMN_USER_ROLE, newRole)
        }

        val selection = "${DatabaseHelper.COLUMN_USER_EMAIL} = ?"
        val selectionArgs = arrayOf(email)

        val count = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs)
        return count
    }

    /**
     * Actualiza la imagen de perfil personalizada de un usuario
     * @return Número de filas afectadas
     */
    fun updateUserProfilePicPath(email: String, profilePicPath: String?): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_PROFILE_PIC_PATH, profilePicPath)
        }

        val selection = "${DatabaseHelper.COLUMN_USER_EMAIL} = ?"
        val selectionArgs = arrayOf(email)

        val count = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs)
        return count
    }

    /**
     * Actualiza toda la información del usuario incluyendo imagen de perfil
     * @return Número de filas afectadas
     */
    fun updateUserComplete(email: String, newName: String, newRole: String, profilePicPath: String?): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, newName)
            put(DatabaseHelper.COLUMN_USER_ROLE, newRole)
            put(DatabaseHelper.COLUMN_USER_PROFILE_PIC_PATH, profilePicPath)
        }

        val selection = "${DatabaseHelper.COLUMN_USER_EMAIL} = ?"
        val selectionArgs = arrayOf(email)

        val count = db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs)
        return count
    }

    /**
     * Obtiene todos los usuarios
     */
    fun getAllUsers(): List<User> {
        val usersList = mutableListOf<User>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_USER_NAME} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                val user = cursorToUser(cursor)
                usersList.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return usersList
    }

    /**
     * Elimina un usuario por su email
     * @return Número de filas afectadas
     */
    fun deleteUser(email: String): Int {
        val db = dbHelper.writableDatabase

        val selection = "${DatabaseHelper.COLUMN_USER_EMAIL} = ?"
        val selectionArgs = arrayOf(email)

        val count = db.delete(DatabaseHelper.TABLE_USERS, selection, selectionArgs)
        return count
    }

    /**
     * Método para cerrar la base de datos explícitamente cuando sea necesario
     */
    fun closeDatabase() {
        dbHelper.close()
    }

    /**
     * Convierte un cursor a un objeto User
     */
    private fun cursorToUser(cursor: Cursor): User {
        val idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID)
        val emailIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_EMAIL)
        val nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME)
        val roleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ROLE)
        val profilePicIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PROFILE_PIC)
        val profilePicPathIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PROFILE_PIC_PATH)

        val id = if (idIndex >= 0) cursor.getInt(idIndex) else 0
        val email = if (emailIndex >= 0) cursor.getString(emailIndex) else ""
        val name = if (nameIndex >= 0) cursor.getString(nameIndex) else ""
        val role = if (roleIndex >= 0) cursor.getString(roleIndex) else User.ROLE_USER
        val profilePicUrl = if (profilePicIndex >= 0 && !cursor.isNull(profilePicIndex))
            cursor.getString(profilePicIndex) else null
        val profilePicPath = if (profilePicPathIndex >= 0 && !cursor.isNull(profilePicPathIndex))
            cursor.getString(profilePicPathIndex) else null

        return User(
            id = id,
            email = email,
            name = name,
            role = role,
            profilePicUrl = profilePicUrl,
            profilePicPath = profilePicPath
        )
    }
}