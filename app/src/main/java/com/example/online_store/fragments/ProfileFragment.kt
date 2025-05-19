package com.example.online_store.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.online_store.Activities.LoginActivity
import com.example.online_store.Activities.ProductAdminActivity
import com.example.online_store.Activities.UserAdminActivity
import com.example.online_store.R
import com.example.online_store.utils.SessionManager

class ProfileFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var switchAdminMode: SwitchCompat
    private lateinit var tvAdminModeExplanation: TextView
    private lateinit var btnAdminProducts: Button
    private lateinit var btnLogout: Button
    private lateinit var sessionManager: SessionManager
    private lateinit var btnAdminUsers: Button

    private var isAdmin = false
    private var canBecomeAdmin = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar SessionManager
        sessionManager = SessionManager(requireContext())

        // Inicializar vistas
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserEmail = view.findViewById(R.id.tv_user_email)
        tvUserRole = view.findViewById(R.id.tv_user_role)
        switchAdminMode = view.findViewById(R.id.switch_admin_mode)
        tvAdminModeExplanation = view.findViewById(R.id.tv_admin_mode_explanation)
        btnAdminProducts = view.findViewById(R.id.btn_admin_products)
        btnLogout = view.findViewById(R.id.btn_logout)
        btnAdminUsers = view.findViewById(R.id.btn_admin_users)

        // Cargar datos del usuario
        loadUserData()

        // Configurar switch de modo administrador
        setupAdminModeSwitch()

        // Configurar botón de administración de productos
        setupAdminButtons()

        // Configurar botón de cierre de sesión
        btnLogout.setOnClickListener {
            sessionManager.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar datos al volver al fragmento
        loadUserData()
    }

    private fun loadUserData() {
        // Obtener datos del usuario desde SessionManager
        val userDetails = sessionManager.getUserDetails()

        // Mostrar información del usuario
        tvUserName.text = userDetails["name"] ?: "Usuario"
        tvUserEmail.text = userDetails["email"] ?: ""

        // Verificar si el usuario puede ser administrador
        isAdmin = sessionManager.isAdmin()
        canBecomeAdmin = sessionManager.isAdmin() // Simplificado para este ejemplo

        // Mostrar rol del usuario
        updateRoleText()

        // Configurar visibilidad del switch según si puede ser admin
        switchAdminMode.visibility = if (canBecomeAdmin) View.VISIBLE else View.GONE
        tvAdminModeExplanation.visibility = if (canBecomeAdmin) View.VISIBLE else View.GONE

        // Configurar estado inicial del switch
        switchAdminMode.isChecked = isAdmin

        // Actualizar visibilidad del botón de administración
        updateAdminFeaturesVisibility()
    }

    private fun updateRoleText() {
        tvUserRole.text = if (isAdmin) "Rol: Administrador" else "Rol: Usuario"
    }

    private fun updateAdminFeaturesVisibility() {
        // Mostrar u ocultar los botones de administración según el rol
        btnAdminProducts.visibility = if (isAdmin) View.VISIBLE else View.GONE
        btnAdminUsers.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    private fun setupAdminModeSwitch() {
        switchAdminMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked == isAdmin) {
                // No ha cambiado realmente, evitamos trabajo innecesario
                return@setOnCheckedChangeListener
            }

            // Cambiar el rol en la sesión
            val newRole = if (isChecked) "ADMIN" else "USER"
            sessionManager.updateUserRole(newRole)

            // Actualizar variable local
            isAdmin = isChecked

            // Actualizar la interfaz
            updateRoleText()
            updateAdminFeaturesVisibility()

            // Mostrar mensaje
            val message = if (isChecked)
                "Modo administrador activado"
            else
                "Modo usuario regular activado"

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdminButtons() {
        btnAdminProducts.setOnClickListener {
            // Navegar a la pantalla de administración de productos
            startActivity(Intent(requireContext(), ProductAdminActivity::class.java))
        }

        btnAdminUsers.setOnClickListener {
            startActivity(Intent(requireContext(), UserAdminActivity::class.java))
        }
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}