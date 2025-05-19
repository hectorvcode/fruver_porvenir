package com.example.online_store.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.online_store.Activities.ListActivity
import com.example.online_store.Activities.LoginActivity

/**
 * Clase auxiliar para verificar permisos según el rol del usuario
 */
object RoleHelper {

    /**
     * Verifica si el usuario tiene rol de administrador
     * @return true si tiene permisos, false en caso contrario
     */
    fun checkAdminPermission(context: Context): Boolean {
        val sessionManager = SessionManager(context)

        // Verificar si está logueado
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(context, "Necesitas iniciar sesión", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, LoginActivity::class.java))
            if (context is Activity) {
                context.finish()
            }
            return false
        }

        // Verificar si es administrador
        if (!sessionManager.isAdmin()) {
            Toast.makeText(context, "No tienes permisos de administrador", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, ListActivity::class.java))
            if (context is Activity) {
                context.finish()
            }
            return false
        }

        return true
    }

    /**
     * Verifica si el usuario está logueado
     * @return true si está logueado, false en caso contrario
     */
    fun checkUserLoggedIn(context: Context): Boolean {
        val sessionManager = SessionManager(context)

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(context, "Necesitas iniciar sesión", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, LoginActivity::class.java))
            if (context is Activity) {
                context.finish()
            }
            return false
        }

        return true
    }
}