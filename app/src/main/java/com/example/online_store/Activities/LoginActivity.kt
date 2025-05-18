package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {

    private lateinit var btnGoogle: Button
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    private val TAG = "GoogleSignIn"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()

        // Crear el cliente de Google SignIn
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogle = findViewById(R.id.btn_continuar_google)

        btnGoogle.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Inicio de sesión exitoso
            Log.d(TAG, "signInSuccess: ${account.email}")
            Toast.makeText(this, "Bienvenido ${account.displayName}", Toast.LENGTH_SHORT).show()

            // Ir a MainActivity
            intent = Intent(this, ListActivity::class.java)
            intent.putExtra("USER_EMAIL", account.email)
            intent.putExtra("USER_NAME", account.displayName)
            startActivity(intent)

        } catch (e: ApiException) {
            // Error en el inicio de sesión


            Toast.makeText(this, "mensaje", Toast.LENGTH_SHORT).show()
        }
    }
}