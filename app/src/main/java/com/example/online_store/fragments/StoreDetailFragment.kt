package com.example.online_store.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.online_store.R
import com.example.online_store.data.StoreRepository
import com.example.online_store.model.Store
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StoreDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var storeRepository: StoreRepository
    private lateinit var store: Store

    private lateinit var tvStoreName: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvHours: TextView
    private lateinit var btnCall: Button
    private lateinit var btnDirections: Button

    companion object {
        private const val ARG_STORE_ID = "store_id"

        fun newInstance(storeId: Int): StoreDetailFragment {
            val fragment = StoreDetailFragment()
            val args = Bundle()
            args.putInt(ARG_STORE_ID, storeId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el repositorio
        storeRepository = StoreRepository()

        // Obtener el ID de la tienda de los argumentos
        val storeId = arguments?.getInt(ARG_STORE_ID, 0) ?: 0

        // Obtener la tienda por su ID
        store = storeRepository.getStoreById(storeId) ?: run {
            // Si no se encuentra la tienda, volver atrás
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        // Inicializar vistas
        tvStoreName = view.findViewById(R.id.tv_store_name)
        tvAddress = view.findViewById(R.id.tv_address)
        tvPhone = view.findViewById(R.id.tv_phone)
        tvHours = view.findViewById(R.id.tv_hours)
        btnCall = view.findViewById(R.id.btn_call)
        btnDirections = view.findViewById(R.id.btn_directions)

        // Configurar el mapa
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_detail) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Mostrar información de la tienda
        displayStoreInfo()

        // Configurar botones
        setupButtons()
    }

    private fun displayStoreInfo() {
        tvStoreName.text = store.name
        tvAddress.text = store.address
        tvPhone.text = store.phone
        tvHours.text = store.openHours
    }

    private fun setupButtons() {
        // Botón de llamada
        btnCall.setOnClickListener {
            callStore(store)
        }

        // Botón de indicaciones
        btnDirections.setOnClickListener {
            openDirections(store)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Log para depuración
        Log.d("StoreDetailFragment", "onMapReady - Store location: ${store.location.latitude}, ${store.location.longitude}")

        // Configurar el tipo de mapa (NORMAL muestra calles y edificios)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Configurar controles del mapa
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        // Añadir marcador para la tienda
        val storePosition = store.location
        mMap.addMarker(
            MarkerOptions()
                .position(storePosition)
                .title(store.name)
        )

        // Mover cámara a la ubicación de la tienda con un zoom adecuado (15-18 es para nivel de calle)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(storePosition, 16f))
    }

    private fun callStore(store: Store) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${store.phone}")
        startActivity(intent)
    }

    private fun openDirections(store: Store) {
        // Crear una URI para abrir Google Maps con indicaciones a la tienda
        val uri = "google.navigation:q=${store.location.latitude},${store.location.longitude}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps") // Especificar que se abra con Google Maps

        // Verificar si Google Maps está instalado
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            // Si Google Maps no está instalado, abrir en el navegador
            val webUri = "https://www.google.com/maps/dir/?api=1&destination=${store.location.latitude},${store.location.longitude}"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
            startActivity(webIntent)
        }
    }
}