package com.bodegapp.app.ui.dashboard

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository
import com.bodegapp.app.data.Producto

class NuevaVentaActivity : AppCompatActivity() {

    private lateinit var repo: BodegaRepository
    private var productoSeleccionado: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_venta)
        repo = BodegaRepository(this)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerProductosVenta)
        recycler.layoutManager = LinearLayoutManager(this)
        val productos = repo.obtenerProductos()
        recycler.adapter = ProductoVentaAdapter(productos) { producto ->
            productoSeleccionado = producto
            actualizarResumen()
        }

        val etCantidad = findViewById<EditText>(R.id.etCantidad)
        etCantidad.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { actualizarResumen() }
        })

        findViewById<android.view.View>(R.id.btnRegistrarVenta).setOnClickListener {
            registrarVenta()
        }
    }

    private fun cantidadActual(): Int {
        val texto = findViewById<EditText>(R.id.etCantidad).text.toString()
        return texto.toIntOrNull() ?: 1
    }

    private fun actualizarResumen() {
        val producto = productoSeleccionado
        val tvSel = findViewById<TextView>(R.id.tvProductoSeleccionado)
        val tvTotal = findViewById<TextView>(R.id.tvTotalVenta)
        if (producto == null) {
            tvSel.text = "Ningún producto seleccionado"
            tvTotal.text = "Total: S/ 0.00"
            return
        }
        val cantidad = cantidadActual()
        tvSel.text = "Producto: ${producto.nombre}"
        tvTotal.text = "Total: S/ %.2f".format(producto.precio * cantidad)
    }

    private fun registrarVenta() {
        val producto = productoSeleccionado
        if (producto == null) {
            Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show()
            return
        }
        val cantidad = cantidadActual()
        if (cantidad <= 0) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show()
            return
        }
        if (cantidad > producto.stock) {
            Toast.makeText(this, "No hay suficiente stock", Toast.LENGTH_SHORT).show()
            return
        }
        val total = producto.precio * cantidad
        repo.registrarVenta(producto.id, producto.nombre, cantidad, total)
        Toast.makeText(this, "Venta registrada: S/ %.2f".format(total), Toast.LENGTH_SHORT).show()
        finish()
    }
}
