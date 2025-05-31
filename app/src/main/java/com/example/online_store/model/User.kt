package com.example.online_store.model

/**
 * Clase modelo para representar un usuario en la aplicación
 */
data class User(
    val id: Int = 0,                // ID único del usuario
    val email: String,              // Email (usado como identificador único)
    val name: String,               // Nombre completo
    val role: String,               // Rol: "ADMIN" o "USER"
    val password: String = "",      // Contraseña (hasheada para seguridad)
    val profilePicUrl: String? = null, // URL de foto de perfil (Google)
    val profilePicPath: String? = null // Ruta de imagen personalizada (cámara/galería)
) {
    companion object {
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_USER = "USER"
    }

    /**
     * Función para determinar qué imagen de perfil usar
     * Prioriza profilePicPath sobre profilePicUrl
     */
    fun getProfileImageToUse(): String? {
        return profilePicPath ?: profilePicUrl
    }

    /**
     * Indica si el usuario tiene una imagen personalizada (tomada con cámara/galería)
     */
    fun hasCustomProfileImage(): Boolean {
        return !profilePicPath.isNullOrEmpty()
    }
}