package com.example.online_store.model

/**
 * Clase modelo para representar un usuario en la aplicación
 */
data class User(
    val id: Int = 0,                // ID único del usuario
    val email: String,              // Email (usado como identificador único)
    val name: String,               // Nombre completo
    val role: String,               // Rol: "ADMIN" o "USER"
    val profilePicUrl: String? = null // URL de foto de perfil (opcional)
) {
    companion object {
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_USER = "USER"
    }
}