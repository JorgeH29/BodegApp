package com.bodegapp.app.ui.inventario

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository
import com.bodegapp.app.data.Producto

class InventarioFragment : Fragment() {

    private lateinit var repo: BodegaRepository
    private lateinit var adapter: InventarioAdapter
    private var todosLosProductos: List<Producto> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_inventario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = BodegaRepository(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerInventario)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = InventarioAdapter(emptyList()) { producto ->
            val intent = Intent(requireContext(), AgregarProductoActivity::class.java)
            intent.putExtra(AgregarProductoActivity.EXTRA_PRODUCTO_ID, producto.id)
            startActivity(intent)
        }
        recycler.adapter = adapter

        view.findViewById<View>(R.id.btnAgregarProducto).setOnClickListener { abrirAgregar() }
        view.findViewById<View>(R.id.btnAgregarProductoBoton).setOnClickListener { abrirAgregar() }

        view.findViewById<EditText>(R.id.etBuscarProducto).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { filtrar(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun abrirAgregar() {
        startActivity(Intent(requireContext(), AgregarProductoActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        cargarProductos()
    }

    private fun cargarProductos() {
        todosLosProductos = repo.obtenerProductos()
        adapter.actualizar(todosLosProductos)
        view?.findViewById<TextView>(R.id.tvTotalProductosInventario)?.text =
            "Todos los productos (${todosLosProductos.size})"
    }

    private fun filtrar(texto: String) {
        val filtrados = if (texto.isBlank()) {
            todosLosProductos
        } else {
            todosLosProductos.filter { it.nombre.contains(texto, ignoreCase = true) }
        }
        adapter.actualizar(filtrados)
    }
}
