package com.example.online_store.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.Unit

class UnitAdapter(
    private var units: List<Unit>,
    private val onEditClickListener: (Unit) -> kotlin.Unit,
    private val onDeleteClickListener: (Unit) -> kotlin.Unit,
    private val onToggleActiveClickListener: (Unit) -> kotlin.Unit
) : RecyclerView.Adapter<UnitAdapter.UnitViewHolder>() {

    class UnitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUnitName: TextView = itemView.findViewById(R.id.tv_unit_name)
        val tvUnitAbbreviation: TextView = itemView.findViewById(R.id.tv_unit_abbreviation)
        val tvUnitCategory: TextView = itemView.findViewById(R.id.tv_unit_category)
        val tvUnitConversionFactor: TextView = itemView.findViewById(R.id.tv_unit_conversion_factor)
        val tvUnitStatus: TextView = itemView.findViewById(R.id.tv_unit_status)
        val ivEdit: ImageView = itemView.findViewById(R.id.iv_edit)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete)
        val ivToggleActive: ImageView = itemView.findViewById(R.id.iv_toggle_active)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unit, parent, false)
        return UnitViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        val unit = units[position]

        // Configurar los datos de la unidad en la vista
        holder.tvUnitName.text = unit.name
        holder.tvUnitAbbreviation.text = unit.abbreviation
        holder.tvUnitCategory.text = "Categoría: ${unit.category}"

        // Mostrar factor de conversión solo si es diferente de 1.0
        if (unit.conversionFactor != 1.0) {
            holder.tvUnitConversionFactor.text = "Factor: ${String.format("%.4f", unit.conversionFactor)}"
            holder.tvUnitConversionFactor.visibility = View.VISIBLE
        } else {
            holder.tvUnitConversionFactor.visibility = View.GONE
        }

        // Configurar estado (activo/inactivo)
        if (unit.isActive) {
            holder.tvUnitStatus.text = "Activa"
            holder.tvUnitStatus.setTextColor(holder.itemView.context.getColor(R.color.green_mint))
            holder.ivToggleActive.setImageResource(R.drawable.baseline_visibility_24)
            holder.itemView.alpha = 1.0f
        } else {
            holder.tvUnitStatus.text = "Inactiva"
            holder.tvUnitStatus.setTextColor(holder.itemView.context.getColor(R.color.gray))
            holder.ivToggleActive.setImageResource(R.drawable.baseline_visibility_off_24)
            holder.itemView.alpha = 0.6f
        }

        // Configurar listeners para los botones
        holder.ivEdit.setOnClickListener {
            onEditClickListener(unit)
        }

        holder.ivDelete.setOnClickListener {
            onDeleteClickListener(unit)
        }

        holder.ivToggleActive.setOnClickListener {
            onToggleActiveClickListener(unit)
        }

        // Configurar colores según la categoría
        val categoryColorRes = when (unit.category) {
            Unit.CATEGORY_WEIGHT -> R.color.green_mint
            Unit.CATEGORY_VOLUME -> R.color.purple
            Unit.CATEGORY_QUANTITY -> R.color.orange
            else -> R.color.gray
        }

        val categoryColor = holder.itemView.context.getColor(categoryColorRes)

        // Aplicar un fondo sutil según la categoría
        when (unit.category) {
            Unit.CATEGORY_WEIGHT -> {
                holder.itemView.setBackgroundResource(R.drawable.card_background_weight)
            }
            Unit.CATEGORY_VOLUME -> {
                holder.itemView.setBackgroundResource(R.drawable.card_background_volume)
            }
            Unit.CATEGORY_QUANTITY -> {
                holder.itemView.setBackgroundResource(R.drawable.card_background_quantity)
            }
            else -> {
                holder.itemView.setBackgroundResource(R.drawable.card_background_default)
            }
        }
    }

    override fun getItemCount(): Int = units.size

    // Método para actualizar la lista de unidades
    fun updateUnits(newUnits: List<Unit>) {
        units = newUnits
        notifyDataSetChanged()
    }

    // Método para obtener una unidad por posición
    fun getUnitAt(position: Int): Unit? {
        return if (position in 0 until units.size) {
            units[position]
        } else {
            null
        }
    }

    // Método para filtrar unidades por categoría
    fun filterByCategory(category: String?) {
        // Este método se puede implementar si necesitas filtrado en tiempo real
        // Por ahora, el filtrado se maneja en la actividad
    }

    // Método para filtrar unidades por estado (activa/inactiva)
    fun filterByStatus(activeOnly: Boolean) {
        // Este método se puede implementar si necesitas filtrado en tiempo real
        // Por ahora, el filtrado se maneja en la actividad
    }
}
