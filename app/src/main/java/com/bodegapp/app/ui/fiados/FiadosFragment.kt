package com.bodegapp.app.ui.fiados

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository

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
        adapter = FiadosAdapter(emptyList()) { fiado ->
            repo.marcarFiadoPagado(fiado.id)
            cargarDatos()
        }
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
}
