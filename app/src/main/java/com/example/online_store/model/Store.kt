package com.example.online_store.model

import com.google.android.gms.maps.model.LatLng

data class Store(
    val id: Int,
    val name: String,
    val address: String,
    val location: LatLng,  // Latitud y longitud
    val phone: String,
    val openHours: String,
    val imageResource: Int? = null
)
