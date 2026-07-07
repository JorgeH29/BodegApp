package com.bodegapp.app.ui.inventario

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository
import java.io.File
import java.io.FileOutputStream

class AgregarProductoActivity : AppCompatActivity() {

    private lateinit var repo: BodegaRepository
    private var productoId: Long = -1L
    private var fotoUri: Uri? = null
    private var rutaFotoGuardada: String? = null

    companion object {
        const val EXTRA_PRODUCTO_ID = "extra_producto_id"
    }

    // Lanzador de cámara
    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito && fotoUri != null) {
            findViewById<ImageView>(R.id.ivFotoProducto).apply {
                setImageURI(fotoUri)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            rutaFotoGuardada = fotoUri.toString()
        }
    }

    // Lanzador de permiso de cámara
    private val pedirPermisoCamara = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { otorgado ->
        if (otorgado) abrirCamara()
        else Toast.makeText(this, "Permiso de cámara necesario", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_producto)
        repo = BodegaRepository(this)

        productoId = intent.getLongExtra(EXTRA_PRODUCTO_ID, -1L)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.btnGuardarProducto).setOnClickListener { guardar() }
        findViewById<android.view.View>(R.id.btnEliminarProducto).setOnClickListener { confirmarEliminar() }
        findViewById<android.view.View>(R.id.btnTomarFoto).setOnClickListener { verificarPermisoCamara() }

        if (productoId != -1L) modoEdicion()
    }

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> abrirCamara()
            else -> pedirPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        val nombre = findViewById<EditText>(R.id.etNombreProducto).text.toString().trim()
        val nombreArchivo = if (nombre.isNotEmpty())
            "producto_${nombre.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
        else
            "producto_${System.currentTimeMillis()}.jpg"

        val cv = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        fotoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        fotoUri?.let { tomarFotoLauncher.launch(it) }
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

        // Guardar foto en almacenamiento interno de la app
        val rutaFinal = rutaFotoGuardada?.let { guardarFotoInterna(it, nombre) }

        if (productoId != -1L) {
            repo.actualizarProducto(productoId, nombre, categoriaFinal, precio, stock, stockMin)
            Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show()
        } else {
            val rutaFoto = rutaFotoGuardada?.let { guardarFotoInterna(it, nombre) }
            repo.insertarProducto(nombre, categoriaFinal, precio, stock, stockMin, rutaFoto)
        }
        finish()
    }

    private fun guardarFotoInterna(uriStr: String, nombre: String): String? {
        return try {
            val uri = Uri.parse(uriStr)
            val carpeta = File(filesDir, "fotos_productos")
            if (!carpeta.exists()) carpeta.mkdirs()
            val archivo = File(carpeta, "foto_${nombre.replace(" ", "_")}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(archivo).use { output ->
                    input.copyTo(output)
                }
            }
            archivo.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun confirmarEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Seguro que quieres eliminar este producto? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                repo.eliminarProducto(productoId)
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}