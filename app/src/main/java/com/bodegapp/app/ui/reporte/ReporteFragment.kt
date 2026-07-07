package com.bodegapp.app.ui.reporte

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReporteFragment : Fragment() {

    private lateinit var repo: BodegaRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reporte, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo = BodegaRepository(requireContext())

        view.findViewById<View>(R.id.btnBackReporte).setOnClickListener {
            activity?.findViewById<BottomNavigationView>(R.id.bottomNav)?.selectedItemId = R.id.nav_inicio
        }

        view.findViewById<View>(R.id.btnCompartirReporte).setOnClickListener {
            compartirReporte()
        }

        view.findViewById<View>(R.id.btnExportarPdf).setOnClickListener {
            exportarPdf()
        }

        view.findViewById<View>(R.id.btnVerHistorial).setOnClickListener {
            startActivity(Intent(requireContext(), HistorialVentasActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarReporte()
    }

    private fun cargarReporte() {
        val v = view ?: return
        val sdf = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "PE"))
        v.findViewById<TextView>(R.id.tvFechaReporte).text = sdf.format(Date()).uppercase()

        val totalHoy = repo.totalVendidoHoy()
        v.findViewById<TextView>(R.id.tvTotalVendidoReporte).text = "S/ %.2f".format(totalHoy)

        dibujarBarrasPorHora(v)
        dibujarTopProductos(v)
    }

    private fun dibujarBarrasPorHora(v: View) {
        val ventas = repo.obtenerVentasDeHoy()
        val porHora = ventas.groupBy { it.hora.substringBefore(":") }
            .mapValues { (_, lista) -> lista.sumOf { it.total } }

        val contenedor = v.findViewById<LinearLayout>(R.id.contenedorBarras)
        contenedor.removeAllViews()

        if (porHora.isEmpty()) {
            val tv = TextView(requireContext())
            tv.text = "Aún no hay ventas registradas hoy"
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
            contenedor.addView(tv)
            return
        }

        val maxVenta = porHora.values.maxOrNull() ?: 1.0
        val horasOrdenadas = porHora.keys.sorted()

        for (hora in horasOrdenadas) {
            val monto = porHora[hora] ?: 0.0
            val alturaPx = (80 * (monto / maxVenta)).toInt().coerceAtLeast(6)

            val barraContainer = LinearLayout(requireContext())
            barraContainer.orientation = LinearLayout.VERTICAL
            barraContainer.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            val paramsContainer = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            paramsContainer.marginEnd = 6
            barraContainer.layoutParams = paramsContainer

            val barra = View(requireContext())
            barra.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.orange_primary))
            val paramsBarra = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(alturaPx))
            barra.layoutParams = paramsBarra

            val etiqueta = TextView(requireContext())
            etiqueta.text = "${hora}h"
            etiqueta.textSize = 10f
            etiqueta.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
            etiqueta.gravity = android.view.Gravity.CENTER

            barraContainer.addView(barra)
            barraContainer.addView(etiqueta)
            contenedor.addView(barraContainer)
        }
    }

    private fun dibujarTopProductos(v: View) {
        val contenedor = v.findViewById<LinearLayout>(R.id.contenedorTopProductos)
        contenedor.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        val ventasHoy = repo.obtenerVentasDeHoy()
        val top = repo.productosMasVendidosHoy().take(5)

        if (top.isEmpty()) {
            val tv = TextView(requireContext())
            tv.text = "Todavía no se vendió ningún producto hoy"
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray))
            contenedor.addView(tv)
            return
        }

        top.forEachIndexed { index, (nombre, cantidad) ->
            val montoProducto = ventasHoy.filter { it.productoNombre == nombre }.sumOf { it.total }
            val item = inflater.inflate(R.layout.item_top_producto, contenedor, false)
            item.findViewById<TextView>(R.id.tvRankingNumero).text = (index + 1).toString()
            item.findViewById<TextView>(R.id.tvNombreTopProducto).text = nombre
            item.findViewById<TextView>(R.id.tvCantidadTopProducto).text = "$cantidad unidades"
            item.findViewById<TextView>(R.id.tvMontoTopProducto).text = "S/ %.2f".format(montoProducto)
            contenedor.addView(item)
        }
    }

    private fun compartirReporte() {
        val totalHoy = repo.totalVendidoHoy()
        val top = repo.productosMasVendidosHoy().take(3)
        val resumenTop = top.joinToString("\n") { "- ${it.first}: ${it.second} uds" }

        val texto = """
            📊 Reporte del día - BodegApp
            Total vendido hoy: S/ %.2f

            Lo más vendido:
            $resumenTop
        """.trimIndent().format(totalHoy)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, texto)
        startActivity(Intent.createChooser(intent, "Compartir reporte"))
    }

    /**
     * Genera un PDF de una página con el resumen del día usando android.graphics.pdf.PdfDocument
     * (no requiere librerías externas) y lo abre con el selector de apps del sistema.
     */
    private fun exportarPdf() {
        val context = requireContext()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaTexto = sdf.format(Date())
        val totalHoy = repo.totalVendidoHoy()
        val top = repo.productosMasVendidosHoy().take(5)
        val ventasHoy = repo.obtenerVentasDeHoy()

        val documento = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 a 72dpi aprox.
        val pagina = documento.startPage(pageInfo)
        val canvas: Canvas = pagina.canvas

        val paintTitulo = Paint().apply {
            color = Color.parseColor("#FF6A1B")
            textSize = 22f
            isFakeBoldText = true
        }
        val paintSubtitulo = Paint().apply {
            color = Color.DKGRAY
            textSize = 13f
        }
        val paintTexto = Paint().apply {
            color = Color.BLACK
            textSize = 13f
        }
        val paintTotal = Paint().apply {
            color = Color.parseColor("#2E7D32")
            textSize = 20f
            isFakeBoldText = true
        }

        var y = 50f
        canvas.drawText("BodegApp - Reporte del día", 40f, y, paintTitulo)
        y += 24f
        canvas.drawText("Fecha: $fechaTexto", 40f, y, paintSubtitulo)
        y += 34f
        canvas.drawText("Total vendido: S/ %.2f".format(totalHoy), 40f, y, paintTotal)
        y += 36f

        canvas.drawText("Lo más vendido:", 40f, y, paintTitulo.apply { textSize = 16f })
        y += 24f
        if (top.isEmpty()) {
            canvas.drawText("No se registraron ventas este día.", 40f, y, paintTexto)
            y += 20f
        } else {
            top.forEach { (nombre, cantidad) ->
                canvas.drawText("• $nombre — $cantidad unidades", 40f, y, paintTexto)
                y += 20f
            }
        }

        y += 16f
        canvas.drawText("Detalle de ventas:", 40f, y, paintTitulo.apply { textSize = 16f })
        y += 24f
        if (ventasHoy.isEmpty()) {
            canvas.drawText("Sin ventas registradas.", 40f, y, paintTexto)
        } else {
            ventasHoy.forEach { venta ->
                canvas.drawText(
                    "${venta.hora}  ${venta.productoNombre}  x${venta.cantidad}  S/ %.2f".format(venta.total),
                    40f, y, paintTexto
                )
                y += 18f
                if (y > 800f) return@forEach // evita salirse de la página en reportes muy largos
            }
        }

        documento.finishPage(pagina)

        try {
            val carpeta = File(context.cacheDir, "reportes")
            if (!carpeta.exists()) carpeta.mkdirs()
            val archivo = File(carpeta, "reporte_bodegapp.pdf")
            FileOutputStream(archivo).use { documento.writeTo(it) }
            documento.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Abrir reporte PDF"))
        } catch (e: Exception) {
            documento.close()
            Toast.makeText(context, "No se pudo generar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
