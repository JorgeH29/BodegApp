package com.bodegapp.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bodegapp.app.data.BodegaRepository
import com.bodegapp.app.ui.dashboard.DashboardFragment
import com.bodegapp.app.ui.fiados.FiadosFragment
import com.bodegapp.app.ui.inventario.InventarioFragment
import com.bodegapp.app.ui.reporte.ReporteFragment
import com.bodegapp.app.ui.tienda.LoginActivity
import com.bodegapp.app.ui.tienda.MiTiendaFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var repo: BodegaRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        repo = BodegaRepository(this)


        // si no hay ningun usuario, que jale el login, si hay un usuario, que entre al dashboard
        if (repo.ExisteUsuario()) {
            mostrarFragmento(DashboardFragment())
        }
        else{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Pantalla inicial: Dashboard ("Inicio")
        // lo comente para que no se pierda
//        if (savedInstanceState == null) {
//            mostrarFragmento(DashboardFragment())
//        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_inicio -> DashboardFragment()
                R.id.nav_inventario -> InventarioFragment()
                R.id.nav_fiados -> FiadosFragment()
                R.id.nav_reporte -> ReporteFragment()
                R.id.nav_tienda -> MiTiendaFragment()
                else -> DashboardFragment()
            }
            mostrarFragmento(fragment)
            true
        }
    }

    private fun mostrarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
