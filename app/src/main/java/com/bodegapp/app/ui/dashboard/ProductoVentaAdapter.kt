package com.bodegapp.app.ui.dashboard

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.Producto
import java.io.File

class ProductoVentaAdapter(
    private val productos: List<Producto>,
    private val onSeleccionado: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoVentaAdapter.VH>() {

    private var seleccionadoId: Long? = null

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val foto: ImageView = view.findViewById(R.id.ivFotoProductoVenta)
        val nombre: TextView = view.findViewById(R.id.tvNombreProductoVenta)
        val stock: TextView = view.findViewById(R.id.tvStockProductoVenta)
        val precio: TextView = view.findViewById(R.id.tvPrecioProductoVenta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_venta, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = productos[position]
        holder.nombre.text = p.nombre
        holder.stock.text = "Stock: ${p.stock} uds"
        holder.precio.text = "S/ %.2f".format(p.precio)
        holder.itemView.alpha = if (p.id == seleccionadoId) 1f else 0.85f

        // Cargar foto si existe
        if (!p.fotografia.isNullOrEmpty()) {
            val archivo = File(p.fotografia)
            if (archivo.exists()) {
                holder.foto.setImageURI(Uri.fromFile(archivo))
            } else {
                holder.foto.setImageResource(R.drawable.ic_box)
            }
        } else {
            holder.foto.setImageResource(R.drawable.ic_box)
        }

        holder.itemView.setOnClickListener {
            seleccionadoId = p.id
            notifyDataSetChanged()
            onSeleccionado(p)
        }
    }

    override fun getItemCount(): Int = productos.size
}