package com.bodegapp.app.ui.tienda

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bodegapp.app.R
import com.bodegapp.app.data.DbHelper
import com.bodegapp.app.data.BodegaRepository
import com.bodegapp.app.ui.dashboard.DashboardFragment


class LoginActivity : AppCompatActivity() {

    private lateinit var repo: BodegaRepository

    private lateinit var btnAceptar: Button
    private lateinit var etNombre: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etNombreTienda: EditText
    private lateinit var etDireccionTienda: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        repo = BodegaRepository(this)

        if (repo.ExisteUsuario()) {
            val intent = Intent(this, DashboardFragment::class.java)
            startActivity(intent)
        }

        btnAceptar = findViewById(R.id.btnAceptar)
        etNombre = findViewById(R.id.etNombre)
        etApellidos = findViewById(R.id.etApellidos)
        etNombreTienda = findViewById(R.id.etNombreTienda)
        etDireccionTienda = findViewById(R.id.etDireccionTienda)

        // agrego la informacion a la base de datos con el boton
        btnAceptar.setOnClickListener {
            val inputNombre = etNombre.text.toString().trim()
            val inputApellidos = etApellidos.text.toString().trim()
            val inputNombreTienda = etNombreTienda.text.toString().trim()
//            val inputDireccionTienda = etDireccionTienda.text.toString().trim()


            //Validacion
            if (inputNombreTienda.isEmpty() || inputApellidos.isEmpty() || inputNombre.isEmpty()) {
                //mostramos mensaje si esta vacio
                Toast.makeText(this, "Por favor, complete los campos", Toast.LENGTH_SHORT).show()
            } else {
                // Si funciona
                repo.insertar_usuario(
                    inputNombre,
                    inputApellidos,
                )
                Toast.makeText(this, "Bienvenido, $inputNombre", Toast.LENGTH_SHORT).show()

                //aca que lo jale a otra vista
                mostrarFragmento(DashboardFragment())

            }


        }

    }

    private fun mostrarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}