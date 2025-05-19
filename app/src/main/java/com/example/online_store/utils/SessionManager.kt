package com.example.online_store.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.online_store.model.User

/**
 * Clase para gestionar la sesión del usuario
 */
class SessionManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    companion object {
        private const val PREF_NAME = "OnlineStoreSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "name"
        private const val KEY_ROLE = "role"
        private const val KEY_PROFILE_PIC = "profilePic"
    }

    /**
     * Guarda la sesión del usuario
     */
    fun createLoginSession(user: User) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_ROLE, user.role)
        user.profilePicUrl?.let { editor.putString(KEY_PROFILE_PIC, it) }
        editor.apply()
    }

    /**
     * Obtiene los datos del usuario en sesión
     */
    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_EMAIL] = pref.getString(KEY_EMAIL, null)
        user[KEY_NAME] = pref.getString(KEY_NAME, null)
        user[KEY_ROLE] = pref.getString(KEY_ROLE, null)
        user[KEY_PROFILE_PIC] = pref.getString(KEY_PROFILE_PIC, null)
        return user
    }

    /**
     * Verifica si el usuario está logueado
     */
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Verifica si el usuario tiene rol de administrador
     */
    fun isAdmin(): Boolean {
        val role = pref.getString(KEY_ROLE, null)
        return role == User.ROLE_ADMIN
    }

    /**
     * Actualiza el rol del usuario en la sesión
     */
    fun updateUserRole(role: String) {
        editor.putString(KEY_ROLE, role)
        editor.apply()
    }

    /**
     * Cierra la sesión
     */
    fun logout() {
        editor.clear()
        editor.apply()
    }

    // Añadir esta función a la clase SessionManager
    fun updateSessionPassword(password: String) {
        editor.putString("password", password)
        editor.apply()
    }
}