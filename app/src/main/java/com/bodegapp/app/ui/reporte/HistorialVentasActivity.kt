package com.bodegapp.app.ui.reporte

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository

/**
 * Pantalla de historial. Sin extra: muestra la lista de días con su total vendido.
 * Con EXTRA_FECHA: muestra el detalle (cada venta) de ese día.
 */
class HistorialVentasActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FECHA = "extra_fecha"
    }

    private lateinit var repo: BodegaRepository
    private var fechaSeleccionada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_ventas)
        repo = BodegaRepository(this)

        fechaSeleccionada = intent.getStringExtra(EXTRA_FECHA)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistorial)
        recycler.layoutManager = LinearLayoutManager(this)

        if (fechaSeleccionada == null) {
            mostrarListaDeDias(recycler)
        } else {
            mostrarDetalleDelDia(recycler, fechaSeleccionada!!)
        }
    }

    private fun mostrarListaDeDias(recycler: RecyclerView) {
        findViewById<TextView>(R.id.tvTituloHistorial).text = "Historial de ventas"
        val dias = repo.obtenerHistorialPorDia()

        if (dias.isEmpty()) {
            findViewById<View>(R.id.tvVacioHistorial).visibility = View.VISIBLE
            recycler.visibility = View.GONE
            return
        }

        recycler.adapter = object : RecyclerView.Adapter<DiaVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, position: Int): DiaVH {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_dia_historial, parent, false)
                return DiaVH(v)
            }

            override fun onBindViewHolder(holder: DiaVH, position: Int) {
                val (fecha, total) = dias[position]
                holder.fecha.text = fecha
                holder.total.text = "S/ %.2f".format(total)
                holder.itemView.setOnClickListener {
                    val intent = Intent(this@HistorialVentasActivity, HistorialVentasActivity::class.java)
                    intent.putExtra(EXTRA_FECHA, fecha)
                    startActivity(intent)
                }
            }

            override fun getItemCount(): Int = dias.size
        }
    }

    private fun mostrarDetalleDelDia(recycler: RecyclerView, fecha: String) {
        findViewById<TextView>(R.id.tvTituloHistorial).text = "Ventas del $fecha"
        val ventas = repo.obtenerVentasPorFecha(fecha)

        if (ventas.isEmpty()) {
            findViewById<View>(R.id.tvVacioHistorial).visibility = View.VISIBLE
            recycler.visibility = View.GONE
            return
        }

        recycler.adapter = object : RecyclerView.Adapter<VentaVH>() {
            override fun onCreateViewHolder(parent: ViewGroup, position: Int): VentaVH {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_venta_detalle, parent, false)
                return VentaVH(v)
            }

            override fun onBindViewHolder(holder: VentaVH, position: Int) {
                val venta = ventas[position]
                holder.hora.text = venta.hora
                holder.nombre.text = venta.productoNombre
                holder.cantidad.text = "${venta.cantidad} unidades"
                holder.total.text = "S/ %.2f".format(venta.total)
            }

            override fun getItemCount(): Int = ventas.size
        }
    }

    class DiaVH(view: View) : RecyclerView.ViewHolder(view) {
        val fecha: TextView = view.findViewById(R.id.tvFechaDia)
        val total: TextView = view.findViewById(R.id.tvTotalDia)
    }

    class VentaVH(view: View) : RecyclerView.ViewHolder(view) {
        val hora: TextView = view.findViewById(R.id.tvHoraVenta)
        val nombre: TextView = view.findViewById(R.id.tvNombreVenta)
        val cantidad: TextView = view.findViewById(R.id.tvCantidadVenta)
        val total: TextView = view.findViewById(R.id.tvTotalVentaItem)
    }
}
