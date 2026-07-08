package com.bodegapp.app.ui.fiados

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository
import com.bodegapp.app.data.Fiado

class FiadosFragment : Fragment() {

    private lateinit var repo: BodegaRepository
    private lateinit var adapter: FiadosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_fiados, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = BodegaRepository(requireContext())

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerFiados)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = FiadosAdapter(
            fiados = emptyList(),
            onMarcarPagado = { fiado ->
                repo.marcarFiadoPagado(fiado.id)
                cargarDatos()
            },
            onEditarOEliminar = { fiado ->
                mostrarDialogoEditarEliminar(fiado)
            }
        )
        recycler.adapter = adapter

        view.findViewById<View>(R.id.btnRegistrarFiado).setOnClickListener {
            startActivity(Intent(requireContext(), RegistrarFiadoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        val v = view ?: return
        val fiados = repo.obtenerFiados()
        adapter.actualizar(fiados)

        v.findViewById<TextView>(R.id.tvTotalFiadoCard).text =
            "S/ %.0f".format(repo.totalFiadoPendiente())
        v.findViewById<TextView>(R.id.tvClientesDeudaCard).text =
            repo.contarClientesConDeuda().toString()
    }

    /**
     * Diálogo que se abre al mantener presionado un fiado. Permite corregir
     * el nombre del cliente y el monto, o eliminar el registro por completo.
     */
    private fun mostrarDialogoEditarEliminar(fiado: Fiado) {
        val ctx = requireContext()
        val padding = (16 * resources.displayMetrics.density).toInt()

        val contenedor = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }

        val inputNombre = EditText(ctx).apply {
            hint = "Nombre del cliente"
            setText(fiado.clienteNombre)
        }
        val inputMonto = EditText(ctx).apply {
            hint = "Monto"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(if (fiado.monto % 1.0 == 0.0) fiado.monto.toInt().toString() else fiado.monto.toString())
        }

        contenedor.addView(inputNombre)
        contenedor.addView(inputMonto)

        AlertDialog.Builder(ctx)
            .setTitle("Editar fiado")
            .setView(contenedor)
            .setPositiveButton("Guardar") { dialog, _ ->
                val nuevoNombre = inputNombre.text.toString().trim()
                val nuevoMonto = inputMonto.text.toString().trim().toDoubleOrNull()

                if (nuevoNombre.isEmpty() || nuevoMonto == null) {
                    android.widget.Toast.makeText(
                        ctx, "Revisa el nombre y el monto ingresados", android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    repo.actualizarFiado(fiado.id, nuevoNombre, nuevoMonto)
                    cargarDatos()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Eliminar") { dialog, _ ->
                dialog.dismiss()
                confirmarEliminarFiado(fiado)
            }
            .show()
    }

    private fun confirmarEliminarFiado(fiado: Fiado) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar fiado")
            .setMessage("¿Seguro que quieres eliminar el fiado de ${fiado.clienteNombre} por S/ %.2f?".format(fiado.monto))
            .setPositiveButton("Eliminar") { dialog, _ ->
                repo.eliminarFiado(fiado.id)
                cargarDatos()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
