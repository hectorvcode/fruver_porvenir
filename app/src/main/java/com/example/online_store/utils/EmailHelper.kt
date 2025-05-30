package com.example.online_store.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Clase auxiliar para el envío de emails de recuperación
 */
object EmailHelper {

    /**
     * Envía un email de recuperación de contraseña
     * En una implementación real, esto se haría a través de un servicio backend
     */
    fun sendRecoveryEmail(context: Context, userEmail: String, recoveryCode: String): Boolean {
        return try {
            // En una implementación real, aquí enviarías el email a través de tu backend
            // Por ahora, simulamos el envío abriendo la app de email del dispositivo

            val subject = "Fruver Porvenir - Código de Recuperación"
            val body = """
                Hola,
                
                Has solicitado recuperar tu contraseña en Fruver Porvenir.
                
                Tu código de verificación es: $recoveryCode
                
                Este código es válido por 10 minutos.
                
                Si no solicitaste este cambio, puedes ignorar este mensaje.
                
                Saludos,
                Equipo Fruver Porvenir
            """.trimIndent()

            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$userEmail")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            // Verificar si hay una app de email disponible
            if (emailIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(emailIntent)

                // Mostrar mensaje informativo
                Toast.makeText(
                    context,
                    "Se ha abierto tu app de email. Envía el mensaje para recibir el código.",
                    Toast.LENGTH_LONG
                ).show()

                return true
            } else {
                // No hay app de email, simular envío exitoso para propósitos de demostración
                Toast.makeText(
                    context,
                    "Email simulado enviado. Tu código es: $recoveryCode",
                    Toast.LENGTH_LONG
                ).show()

                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()

            // En caso de error, mostrar el código directamente (solo para desarrollo)
            Toast.makeText(
                context,
                "Error al enviar email. Tu código es: $recoveryCode",
                Toast.LENGTH_LONG
            ).show()

            return true
        }
    }

    /**
     * Valida el formato de email
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Genera un código de recuperación de 6 dígitos
     */
    fun generateRecoveryCode(): String {
        return (100000..999999).random().toString()
    }

    /**
     * En una implementación real, este método se conectaría con tu backend
     * para enviar emails a través de servicios como SendGrid, AWS SES, etc.
     */
    private fun sendEmailThroughBackend(
        userEmail: String,
        subject: String,
        body: String
    ): Boolean {
        // TODO: Implementar conexión con backend para envío real de emails
        // Ejemplo:
        // val apiCall = emailService.sendEmail(userEmail, subject, body)
        // return apiCall.isSuccessful

        return true // Simulación para desarrollo
    }
}