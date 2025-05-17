package com.example.online_store.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.online_store.R

class DetalleProductoFragment : Fragment() {

    private lateinit var producto: Producto

    companion object {
        private const val ARG_PRODUCTO = "producto"

        fun newInstance(producto: Producto): DetalleProductoFragment {
            val fragment = DetalleProductoFragment()
            val args = Bundle()
            args.putParcelable(ARG_PRODUCTO, producto)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Verificar si los argumentos no son nulos
        if(arguments != null){
            //A partir de Android 13 (API 33) hay que usar getParcelable()
            producto = if (android.os.Build.VERSION.SDK_INT >= 33){
                arguments?.getParcelable(ARG_PRODUCTO, Producto::class.java)
                    ?: Producto(0,"Sin nombre", "Sin descripción", 0.0, R.drawable.placeholder)
            } else {
                @Suppress("DEPRECATION")
                arguments?.getParcelable(ARG_PRODUCTO) ?: Producto(0,"Sin nombre", "Sin descripción", 0.0, R.drawable.placeholder)
            }
        } else {
            //Si no hay argumentos, crear un objeto Producto vacío
            producto = Producto(0,"Sin nombre", "Sin descripción", 0.0, R.drawable.placeholder)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_detalle_producto, container, false)

        val imagenProducto = view.findViewById<ImageView>(R.id.detalle_imagen_producto)
        val nombreProducto = view.findViewById<TextView>(R.id.detalle_nombre_producto)
        val precioProducto = view.findViewById<TextView>(R.id.detalle_precio_producto)
        val descripcionProducto = view.findViewById<TextView>(R.id.detalle_descripcion_producto)
        val botonComprar = view.findViewById<Button>(R.id.btn_comprar)

        imagenProducto.setImageResource(producto.imagen)
        nombreProducto.text = producto.nombre
        precioProducto.text = "Precio: $${producto.precio}"
        descripcionProducto.text = producto.descripcion

        botonComprar.setOnClickListener {
            //Logica para agregar al carrito
            Toast.makeText(requireContext(), "Producto agregado al carrito", Toast.LENGTH_SHORT).show()

        }

        return view

    }
}