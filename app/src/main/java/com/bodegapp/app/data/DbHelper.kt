package com.bodegapp.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Maneja la creación y acceso a la base de datos SQLite local de BodegApp.
 * Todas las pantallas (Inventario, Fiados, Ventas, Reporte) leen y escriben aquí.
 */
class DbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "bodegapp.db"
        const val DB_VERSION = 4


        //tabla usuario - No habia
        const val TABBLE_USUARIO = "usuario"
        const val COL_USUARIO_ID = "UsuarioID"
        const val COL_USUARIO_NOMBRES = "Nombre"
        const val COL_USUARIO_APELLIDOS = "Apellidos"


        // Tabla productos
        const val TABLE_PRODUCTOS = "productos"
        const val COL_PROD_ID = "id"
        const val COL_PROD_NOMBRE = "nombre"
        const val COL_PROD_CATEGORIA = "categoria"
        const val COL_PROD_PRECIO = "precio"
        const val COL_PROD_STOCK = "stock"
        const val COL_PROD_STOCK_MIN = "stock_min"

        // Tabla clientes (para fiados)
        const val TABLE_CLIENTES = "clientes"
        const val COL_CLI_ID = "id"
        const val COL_CLI_NOMBRE = "nombre"

        // Tabla fiados
        const val TABLE_FIADOS = "fiados"
        const val COL_FIADO_ID = "id"
        const val COL_FIADO_CLIENTE_ID = "cliente_id"
        const val COL_FIADO_MONTO = "monto"
        const val COL_FIADO_FECHA = "fecha"
        const val COL_FIADO_PAGADO = "pagado" // 0 = pendiente, 1 = pagado

        // Tabla ventas
        const val TABLE_VENTAS = "ventas"
        const val COL_VENTA_ID = "id"
        const val COL_VENTA_PRODUCTO_ID = "producto_id"
        const val COL_VENTA_PRODUCTO_NOMBRE = "producto_nombre"
        const val COL_VENTA_CANTIDAD = "cantidad"
        const val COL_VENTA_TOTAL = "total"
        const val COL_VENTA_FECHA = "fecha" // yyyy-MM-dd
        const val COL_VENTA_HORA = "hora"   // HH:mm

        // En companion object agrega:
        const val COL_PROD_FOTO = "fotografia"
    }


    //USUARIO, no habia una tabla usuario
    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
    CREATE TABLE $TABBLE_USUARIO (
        $COL_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_USUARIO_NOMBRES TEXT,
        $COL_USUARIO_APELLIDOS TEXT 

    )
    """.trimIndent()
        )





        db.execSQL(
            """
    CREATE TABLE $TABLE_PRODUCTOS (
        $COL_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COL_PROD_NOMBRE TEXT NOT NULL,
        $COL_PROD_CATEGORIA TEXT,
        $COL_PROD_PRECIO REAL NOT NULL DEFAULT 0,
        $COL_PROD_STOCK INTEGER NOT NULL DEFAULT 0,
        $COL_PROD_STOCK_MIN INTEGER NOT NULL DEFAULT 0,
        $COL_PROD_FOTO TEXT
    )
    """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_CLIENTES (
                $COL_CLI_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CLI_NOMBRE TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_FIADOS (
                $COL_FIADO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FIADO_CLIENTE_ID INTEGER NOT NULL,
                $COL_FIADO_MONTO REAL NOT NULL DEFAULT 0,
                $COL_FIADO_FECHA TEXT,
                $COL_FIADO_PAGADO INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY($COL_FIADO_CLIENTE_ID) REFERENCES $TABLE_CLIENTES($COL_CLI_ID)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_VENTAS (
                $COL_VENTA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_VENTA_PRODUCTO_ID INTEGER,
                $COL_VENTA_PRODUCTO_NOMBRE TEXT,
                $COL_VENTA_CANTIDAD INTEGER NOT NULL DEFAULT 1,
                $COL_VENTA_TOTAL REAL NOT NULL DEFAULT 0,
                $COL_VENTA_FECHA TEXT,
                $COL_VENTA_HORA TEXT
            )
            """.trimIndent()
        )

        // Datos de ejemplo, igual al mockup, para que la app no se vea vacía la primera vez
        insertarDatosDemo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABBLE_USUARIO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLIENTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FIADOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VENTAS")
        onCreate(db)
    }

    private fun insertarDatosDemo(db: SQLiteDatabase) {
        fun prod(nombre: String, cat: String, precio: Double, stock: Int, min: Int) {
            val cv = ContentValues().apply {
                put(COL_PROD_NOMBRE, nombre)
                put(COL_PROD_CATEGORIA, cat)
                put(COL_PROD_PRECIO, precio)
                put(COL_PROD_STOCK, stock)
                put(COL_PROD_STOCK_MIN, min)
            }
            db.insert(TABLE_PRODUCTOS, null, cv)
        }

        fun cliente(nombre: String): Long {
            val cv = ContentValues().apply { put(COL_CLI_NOMBRE, nombre) }
            return db.insert(TABLE_CLIENTES, null, cv)
        }

        fun fiado(clienteId: Long, monto: Double, fecha: String, pagado: Int) {
            val cv = ContentValues().apply {
                put(COL_FIADO_CLIENTE_ID, clienteId)
                put(COL_FIADO_MONTO, monto)
                put(COL_FIADO_FECHA, fecha)
                put(COL_FIADO_PAGADO, pagado)
            }
            db.insert(TABLE_FIADOS, null, cv)
        }

    }
}
