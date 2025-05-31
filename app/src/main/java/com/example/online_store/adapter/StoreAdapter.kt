package com.example.online_store.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.model.Store

class StoreAdapter(
    private var stores: List<Store>,
    private val onStoreClickListener: (Store) -> Unit,
    private val onMapButtonClickListener: (Store) -> Unit,
    private val onDetailsButtonClickListener: (Store) -> Unit // Nueva función para el botón de detalles
) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivStoreImage: ImageView = itemView.findViewById(R.id.iv_store_image)
        val tvStoreName: TextView = itemView.findViewById(R.id.tv_store_name)
        val tvStoreAddress: TextView = itemView.findViewById(R.id.tv_store_address)
        val tvStoreDistance: TextView = itemView.findViewById(R.id.tv_store_distance)
        val btnViewOnMap: Button = itemView.findViewById(R.id.btn_view_on_map)
        val btnViewDetails: Button = itemView.findViewById(R.id.btn_view_details) // Nuevo botón
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val store = stores[position]

        holder.tvStoreName.text = store.name
        holder.tvStoreAddress.text = store.address
        // Temporalmente dejamos la distancia vacía, se actualizará cuando tengamos la ubicación del usuario
        holder.tvStoreDistance.visibility = View.GONE

        // Configurar imagen
        store.imageResource?.let {
            holder.ivStoreImage.setImageResource(it)
        } ?: run {
            holder.ivStoreImage.setImageResource(R.drawable.app_logo)
        }

        // Configurar listeners
        holder.itemView.setOnClickListener {
            onStoreClickListener(store)
        }

        // Botón para ver en el mapa
        holder.btnViewOnMap.setOnClickListener {
            onMapButtonClickListener(store)
        }

        // Nuevo botón para ver detalles
        holder.btnViewDetails.setOnClickListener {
            onDetailsButtonClickListener(store)
        }
    }

    override fun getItemCount(): Int = stores.size

    fun updateStores(newStores: List<Store>) {
        stores = newStores
        notifyDataSetChanged()
    }

    // Actualiza las distancias de las tiendas desde la ubicación del usuario
    fun updateDistances(userLat: Double, userLng: Double) {
        for (position in stores.indices) {
            val holder = getViewHolderAt(position) ?: continue
            val store = stores[position]

            val distance = calculateDistance(
                userLat, userLng,
                store.location.latitude, store.location.longitude
            )

            holder.tvStoreDistance.text = "A ${String.format("%.1f", distance)} km de distancia"
            holder.tvStoreDistance.visibility = View.VISIBLE
        }
    }

    private fun getViewHolderAt(position: Int): StoreViewHolder? {
        return null // Esta es una implementación simplificada, necesitarías una manera de obtener el ViewHolder
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radio de la tierra en km

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return r * c
    }
}