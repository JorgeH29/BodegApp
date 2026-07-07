package com.bodegapp.app.ui.fiados

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bodegapp.app.R
import com.bodegapp.app.data.BodegaRepository

class RegistrarFiadoActivity : AppCompatActivity() {

    private lateinit var repo: BodegaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_fiado)
        repo = BodegaRepository(this)

        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.btnGuardarFiado).setOnClickListener { guardar() }
    }

    private fun guardar() {
        val nombre = findViewById<EditText>(R.id.etNombreCliente).text.toString().trim()
        val montoTxt = findViewById<EditText>(R.id.etMontoFiado).text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa el nombre del cliente", Toast.LENGTH_SHORT).show()
            return
        }
        val monto = montoTxt.toDoubleOrNull()
        if (monto == null || monto <= 0) {
            Toast.makeText(this, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
            return
        }
        repo.insertarFiado(nombre, monto)
        Toast.makeText(this, "Fiado registrado", Toast.LENGTH_SHORT).show()
        finish()
    }
}
