package com.bodegapp.app.ui.inventario

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var repo: BodegaRepository
    private var productoId: Long = -1L

    companion object {
        const val EXTRA_PRODUCTO_ID = "extra_producto_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)
        repo = BodegaRepository(this)

        productoId = intent.getLongExtra(EXTRA_PRODUCTO_ID, -1L)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.btnGuardarProducto).setOnClickListener { guardar() }
        findViewById<android.view.View>(R.id.btnEliminarProducto).setOnClickListener { confirmarEliminar() }

        if (productoId != -1L) {
            modoEdicion()
        }
    }

    private fun modoEdicion() {
        val producto = repo.obtenerProductoPorId(productoId) ?: return
        findViewById<TextView>(R.id.tvTituloAgregarProducto).text = "Editar producto"
        findViewById<EditText>(R.id.etNombreProducto).setText(producto.nombre)
        findViewById<EditText>(R.id.etCategoriaProducto).setText(producto.categoria)
        findViewById<EditText>(R.id.etPrecioProducto).setText(producto.precio.toString())
        findViewById<EditText>(R.id.etStockProducto).setText(producto.stock.toString())
        findViewById<EditText>(R.id.etStockMinProducto).setText(producto.stockMin.toString())
        findViewById<TextView>(R.id.btnGuardarProducto).text = "Guardar cambios"
        findViewById<android.view.View>(R.id.btnEliminarProducto).visibility = android.view.View.VISIBLE
    }

    private fun guardar() {
        val nombre = findViewById<EditText>(R.id.etNombreProducto).text.toString().trim()
        val categoria = findViewById<EditText>(R.id.etCategoriaProducto).text.toString().trim()
        val precioTxt = findViewById<EditText>(R.id.etPrecioProducto).text.toString().trim()
        val stockTxt = findViewById<EditText>(R.id.etStockProducto).text.toString().trim()
        val stockMinTxt = findViewById<EditText>(R.id.etStockMinProducto).text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre del producto", Toast.LENGTH_SHORT).show()
            return
        }
        val precio = precioTxt.toDoubleOrNull() ?: 0.0
        val stock = stockTxt.toIntOrNull() ?: 0
        val stockMin = stockMinTxt.toIntOrNull() ?: 5
        val categoriaFinal = categoria.ifEmpty { "General" }

        if (productoId != -1L) {
            repo.actualizarProducto(productoId, nombre, categoriaFinal, precio, stock, stockMin)
            Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
        } else {
            repo.insertarProducto(nombre, categoriaFinal, precio, stock, stockMin)
            Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun confirmarEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Seguro que quieres eliminar este producto del inventario? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                repo.eliminarProducto(productoId)
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
