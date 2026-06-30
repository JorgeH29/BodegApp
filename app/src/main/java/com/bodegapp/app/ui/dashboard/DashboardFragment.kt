package com.bodegapp.app.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository
import com.bodegapp.app.ui.fiados.RegistrarFiadoActivity
import com.bodegapp.app.ui.inventario.AgregarProductoActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardFragment : Fragment() {

    private lateinit var repo: BodegaRepository
    private lateinit var txtSaludo: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = BodegaRepository(requireContext())
        txtSaludo = view.findViewById(R.id.txtSaludo)

        view.findViewById<View>(R.id.btnNuevaVenta).setOnClickListener {
            startActivity(Intent(requireContext(), NuevaVentaActivity::class.java))
        }
        view.findViewById<View>(R.id.btnAgregarProductoQuick).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarProductoActivity::class.java))
        }
        view.findViewById<View>(R.id.btnFiadosQuick).setOnClickListener {
            irATab(R.id.nav_fiados)
        }
        view.findViewById<View>(R.id.btnReporteQuick).setOnClickListener {
            irATab(R.id.nav_reporte)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun irATab(itemId: Int) {
        val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav?.selectedItemId = itemId
    }

    private fun cargarDatos() {
        val v = view ?: return

        val totalHoy = repo.totalVendidoHoy()
        val totalProductos = repo.contarProductos()
        val totalFiado = repo.totalFiadoPendiente()
        val stockBajo = repo.contarStockBajo()


        //mostrar nombre
        val cursor = repo.mostrarNombre()
        if (cursor.moveToFirst()) {
            val nombre = cursor.getString(0)
            txtSaludo.text = "Hola $nombre"
        }
        cursor.close()



        v.findViewById<android.widget.TextView>(R.id.tvVentasHoy).text =
            "S/ %.2f".format(totalHoy)
        v.findViewById<android.widget.TextView>(R.id.tvProductosCount).text =
            "$totalProductos Productos"
        v.findViewById<android.widget.TextView>(R.id.tvFiadosPendientes).text =
            "S/ %.2f".format(totalFiado)
        v.findViewById<android.widget.TextView>(R.id.tvStockBajoCount).text =
            "$stockBajo Stock bajo"

        // Alertas dinámicas: productos con stock bajo + fiados antiguos
        val contenedor = v.findViewById<android.widget.LinearLayout>(R.id.contenedorAlertas)
        contenedor.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        val productosBajos = repo.obtenerProductos().filter { it.stockBajo }
        for (p in productosBajos) {
            val item = inflater.inflate(R.layout.item_alerta, contenedor, false)
            item.findViewById<android.widget.TextView>(R.id.tvAlertaTitulo).text = p.nombre
            item.findViewById<android.widget.TextView>(R.id.tvAlertaSubtitulo).text =
                if (p.sinStock) "Sin stock" else "Solo ${p.stock} unidades"
            item.findViewById<android.widget.TextView>(R.id.tvAlertaEtiqueta).text =
                if (p.sinStock) "Agotado" else "Bajo"
            contenedor.addView(item)
        }

        val fiadosPendientes = repo.obtenerFiados(soloPendientes = true)
        for (f in fiadosPendientes) {
            val item = inflater.inflate(R.layout.item_alerta, contenedor, false)
            item.findViewById<android.widget.TextView>(R.id.tvAlertaTitulo).text = f.clienteNombre
            item.findViewById<android.widget.TextView>(R.id.tvAlertaSubtitulo).text =
                "Fiado desde el ${f.fecha}"
            item.findViewById<android.widget.TextView>(R.id.tvAlertaEtiqueta).text =
                "S/ %.2f".format(f.monto)
            contenedor.addView(item)
        }

        if (productosBajos.isEmpty() && fiadosPendientes.isEmpty()) {
            val tv = android.widget.TextView(requireContext())
            tv.text = "Sin alertas por ahora 🎉"
            tv.setTextColor(resources.getColor(R.color.text_gray, null))
            contenedor.addView(tv)
        }
    }
}
