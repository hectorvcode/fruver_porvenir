package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.UserAdapter
import com.example.online_store.data.UserDao
import com.example.online_store.model.User
import com.example.online_store.utils.RoleHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserAdminActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var fabAddUser: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_admin)

        // Verificar permisos de administrador
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Inicializar el DAO
        userDao = UserDao(this)

        // Configurar la ActionBar
        supportActionBar?.title = "Administración de Usuarios"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializar vistas
        rvUsers = findViewById(R.id.rv_users)
        fabAddUser = findViewById(R.id.fab_add_user)

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar FAB para añadir usuarios
        fabAddUser.setOnClickListener {
            val intent = Intent(this, UserFormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista de usuarios al volver a la actividad
        if (::userAdapter.isInitialized) {
            loadUsers()
        }
    }

    private fun setupRecyclerView() {
        // Crear el adaptador
        userAdapter = UserAdapter(
            users = emptyList(),
            onEditClickListener = { user ->
                // Abrir la actividad de edición
                val intent = Intent(this, UserFormActivity::class.java)
                intent.putExtra("USER_EMAIL", user.email)
                startActivity(intent)
            },
            onDeleteClickListener = { user ->
                // Mostrar diálogo de confirmación
                showDeleteConfirmationDialog(user)
            }
        )

        // Configurar el RecyclerView
        rvUsers.apply {
            layoutManager = LinearLayoutManager(this@UserAdminActivity)
            adapter = userAdapter
        }

        // Cargar los usuarios
        loadUsers()
    }

    private fun loadUsers() {
        val users = userDao.getAllUsers()
        userAdapter.updateUsers(users)

        // Mostrar mensaje si no hay usuarios
        if (users.isEmpty()) {
            Toast.makeText(
                this,
                "No hay usuarios registrados",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmationDialog(user: User) {
        // No permitir eliminar al usuario actual (admin que está usando la app)
        if (user.email == "admin@example.com") {
            Toast.makeText(this, "No puedes eliminar el usuario administrador principal", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de eliminar el usuario ${user.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Eliminar el usuario
                val result = userDao.deleteUser(user.email)

                if (result > 0) {
                    Toast.makeText(this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show()
                    // Recargar la lista
                    loadUsers()
                } else {
                    Toast.makeText(this, "Error al eliminar el usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}