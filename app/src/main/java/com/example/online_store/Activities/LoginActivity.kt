package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.UserDao
import com.example.online_store.model.User
import com.example.online_store.utils.PasswordUtils
import com.example.online_store.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {

    private lateinit var btnGoogle: Button
    private lateinit var btnIngresar: Button
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var tvRegister: TextView
    private lateinit var tvRecuperarContrasena: TextView // Nueva vista
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager
    private lateinit var userDao: UserDao
    private val RC_SIGN_IN = 123
    private val TAG = "GoogleSignIn"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar SessionManager y UserDao
        sessionManager = SessionManager(this)
        userDao = UserDao(this)

        // Verificar si el usuario ya está logueado
        if (sessionManager.isLoggedIn()) {
            redirectBasedOnRole()
            return
        }

        // Inicializar vistas PRIMERO
        btnIngresar = findViewById(R.id.btn_ingresar)
        edtEmail = findViewById(R.id.edt_email_login)
        edtPassword = findViewById(R.id.edt_password_login)
        tvRegister = findViewById(R.id.tv_registrarse)
        tvRecuperarContrasena = findViewById(R.id.tv_recuperar_contrasena) // Nueva vista
        btnGoogle = findViewById(R.id.btn_continuar_google)

        // Verificar si viene de un reset de contraseña exitoso DESPUÉS de inicializar vistas
        checkPasswordResetSuccess()

        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()

        // Crear el cliente de Google SignIn
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Configurar listeners
        btnGoogle.setOnClickListener {
            signIn()
        }

        btnIngresar.setOnClickListener {
            loginWithCredentials()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Nuevo listener para recuperar contraseña
        tvRecuperarContrasena.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Configurar listener para el botón "Done" del teclado en el campo de contraseña
        edtPassword.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                loginWithCredentials()
                true
            } else {
                false
            }
        }

        // Configurar listener para el botón "Next" del teclado en el campo de email
        edtEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtPassword.requestFocus()
                true
            } else {
                false
            }
        }
    }

    private fun checkPasswordResetSuccess() {
        // Verificar si viene de un cambio de contraseña exitoso
        if (intent.getBooleanExtra("password_reset_success", false)) {
            val email = intent.getStringExtra("email")
            Toast.makeText(
                this,
                "Contraseña cambiada exitosamente. Ya puedes iniciar sesión.",
                Toast.LENGTH_LONG
            ).show()

            // Pre-llenar el email si está disponible
            email?.let {
                edtEmail.setText(it)
                edtPassword.requestFocus()
            }
        }
    }

    private fun loginWithCredentials() {
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        // Validación básica
        if (email.isEmpty()) {
            edtEmail.error = "Por favor ingrese su email"
            edtEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            edtPassword.error = "Por favor ingrese su contraseña"
            edtPassword.requestFocus()
            return
        }

        // Verificar credenciales
        val user = userDao.validateCredentials(email, password)

        if (user != null) {
            // Guardar sesión
            sessionManager.createLoginSession(user)
            Toast.makeText(this, "Bienvenido ${user.name}", Toast.LENGTH_SHORT).show()
            // Redireccionar según el rol
            redirectBasedOnRole()
        } else {
            Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
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

            // Verificar si el usuario existe en la BD
            val email = account.email ?: ""
            var user = userDao.getUserByEmail(email)

            if (user == null) {
                // Crear nuevo usuario con rol USER por defecto
                user = User(
                    email = email,
                    name = account.displayName ?: "Usuario",
                    role = User.ROLE_USER,
                    profilePicUrl = account.photoUrl?.toString()
                )
                userDao.insertUser(user)
            }

            // Guardar sesión
            sessionManager.createLoginSession(user)

            Toast.makeText(this, "Bienvenido ${user.name}", Toast.LENGTH_SHORT).show()

            // Redireccionar según el rol
            redirectBasedOnRole()

        } catch (e: ApiException) {
            // Error en el inicio de sesión
            Log.w(TAG, "signInFailure:code=" + e.statusCode)
            Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectBasedOnRole() {
        // Independientemente del rol, ahora redirigimos al contenedor principal
        startActivity(Intent(this, MainContainerActivity::class.java))
        finish()
    }
}