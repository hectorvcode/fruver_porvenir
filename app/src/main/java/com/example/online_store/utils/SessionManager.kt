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
        private const val KEY_PROFILE_PIC_PATH = "profilePicPath" // Nueva clave
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
        user.profilePicPath?.let { editor.putString(KEY_PROFILE_PIC_PATH, it) } // Guardar imagen personalizada
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
        user[KEY_PROFILE_PIC_PATH] = pref.getString(KEY_PROFILE_PIC_PATH, null) // Obtener imagen personalizada
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
     * Actualiza la imagen de perfil personalizada en la sesión
     */
    fun updateUserProfilePicPath(profilePicPath: String?) {
        if (profilePicPath != null) {
            editor.putString(KEY_PROFILE_PIC_PATH, profilePicPath)
        } else {
            editor.remove(KEY_PROFILE_PIC_PATH)
        }
        editor.apply()
    }

    /**
     * Obtiene la imagen de perfil a usar (prioriza imagen personalizada)
     */
    fun getUserProfileImage(): String? {
        val profilePicPath = pref.getString(KEY_PROFILE_PIC_PATH, null)
        val profilePicUrl = pref.getString(KEY_PROFILE_PIC, null)

        // Priorizar imagen personalizada sobre URL de Google
        return profilePicPath ?: profilePicUrl
    }

    /**
     * Cierra la sesión
     */
    fun logout() {
        editor.clear()
        editor.apply()
    }

    /**
     * Actualiza la contraseña en la sesión
     */
    fun updateSessionPassword(password: String) {
        editor.putString("password", password)
        editor.apply()
    }
}