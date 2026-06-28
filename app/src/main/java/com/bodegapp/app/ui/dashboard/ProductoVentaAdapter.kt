package com.bodegapp.app.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.Producto

class ProductoVentaAdapter(
    private val productos: List<Producto>,
    private val onSeleccionado: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoVentaAdapter.VH>() {

    private var seleccionadoId: Long? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombreProductoVenta)
        val stock: TextView = view.findViewById(R.id.tvStockProductoVenta)
        val precio: TextView = view.findViewById(R.id.tvPrecioProductoVenta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_venta, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = productos[position]
        holder.nombre.text = p.nombre
        holder.stock.text = "Stock: ${p.stock} uds"
        holder.precio.text = "S/ %.2f".format(p.precio)
        holder.itemView.alpha = if (p.id == seleccionadoId) 1f else 0.85f
        holder.itemView.setOnClickListener {
            seleccionadoId = p.id
            notifyDataSetChanged()
            onSeleccionado(p)
        }
    }

    override fun getItemCount(): Int = productos.size
}
