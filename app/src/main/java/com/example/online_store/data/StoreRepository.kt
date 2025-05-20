package com.example.online_store.data

import com.example.online_store.R
import com.example.online_store.model.Store
import com.google.android.gms.maps.model.LatLng

class StoreRepository {

    // Lista de tiendas en Colombia (datos de ejemplo)
    fun getStores(): List<Store> {
        return listOf(
            Store(
                id = 1,
                name = "Fruver Porvenir - Sede Principal",
                address = "Calle 75A Sur #14-45, Bogotá",
                location = LatLng(4.51386153350474, -74.11549107408243),
                phone = "+57 601 234 5678",
                openHours = "Lun-Sáb: 8:00 AM - 8:00 PM, Dom: 9:00 AM - 6:00 PM",
                imageResource = R.drawable.app_logo
            ),
            Store(
                id = 2,
                name = "Fruver Porvenir - Medellín",
                address = "Carrera 45 #49a-08, Medellín",
                location = LatLng(6.2476, -75.5658), // Coordenadas para Medellín
                phone = "+57 604 234 5678",
                openHours = "Lun-Sáb: 8:00 AM - 8:00 PM, Dom: 9:00 AM - 6:00 PM",
                imageResource = R.drawable.app_logo
            ),
            Store(
                id = 3,
                name = "Fruver Porvenir - Cali",
                address = "Avenida 5 #12-18, Cali",
                location = LatLng(3.4516, -76.5320), // Coordenadas para Cali
                phone = "+57 602 234 5678",
                openHours = "Lun-Sáb: 8:00 AM - 8:00 PM, Dom: 9:00 AM - 6:00 PM",
                imageResource = R.drawable.app_logo
            )
        )
    }

    // Obtener una tienda por su ID
    fun getStoreById(storeId: Int): Store? {
        return getStores().find { it.id == storeId }
    }

    // Obtener tiendas cercanas a una ubicación (simulado)
    fun getNearbyStores(userLat: Double, userLng: Double, radiusKm: Double = 10.0): List<Store> {
        // Implementación simple - en una aplicación real usarías cálculos de distancia más precisos
        val userLocation = LatLng(userLat, userLng)

        return getStores().filter { store ->
            calculateDistance(
                userLocation.latitude, userLocation.longitude,
                store.location.latitude, store.location.longitude
            ) <= radiusKm
        }
    }

    // Cálculo básico de distancia usando la fórmula de Haversine
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