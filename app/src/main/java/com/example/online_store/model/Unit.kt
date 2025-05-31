package com.example.online_store.model

/**
 * Modelo para representar una unidad de medida
 */
data class Unit(
    val id: Int = 0,                    // ID único de la unidad
    val name: String,                   // Nombre de la unidad (ej: "libra", "kilogramo")
    val abbreviation: String,           // Abreviación (ej: "lb", "kg")
    val category: String,               // Categoría (Peso, Volumen, Cantidad)
    val conversionFactor: Double = 1.0, // Factor de conversión a unidad base
    val isActive: Boolean = true        // Si la unidad está activa
) {
    companion object {
        // Categorías de unidades
        const val CATEGORY_WEIGHT = "Peso"
        const val CATEGORY_VOLUME = "Volumen"
        const val CATEGORY_QUANTITY = "Cantidad"

        // Lista de categorías disponibles
        val CATEGORIES = listOf(CATEGORY_WEIGHT, CATEGORY_VOLUME, CATEGORY_QUANTITY)

        // Unidades predeterminadas del sistema
        fun getDefaultUnits(): List<Unit> {
            return listOf(
                Unit(
                    name = "Libra",
                    abbreviation = "lb",
                    category = CATEGORY_WEIGHT,
                    conversionFactor = 1.0
                ),
                Unit(
                    name = "Kilogramo",
                    abbreviation = "kg",
                    category = CATEGORY_WEIGHT,
                    conversionFactor = 2.20462
                ),
                Unit(
                    name = "Gramo",
                    abbreviation = "g",
                    category = CATEGORY_WEIGHT,
                    conversionFactor = 0.00220462
                ),
                Unit(
                    name = "Onza",
                    abbreviation = "oz",
                    category = CATEGORY_WEIGHT,
                    conversionFactor = 0.0625
                ),
                Unit(
                    name = "Litro",
                    abbreviation = "l",
                    category = CATEGORY_VOLUME,
                    conversionFactor = 1.0
                ),
                Unit(
                    name = "Mililitro",
                    abbreviation = "ml",
                    category = CATEGORY_VOLUME,
                    conversionFactor = 0.001
                ),
                Unit(
                    name = "Unidad",
                    abbreviation = "unidad",
                    category = CATEGORY_QUANTITY,
                    conversionFactor = 1.0
                ),
                Unit(
                    name = "Caja",
                    abbreviation = "caja",
                    category = CATEGORY_QUANTITY,
                    conversionFactor = 1.0
                ),
                Unit(
                    name = "Paquete",
                    abbreviation = "paquete",
                    category = CATEGORY_QUANTITY,
                    conversionFactor = 1.0
                )
            )
        }
    }

    /**
     * Obtiene el texto para mostrar en la interfaz
     */
    fun getDisplayText(): String {
        return "$name ($abbreviation)"
    }

    /**
     * Convierte una cantidad de esta unidad a la unidad base de la categoría
     */
    fun convertToBase(quantity: Double): Double {
        return quantity * conversionFactor
    }

    /**
     * Convierte una cantidad de la unidad base a esta unidad
     */
    fun convertFromBase(baseQuantity: Double): Double {
        return if (conversionFactor != 0.0) {
            baseQuantity / conversionFactor
        } else {
            0.0
        }
    }
}