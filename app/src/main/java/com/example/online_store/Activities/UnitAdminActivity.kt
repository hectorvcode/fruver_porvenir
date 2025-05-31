package com.example.online_store.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.UnitAdapter
import com.example.online_store.data.UnitDao
import com.example.online_store.model.Unit
import com.example.online_store.utils.RoleHelper
import com.example.online_store.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UnitAdminActivity : AppCompatActivity() {

    private lateinit var unitDao: UnitDao
    private lateinit var rvUnits: RecyclerView
    private lateinit var unitAdapter: UnitAdapter
    private lateinit var fabAddUnit: FloatingActionButton
    private lateinit var spCategoryFilter: Spinner
    private lateinit var spStatusFilter: Spinner
    private lateinit var sessionManager: SessionManager
    private lateinit var bottomNavigationView: BottomNavigationView

    private val categories = listOf("Todas") + Unit.CATEGORIES
    private val statusOptions = listOf("Todas", "Activas", "Inactivas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_admin)

        // Verificar permisos de administrador
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Inicializar SessionManager
        sessionManager = SessionManager(this)

        // Inicializar el DAO
        unitDao = UnitDao(this)

        // Configurar la ActionBar
        supportActionBar?.title = "Administración de Unidades"

        // Inicializar vistas
        rvUnits = findViewById(R.id.rv_units)
        fabAddUnit = findViewById(R.id.fab_add_unit)
        spCategoryFilter = findViewById(R.id.sp_category_filter)
        spStatusFilter = findViewById(R.id.sp_status_filter)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Configurar BottomNavigationView
        setupBottomNavigation()

        // Configurar spinners de filtro
        setupFilterSpinners()

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar FAB para añadir unidades
        fabAddUnit.setOnClickListener {
            val intent = Intent(this, UnitFormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Navegar al MainContainerActivity con el fragmento de profile
        val intent = Intent(this, MainContainerActivity::class.java)
        intent.putExtra("fragment", "profile")
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Verificar permisos de administrador cada vez que se vuelve a la actividad
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Actualizar la lista de unidades al volver a la actividad
        if (::unitAdapter.isInitialized) {
            loadUnits()
        }
    }

    private fun setupBottomNavigation() {
        // Configurar el listener para la navegación
        bottomNavigationView.setOnItemSelectedListener { item ->
            val intent = Intent(this, MainContainerActivity::class.java)
            when (item.itemId) {
                R.id.ic_home -> {
                    // No necesita extra, va al home por defecto
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_favorites -> {
                    intent.putExtra("fragment", "favorites")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_cart -> {
                    intent.putExtra("fragment", "cart")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_stores -> {
                    intent.putExtra("fragment", "stores")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.ic_profile -> {
                    intent.putExtra("fragment", "profile")
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFilterSpinners() {
        // Configurar spinner de categorías
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategoryFilter.adapter = categoryAdapter

        // Configurar spinner de estado
        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            statusOptions
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatusFilter.adapter = statusAdapter

        // Configurar listeners
        spCategoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (::unitAdapter.isInitialized) {
                    loadUnits()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (::unitAdapter.isInitialized) {
                    loadUnits()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        // Crear el adaptador
        unitAdapter = UnitAdapter(
            units = emptyList(),
            onEditClickListener = { unit ->
                // Abrir la actividad de edición
                val intent = Intent(this, UnitFormActivity::class.java)
                intent.putExtra("UNIT_ID", unit.id)
                startActivity(intent)
            },
            onDeleteClickListener = { unit ->
                // Mostrar diálogo de confirmación para eliminar
                showDeleteConfirmationDialog(unit)
            },
            onToggleActiveClickListener = { unit ->
                // Alternar estado activo/inactivo
                toggleUnitStatus(unit)
            }
        )

        // Configurar el RecyclerView
        rvUnits.apply {
            layoutManager = LinearLayoutManager(this@UnitAdminActivity)
            adapter = unitAdapter
        }

        // Cargar las unidades
        loadUnits()
    }

    private fun loadUnits() {
        // Obtener filtros seleccionados
        val selectedCategory = if (::spCategoryFilter.isInitialized && spCategoryFilter.selectedItem != null) {
            spCategoryFilter.selectedItem.toString()
        } else {
            "Todas"
        }

        val selectedStatus = if (::spStatusFilter.isInitialized && spStatusFilter.selectedItem != null) {
            spStatusFilter.selectedItem.toString()
        } else {
            "Todas"
        }

        // Obtener unidades según los filtros
        var units = unitDao.getAllUnits()

        // Aplicar filtros
        if (selectedCategory != "Todas") {
            units = units.filter { it.category == selectedCategory }
        }

        when (selectedStatus) {
            "Activas" -> units = units.filter { it.isActive }
            "Inactivas" -> units = units.filter { !it.isActive }
        }

        // Actualizar el adaptador
        if (::unitAdapter.isInitialized) {
            unitAdapter.updateUnits(units)

            // Mostrar mensaje si no hay unidades
            if (units.isEmpty()) {
                val message = when {
                    selectedCategory != "Todas" && selectedStatus != "Todas" ->
                        "No hay unidades $selectedStatus en la categoría $selectedCategory"
                    selectedCategory != "Todas" ->
                        "No hay unidades en la categoría $selectedCategory"
                    selectedStatus != "Todas" ->
                        "No hay unidades $selectedStatus"
                    else -> "No hay unidades registradas"
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmationDialog(unit: Unit) {
        // Verificar si la unidad está siendo utilizada
        if (unitDao.isUnitInUse(unit.abbreviation)) {
            AlertDialog.Builder(this)
                .setTitle("No se puede eliminar")
                .setMessage("La unidad '${unit.name}' está siendo utilizada por productos. No se puede eliminar.")
                .setPositiveButton("Entendido", null)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Eliminar Unidad")
            .setMessage("¿Estás seguro de eliminar la unidad '${unit.name} (${unit.abbreviation})'?\n\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteUnit(unit)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUnit(unit: Unit) {
        val result = unitDao.deleteUnit(unit.id)

        if (result > 0) {
            Toast.makeText(this, "Unidad eliminada correctamente", Toast.LENGTH_SHORT).show()
            // Recargar la lista
            loadUnits()
        } else {
            Toast.makeText(this, "Error al eliminar la unidad", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleUnitStatus(unit: Unit) {
        // Si se está desactivando una unidad que está en uso, mostrar advertencia
        if (unit.isActive && unitDao.isUnitInUse(unit.abbreviation)) {
            AlertDialog.Builder(this)
                .setTitle("Desactivar Unidad")
                .setMessage("La unidad '${unit.name}' está siendo utilizada por productos. ¿Estás seguro de desactivarla?\n\nLos productos seguirán manteniendo esta unidad, pero no se podrá seleccionar para nuevos productos.")
                .setPositiveButton("Desactivar") { _, _ ->
                    performToggleStatus(unit)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            performToggleStatus(unit)
        }
    }

    private fun performToggleStatus(unit: Unit) {
        val updatedUnit = unit.copy(isActive = !unit.isActive)
        val result = unitDao.updateUnit(updatedUnit)

        if (result > 0) {
            val statusText = if (updatedUnit.isActive) "activada" else "desactivada"
            Toast.makeText(this, "Unidad $statusText correctamente", Toast.LENGTH_SHORT).show()
            // Recargar la lista
            loadUnits()
        } else {
            Toast.makeText(this, "Error al cambiar el estado de la unidad", Toast.LENGTH_SHORT).show()
        }
    }
}