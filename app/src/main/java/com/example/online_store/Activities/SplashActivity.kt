package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000 // 3 segundos
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inicializar el SessionManager
        sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            // Determinar la actividad de destino según el estado de la sesión
            val intent = if (sessionManager.isLoggedIn()) {
                // Si el usuario ya está logueado, ir a la pantalla principal
                Intent(this, MainContainerActivity::class.java)
            } else {
                // Si no está logueado, ir a la pantalla de inicio
                Intent(this, HomeActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_TIME_OUT)
    }
}