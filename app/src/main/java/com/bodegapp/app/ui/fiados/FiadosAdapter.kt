package com.bodegapp.app.ui.fiados

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.Fiado

class FiadosAdapter(
    private var fiados: List<Fiado>,
    private val onMarcarPagado: (Fiado) -> Unit
) : RecyclerView.Adapter<FiadosAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreClienteFiado)
        val fecha: TextView = view.findViewById(R.id.tvFechaFiado)
        val monto: TextView = view.findViewById(R.id.tvMontoFiado)
        val estado: TextView = view.findViewById(R.id.tvEstadoFiado)
    }

    fun actualizar(nuevaLista: List<Fiado>) {
        fiados = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_fiado, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val f = fiados[position]
        val ctx = holder.itemView.context
        holder.nombre.text = f.clienteNombre
        holder.fecha.text = if (f.pagado) "Pagado el ${f.fecha}" else "Última compra: ${f.fecha}"
        holder.monto.text = "S/ %.2f".format(f.monto)

        if (f.pagado) {
            holder.estado.text = "Pagado"
            holder.estado.setBackgroundResource(R.drawable.bg_pill_green)
            holder.estado.setTextColor(ContextCompat.getColor(ctx, R.color.green_ok))
            holder.estado.setOnClickListener(null)
        } else {
            holder.estado.text = "Cobrar"
            holder.estado.setBackgroundResource(R.drawable.bg_pill_red)
            holder.estado.setTextColor(ContextCompat.getColor(ctx, R.color.red_alert))
            holder.estado.setOnClickListener { onMarcarPagado(f) }
        }
    }

    override fun getItemCount(): Int = fiados.size
}
