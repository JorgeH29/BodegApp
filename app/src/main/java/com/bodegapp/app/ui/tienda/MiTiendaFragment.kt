package com.bodegapp.app.ui.tienda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bodegapp.app.R

class MiTiendaFragment : Fragment() {

    data class Mayorista(val nombre: String, val tipo: String, val distancia: String, val estado: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_mi_tienda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnGuardarTienda).setOnClickListener {
            Toast.makeText(requireContext(), "Datos de la tienda guardados", Toast.LENGTH_SHORT).show()
        }

        val mayoristas = listOf(
            Mayorista("Makro Breña", "Mayorista", "1.1 km", "Abierto"),
            Mayorista("Metro La Marina", "Supermercado", "2.4 km", "Abierto"),
            Mayorista("Dist. Allcorp", "Distribuidor", "3.1 km", "Cierra 6pm")
        )

        val contenedor = view.findViewById<LinearLayout>(R.id.contenedorMayoristas)
        val inflater = LayoutInflater.from(requireContext())
        for (m in mayoristas) {
            val item = inflater.inflate(R.layout.item_mayorista, contenedor, false)
            item.findViewById<TextView>(R.id.tvNombreMayorista).text = m.nombre
            item.findViewById<TextView>(R.id.tvTipoMayorista).text = m.tipo
            item.findViewById<TextView>(R.id.tvDistanciaMayorista).text = m.distancia
            item.findViewById<TextView>(R.id.tvEstadoMayorista).text = m.estado
            contenedor.addView(item)
        }
    }
}
