package com.example.online_store.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Utilidad para manejar el hash y verificación de contraseñas
 */
object PasswordUtils {

    private const val ALGORITHM = "SHA-256"
    private const val SALT_BYTES = 16

    /**
     * Genera un hash para la contraseña proporcionada con un salt aleatorio
     * @param password La contraseña en texto plano
     * @return String con formato "salt:hash" en Base64
     */
    fun hashPassword(password: String): String {
        // Generar salt aleatorio
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)

        // Hashear la contraseña con el salt
        val hash = getHash(password, salt)

        // Convertir salt y hash a Base64 para almacenamiento
        val saltBase64 = Base64.getEncoder().encodeToString(salt)
        val hashBase64 = Base64.getEncoder().encodeToString(hash)

        // Devolver en formato "salt:hash"
        return "$saltBase64:$hashBase64"
    }

    /**
     * Verifica si una contraseña coincide con su hash almacenado
     * @param password La contraseña en texto plano a verificar
     * @param storedHash El hash almacenado en formato "salt:hash"
     * @return true si la contraseña coincide, false en caso contrario
     */
    fun verifyPassword(password: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrEmpty()) {
            return false // No hay contraseña almacenada
        }

        try {
            // Separar salt y hash
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = Base64.getDecoder().decode(parts[0])
            val expectedHash = Base64.getDecoder().decode(parts[1])

            // Calcular hash de la contraseña de entrada con el mismo salt
            val calculatedHash = getHash(password, salt)

            // Comparar los hashes
            return calculatedHash.contentEquals(expectedHash)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Genera un hash de la contraseña con el salt proporcionado
     */
    private fun getHash(password: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance(ALGORITHM)
        md.reset()
        md.update(salt)
        return md.digest(password.toByteArray(Charsets.UTF_8))
    }
}