package com.bodegapp.app.ui.tienda

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bodegapp.app.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MiTiendaFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    data class Mayorista(
        val nombre: String,
        val tipo: String,
        val distancia: String,
        val estado: String
    )

    // Mayoristas fijos de ejemplo
    private val mayoristasBase = listOf(
        Mayorista("Makro Breña", "Mayorista", "1.1 km", "Abierto"),
        Mayorista("Metro La Marina", "Supermercado", "2.4 km", "Abierto"),
        Mayorista("Dist. Allcorp", "Distribuidor", "3.1 km", "Cierra 6pm")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mi_tienda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        view.findViewById<View>(R.id.btnGuardarTienda).setOnClickListener {
            Toast.makeText(requireContext(), "Datos de la tienda guardados", Toast.LENGTH_SHORT).show()
        }

        mostrarMayoristas(view)
        mostrarFavoritos(view)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val tienda = LatLng(-12.0565, -77.0490)
        mMap?.addMarker(
            MarkerOptions().position(tienda).title("Mi Tienda").snippet("Bodega Don Carlos")
        )
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(tienda, 16f))
        mMap?.uiSettings?.isZoomControlsEnabled = true

        // Al tocar el mapa, pide nombre y lo guarda como mayorista
        mMap?.setOnMapClickListener { latLng ->
            mostrarDialogoGuardarUbicacion(latLng)
        }
    }

    private fun mostrarDialogoGuardarUbicacion(latLng: LatLng) {
        val contexto = requireContext()
        val dialogView = LinearLayout(contexto).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
        }

        val etNombre = EditText(contexto).apply { hint = "Nombre del mayorista" }
        val etTipo = EditText(contexto).apply { hint = "Tipo (Mayorista, Supermercado...)" }

        dialogView.addView(etNombre)
        dialogView.addView(etTipo)

        AlertDialog.Builder(contexto)
            .setTitle("Guardar ubicación")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString().trim()
                val tipo = etTipo.text.toString().trim()

                if (nombre.isEmpty()) {
                    Toast.makeText(contexto, "Escribe un nombre", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Agregar marcador en el mapa
                mMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(nombre)
                        .snippet(tipo)
                )

                // Guardar en SharedPreferences
                val nuevo = Mayorista(
                    nombre = nombre,
                    tipo = tipo.ifEmpty { "Mayorista" },
                    distancia = calcularDistancia(latLng),
                    estado = "Abierto"
                )
                guardarMayoristaPersonalizado(nuevo)

                // Refrescar lista
                view?.let {
                    mostrarMayoristas(it)
                    mostrarFavoritos(it)
                }

                Toast.makeText(contexto, "$nombre guardado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun calcularDistancia(latLng: LatLng): String {
        val tienda = android.location.Location("").apply {
            latitude = -12.0565; longitude = -77.0490
        }
        val punto = android.location.Location("").apply {
            latitude = latLng.latitude; longitude = latLng.longitude
        }
        val metros = tienda.distanceTo(punto)
        return if (metros < 1000) "${metros.toInt()} m" else "${"%.1f".format(metros / 1000)} km"
    }

    private fun mostrarMayoristas(view: View) {
        val contenedor = view.findViewById<LinearLayout>(R.id.contenedorMayoristas)
        contenedor.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        val favoritos = obtenerFavoritos()

        // Combinamos los base + los personalizados guardados
        val todos = mayoristasBase + obtenerMayoristasPersonalizados()

        for (m in todos) {
            val item = inflater.inflate(R.layout.item_mayorista, contenedor, false)
            item.findViewById<TextView>(R.id.tvNombreMayorista).text = m.nombre
            item.findViewById<TextView>(R.id.tvTipoMayorista).text = m.tipo
            item.findViewById<TextView>(R.id.tvDistanciaMayorista).text = m.distancia
            item.findViewById<TextView>(R.id.tvEstadoMayorista).text = m.estado

            val btnFav = item.findViewById<TextView>(R.id.btnGuardarFavorito)
            btnFav.text = if (favoritos.contains(m.nombre)) "★" else "☆"

            btnFav.setOnClickListener {
                val favs = obtenerFavoritos().toMutableSet()
                if (favs.contains(m.nombre)) {
                    favs.remove(m.nombre)
                    btnFav.text = "☆"
                    Toast.makeText(requireContext(), "${m.nombre} eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    favs.add(m.nombre)
                    btnFav.text = "★"
                    Toast.makeText(requireContext(), "${m.nombre} guardado", Toast.LENGTH_SHORT).show()
                }
                guardarFavoritos(favs)
                mostrarFavoritos(view)
            }
            contenedor.addView(item)
        }
    }

    private fun mostrarFavoritos(view: View) {
        val contenedor = view.findViewById<LinearLayout>(R.id.contenedorFavoritos)
        contenedor.removeAllViews()
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloFavoritos)
        val favoritos = obtenerFavoritos()

        if (favoritos.isEmpty()) {
            tvTitulo.visibility = View.GONE
            return
        }

        tvTitulo.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(requireContext())
        val todos = mayoristasBase + obtenerMayoristasPersonalizados()

        for (nombre in favoritos) {
            val m = todos.find { it.nombre == nombre } ?: continue
            val item = inflater.inflate(R.layout.item_mayorista, contenedor, false)
            item.findViewById<TextView>(R.id.tvNombreMayorista).text = m.nombre
            item.findViewById<TextView>(R.id.tvTipoMayorista).text = m.tipo
            item.findViewById<TextView>(R.id.tvDistanciaMayorista).text = m.distancia
            item.findViewById<TextView>(R.id.tvEstadoMayorista).text = m.estado

            val btnFav = item.findViewById<TextView>(R.id.btnGuardarFavorito)
            btnFav.text = "★"
            btnFav.setOnClickListener {
                val favs = obtenerFavoritos().toMutableSet()
                favs.remove(nombre)
                guardarFavoritos(favs)
                mostrarMayoristas(view)
                mostrarFavoritos(view)
            }
            contenedor.addView(item)
        }
    }

    // ---- SharedPreferences: favoritos ----

    private fun obtenerFavoritos(): Set<String> {
        val prefs = requireContext().getSharedPreferences("favoritos_mayoristas", Context.MODE_PRIVATE)
        return prefs.getStringSet("favoritos", emptySet()) ?: emptySet()
    }

    private fun guardarFavoritos(favoritos: Set<String>) {
        val prefs = requireContext().getSharedPreferences("favoritos_mayoristas", Context.MODE_PRIVATE)
        prefs.edit().putStringSet("favoritos", favoritos).apply()
    }

    // ---- SharedPreferences: mayoristas personalizados ----

    private fun guardarMayoristaPersonalizado(m: Mayorista) {
        val prefs = requireContext().getSharedPreferences("mayoristas_custom", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("lista", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add("${m.nombre}|${m.tipo}|${m.distancia}|${m.estado}")
        prefs.edit().putStringSet("lista", set).apply()
    }

    private fun obtenerMayoristasPersonalizados(): List<Mayorista> {
        val prefs = requireContext().getSharedPreferences("mayoristas_custom", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("lista", emptySet()) ?: emptySet()
        return set.mapNotNull { entry ->
            val partes = entry.split("|")
            if (partes.size == 4) Mayorista(partes[0], partes[1], partes[2], partes[3]) else null
        }
    }
}