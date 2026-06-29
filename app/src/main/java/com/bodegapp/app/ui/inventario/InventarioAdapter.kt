package com.bodegapp.app.ui.inventario

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.Producto
import java.io.File

class InventarioAdapter(
    private var productos: List<Producto>,
    private val onClick: (Producto) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val foto: ImageView = view.findViewById(R.id.ivFotoProductoInv)
        val nombre: TextView = view.findViewById(R.id.tvNombreProductoInv)
        val categoria: TextView = view.findViewById(R.id.tvCategoriaProductoInv)
        val stock: TextView = view.findViewById(R.id.tvStockProductoInv)
        val precio: TextView = view.findViewById(R.id.tvPrecioProductoInv)
    }

    fun actualizar(nuevaLista: List<Producto>) {
        productos = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_inventario, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = productos[position]
        holder.nombre.text = p.nombre
        holder.categoria.text = "${p.categoria} · S/ ${"%.2f".format(p.precio)}"
        holder.precio.text = "S/ %.2f".format(p.precio)

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

        val ctx = holder.itemView.context
        when {
            p.sinStock -> {
                holder.stock.text = "0 ud"
                holder.stock.setTextColor(ContextCompat.getColor(ctx, R.color.red_alert))
            }
            p.stockBajo -> {
                holder.stock.text = "${p.stock} ud"
                holder.stock.setTextColor(ContextCompat.getColor(ctx, R.color.red_alert))
            }
            else -> {
                holder.stock.text = "${p.stock} ud"
                holder.stock.setTextColor(ContextCompat.getColor(ctx, R.color.green_ok))
            }
        }
        holder.itemView.setOnClickListener { onClick(p) }
    }

    override fun getItemCount(): Int = productos.size
}