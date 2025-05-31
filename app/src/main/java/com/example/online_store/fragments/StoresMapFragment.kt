package com.example.online_store.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.online_store.R
import com.example.online_store.adapter.StoreAdapter
import com.example.online_store.data.StoreRepository
import com.example.online_store.model.Store
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class StoresMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storeRepository: StoreRepository
    private lateinit var rvStoresList: RecyclerView
    private lateinit var storeAdapter: StoreAdapter

    private val stores = mutableListOf<Store>()
    private val markers = mutableMapOf<Int, Marker>() // Mapeo de ID de tienda a su marcador
    private var userLocation: LatLng? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        fun newInstance(): StoresMapFragment {
            return StoresMapFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stores_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializaciones
        storeRepository = StoreRepository()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        rvStoresList = view.findViewById(R.id.rv_stores_list)

        // Inicializar el mapa
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar RecyclerView
        setupRecyclerView()

        // Cargar tiendas
        loadStores()
    }

    private fun setupRecyclerView() {
        storeAdapter = StoreAdapter(
            stores = emptyList(),
            onStoreClickListener = { store ->
                // Mantener la funcionalidad existente para mostrar detalles cuando se toca la tarjeta
                showStoreDetails(store)
            },
            onMapButtonClickListener = { store ->
                // Centrar el mapa en esta tienda
                centerMapOnStore(store)
            },
            onDetailsButtonClickListener = { store ->
                // Nueva funcionalidad: mostrar detalles cuando se presiona el botón
                showStoreDetails(store)
            }
        )

        rvStoresList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storeAdapter
        }
    }

    private fun loadStores() {
        // Cargar todas las tiendas
        val allStores = storeRepository.getStores()
        stores.clear()
        stores.addAll(allStores)
        storeAdapter.updateStores(stores)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar el mapa
        mMap.uiSettings.isZoomControlsEnabled = true

        // Solicitar permisos de ubicación si es necesario
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Habilitar botón de "Mi ubicación"
        mMap.isMyLocationEnabled = true

        // Obtener ubicación actual
        getUserLocation()

        // Añadir marcadores para todas las tiendas
        addStoreMarkers()
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                userLocation = LatLng(it.latitude, it.longitude)

                // Actualizar distancias en la lista de tiendas
                storeAdapter.updateDistances(it.latitude, it.longitude)

                // Ajustar la cámara para incluir la ubicación del usuario y las tiendas
                adjustCameraBounds()
            }
        }
    }

    private fun addStoreMarkers() {
        // Limpiar marcadores existentes
        markers.values.forEach { it.remove() }
        markers.clear()

        // Añadir marcadores para cada tienda
        stores.forEach { store ->
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(store.location)
                    .title(store.name)
                    .snippet(store.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            marker?.let {
                markers[store.id] = it
            }
        }

        // Ajustar la cámara para mostrar todos los marcadores
        adjustCameraBounds()
    }

    private fun adjustCameraBounds() {
        if (stores.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()

        // Incluir todas las tiendas
        stores.forEach { store ->
            boundsBuilder.include(store.location)
        }

        // Incluir la ubicación del usuario si está disponible
        userLocation?.let {
            boundsBuilder.include(it)
        }

        // Mover la cámara para mostrar todos los puntos
        try {
            val bounds = boundsBuilder.build()
            val padding = 100 // Píxeles de padding alrededor de los límites
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

            mMap.animateCamera(cameraUpdate)
        } catch (e: Exception) {
            // Si hay algún error (por ejemplo, sólo hay un punto), centrar en Colombia
            val colombia = LatLng(4.6097, -74.0817) // Coordenadas aproximadas de Colombia
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombia, 5f))
        }
    }

    private fun centerMapOnStore(store: Store) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(store.location, 15f)
        )

        // Mostrar la info window del marcador
        markers[store.id]?.showInfoWindow()
    }

    private fun showStoreDetails(store: Store) {
        // Navegar al fragmento de detalles de la tienda
        val detailFragment = StoreDetailFragment.newInstance(store.id)

        val containerId = if (activity?.findViewById<View>(R.id.fragment_container) != null) {
            R.id.fragment_container  // MainContainerActivity
        } else {
            R.id.frame_layout       // ListActivity
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(containerId, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido, inicializar el mapa nuevamente
                    onMapReady(mMap)
                } else {
                    // Permiso denegado, mostrar mensaje
                    Toast.makeText(
                        requireContext(),
                        "Se necesitan permisos de ubicación para mostrar tu posición en el mapa",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            else -> {
                // Ignorar otros códigos de solicitud
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}