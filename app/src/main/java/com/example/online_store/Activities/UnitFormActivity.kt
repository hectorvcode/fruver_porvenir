package com.example.online_store.Activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.online_store.R
import com.example.online_store.data.UnitDao
import com.example.online_store.model.Unit
import com.example.online_store.utils.RoleHelper
import com.google.android.material.textfield.TextInputEditText

class UnitFormActivity : AppCompatActivity() {

    private lateinit var unitDao: UnitDao

    private lateinit var tvTitle: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etAbbreviation: TextInputEditText
    private lateinit var actCategory: AutoCompleteTextView
    private lateinit var etConversionFactor: TextInputEditText
    private lateinit var cbIsActive: CheckBox
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var unitId = 0 // 0 indica nueva unidad, otro valor indica edición
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unit_form)

        // Verificar permisos de administrador
        if (!RoleHelper.checkAdminPermission(this)) {
            return
        }

        // Configurar la ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Inicializar el DAO
        unitDao = UnitDao(this)

        // Inicializar vistas
        initializeViews()

        // Configurar el selector de categorías
        setupCategorySelector()

        // Verificar si estamos en modo edición
        checkEditMode()

        // Configurar listeners
        setupListeners()
    }

    private fun initializeViews() {
        tvTitle = findViewById(R.id.tv_title)
        etName = findViewById(R.id.et_name)
        etAbbreviation = findViewById(R.id.et_abbreviation)
        actCategory = findViewById(R.id.act_category)
        etConversionFactor = findViewById(R.id.et_conversion_factor)
        cbIsActive = findViewById(R.id.cb_is_active)
        btnSave = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)
    }

    private fun setupCategorySelector() {
        // Configurar el adaptador para el selector de categorías
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, Unit.CATEGORIES)
        actCategory.setAdapter(adapter)

        // Establecer categoría por defecto
        actCategory.setText(Unit.CATEGORY_WEIGHT, false)
    }

    private fun checkEditMode() {
        if (intent.hasExtra("UNIT_ID")) {
            unitId = intent.getIntExtra("UNIT_ID", 0)
            isEditMode = true
            supportActionBar?.title = "Editar Unidad"
            tvTitle.text = "Editar Unidad"
            loadUnitData(unitId)
        } else {
            supportActionBar?.title = "Nueva Unidad"
            tvTitle.text = "Nueva Unidad"
            // Establecer valores por defecto para nueva unidad
            etConversionFactor.setText("1.0")
            cbIsActive.isChecked = true
        }
    }

    private fun setupListeners() {
        btnSave.setOnClickListener {
            saveUnit()
        }

        btnCancel.setOnClickListener {
            finish() // Volver a la actividad anterior
        }
    }

    private fun loadUnitData(unitId: Int) {
        // Cargar los datos de la unidad para edición
        val unit = unitDao.getUnitById(unitId)

        unit?.let {
            // Llenar el formulario con los datos de la unidad
            etName.setText(it.name)
            etAbbreviation.setText(it.abbreviation)
            actCategory.setText(it.category, false)
            etConversionFactor.setText(it.conversionFactor.toString())
            cbIsActive.isChecked = it.isActive
        } ?: run {
            Toast.makeText(this, "No se encontró la unidad", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveUnit() {
        // Validar el formulario
        if (!validateForm()) {
            return
        }

        // Obtener los valores del formulario
        val name = etName.text.toString().trim()
        val abbreviation = etAbbreviation.text.toString().trim()
        val category = actCategory.text.toString().trim()
        val conversionFactor = etConversionFactor.text.toString().toDoubleOrNull() ?: 1.0
        val isActive = cbIsActive.isChecked

        // Crear el objeto unidad
        val unit = Unit(
            id = if (isEditMode) unitId else 0,
            name = name,
            abbreviation = abbreviation,
            category = category,
            conversionFactor = conversionFactor,
            isActive = isActive
        )

        // Guardar en la base de datos
        if (isEditMode) {
            // Actualizar
            val rowsAffected = unitDao.updateUnit(unit)

            if (rowsAffected > 0) {
                Toast.makeText(this, "Unidad actualizada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar la unidad", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Insertar nueva
            val id = unitDao.insertUnit(unit)

            when {
                id > 0 -> {
                    Toast.makeText(this, "Unidad agregada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                id == -1L -> {
                    Toast.makeText(this, "Ya existe una unidad con esa abreviación", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "Error al agregar la unidad", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validar nombre
        if (etName.text.toString().trim().isEmpty()) {
            etName.error = "El nombre es obligatorio"
            isValid = false
        }

        // Validar abreviación
        val abbreviation = etAbbreviation.text.toString().trim()
        if (abbreviation.isEmpty()) {
            etAbbreviation.error = "La abreviación es obligatoria"
            isValid = false
        } else if (abbreviation.length > 10) {
            etAbbreviation.error = "La abreviación no puede tener más de 10 caracteres"
            isValid = false
        } else {
            // Verificar si la abreviación ya existe (solo para nueva unidad o si cambió)
            val existingUnit = unitDao.getUnitByAbbreviation(abbreviation)
            if (existingUnit != null && (!isEditMode || existingUnit.id != unitId)) {
                etAbbreviation.error = "Ya existe una unidad con esta abreviación"
                isValid = false
            }
        }

        // Validar categoría
        val category = actCategory.text.toString().trim()
        if (category.isEmpty()) {
            actCategory.error = "Seleccione una categoría"
            isValid = false
        } else if (!Unit.CATEGORIES.contains(category)) {
            actCategory.error = "Seleccione una categoría válida"
            isValid = false
        }

        // Validar factor de conversión
        val conversionFactorStr = etConversionFactor.text.toString().trim()
        if (conversionFactorStr.isEmpty()) {
            etConversionFactor.error = "El factor de conversión es obligatorio"
            isValid = false
        } else {
            val conversionFactor = conversionFactorStr.toDoubleOrNull()
            if (conversionFactor == null) {
                etConversionFactor.error = "Ingrese un número válido"
                isValid = false
            } else if (conversionFactor <= 0) {
                etConversionFactor.error = "El factor debe ser mayor que cero"
                isValid = false
            }
        }

        return isValid
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
}