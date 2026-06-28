package com.bodegapp.app.data

import android.content.ContentValues
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio central: aquí se concentran todas las consultas SQL para que
 * los Fragments / Activities no manejen cursores directamente.
 */
class BodegaRepository(context: Context) {

    private val dbHelper = DbHelper(context)

    // ---------------- PRODUCTOS ----------------

    fun obtenerProductos(): List<Producto> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<Producto>()
        val cursor = db.query(
            DbHelper.TABLE_PRODUCTOS, null, null, null, null, null,
            "${DbHelper.COL_PROD_NOMBRE} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(cursorToProducto(it))
            }
        }
        return lista
    }

    fun contarProductos(): Int = obtenerProductos().size

    fun contarStockBajo(): Int = obtenerProductos().count { it.stockBajo }

    fun insertarProducto(nombre: String, categoria: String, precio: Double, stock: Int, stockMin: Int): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(DbHelper.COL_PROD_NOMBRE, nombre)
            put(DbHelper.COL_PROD_CATEGORIA, categoria)
            put(DbHelper.COL_PROD_PRECIO, precio)
            put(DbHelper.COL_PROD_STOCK, stock)
            put(DbHelper.COL_PROD_STOCK_MIN, stockMin)
        }
        return db.insert(DbHelper.TABLE_PRODUCTOS, null, cv)
    }

    fun actualizarStock(productoId: Long, nuevoStock: Int) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply { put(DbHelper.COL_PROD_STOCK, nuevoStock) }
        db.update(DbHelper.TABLE_PRODUCTOS, cv, "${DbHelper.COL_PROD_ID}=?", arrayOf(productoId.toString()))
    }

    fun obtenerProductoPorId(productoId: Long): Producto? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DbHelper.TABLE_PRODUCTOS, null,
            "${DbHelper.COL_PROD_ID}=?", arrayOf(productoId.toString()),
            null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) return cursorToProducto(it)
        }
        return null
    }

    fun actualizarProducto(
        productoId: Long, nombre: String, categoria: String, precio: Double, stock: Int, stockMin: Int
    ) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(DbHelper.COL_PROD_NOMBRE, nombre)
            put(DbHelper.COL_PROD_CATEGORIA, categoria)
            put(DbHelper.COL_PROD_PRECIO, precio)
            put(DbHelper.COL_PROD_STOCK, stock)
            put(DbHelper.COL_PROD_STOCK_MIN, stockMin)
        }
        db.update(DbHelper.TABLE_PRODUCTOS, cv, "${DbHelper.COL_PROD_ID}=?", arrayOf(productoId.toString()))
    }

    fun eliminarProducto(productoId: Long) {
        val db = dbHelper.writableDatabase
        db.delete(DbHelper.TABLE_PRODUCTOS, "${DbHelper.COL_PROD_ID}=?", arrayOf(productoId.toString()))
    }

    private fun cursorToProducto(c: android.database.Cursor): Producto {
        return Producto(
            id = c.getLong(c.getColumnIndexOrThrow(DbHelper.COL_PROD_ID)),
            nombre = c.getString(c.getColumnIndexOrThrow(DbHelper.COL_PROD_NOMBRE)),
            categoria = c.getString(c.getColumnIndexOrThrow(DbHelper.COL_PROD_CATEGORIA)) ?: "",
            precio = c.getDouble(c.getColumnIndexOrThrow(DbHelper.COL_PROD_PRECIO)),
            stock = c.getInt(c.getColumnIndexOrThrow(DbHelper.COL_PROD_STOCK)),
            stockMin = c.getInt(c.getColumnIndexOrThrow(DbHelper.COL_PROD_STOCK_MIN))
        )
    }

    // ---------------- CLIENTES / FIADOS ----------------

    fun obtenerOcrearCliente(nombre: String): Long {
        val db = dbHelper.writableDatabase
        val cursor = db.query(
            DbHelper.TABLE_CLIENTES, arrayOf(DbHelper.COL_CLI_ID),
            "${DbHelper.COL_CLI_NOMBRE}=?", arrayOf(nombre), null, null, null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndexOrThrow(DbHelper.COL_CLI_ID))
            }
        }
        val cv = ContentValues().apply { put(DbHelper.COL_CLI_NOMBRE, nombre) }
        return db.insert(DbHelper.TABLE_CLIENTES, null, cv)
    }

    fun insertarFiado(clienteNombre: String, monto: Double) {
        val clienteId = obtenerOcrearCliente(clienteNombre)
        val db = dbHelper.writableDatabase
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cv = ContentValues().apply {
            put(DbHelper.COL_FIADO_CLIENTE_ID, clienteId)
            put(DbHelper.COL_FIADO_MONTO, monto)
            put(DbHelper.COL_FIADO_FECHA, fecha)
            put(DbHelper.COL_FIADO_PAGADO, 0)
        }
        db.insert(DbHelper.TABLE_FIADOS, null, cv)
    }

    fun marcarFiadoPagado(fiadoId: Long) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply { put(DbHelper.COL_FIADO_PAGADO, 1) }
        db.update(DbHelper.TABLE_FIADOS, cv, "${DbHelper.COL_FIADO_ID}=?", arrayOf(fiadoId.toString()))
    }

    fun obtenerFiados(soloPendientes: Boolean = false): List<Fiado> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<Fiado>()
        val sql = """
            SELECT f.${DbHelper.COL_FIADO_ID}, f.${DbHelper.COL_FIADO_CLIENTE_ID},
                   c.${DbHelper.COL_CLI_NOMBRE}, f.${DbHelper.COL_FIADO_MONTO},
                   f.${DbHelper.COL_FIADO_FECHA}, f.${DbHelper.COL_FIADO_PAGADO}
            FROM ${DbHelper.TABLE_FIADOS} f
            JOIN ${DbHelper.TABLE_CLIENTES} c ON c.${DbHelper.COL_CLI_ID} = f.${DbHelper.COL_FIADO_CLIENTE_ID}
            ${if (soloPendientes) "WHERE f.${DbHelper.COL_FIADO_PAGADO} = 0" else ""}
            ORDER BY f.${DbHelper.COL_FIADO_FECHA} DESC
        """.trimIndent()
        val cursor = db.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    Fiado(
                        id = it.getLong(0),
                        clienteId = it.getLong(1),
                        clienteNombre = it.getString(2),
                        monto = it.getDouble(3),
                        fecha = it.getString(4) ?: "",
                        pagado = it.getInt(5) == 1
                    )
                )
            }
        }
        return lista
    }

    fun totalFiadoPendiente(): Double = obtenerFiados(soloPendientes = true).sumOf { it.monto }

    fun contarClientesConDeuda(): Int =
        obtenerFiados(soloPendientes = true).map { it.clienteId }.distinct().size

    // ---------------- VENTAS ----------------

    fun registrarVenta(productoId: Long?, productoNombre: String, cantidad: Int, total: Double) {
        val db = dbHelper.writableDatabase
        val now = Date()
        val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        val cv = ContentValues().apply {
            put(DbHelper.COL_VENTA_PRODUCTO_ID, productoId)
            put(DbHelper.COL_VENTA_PRODUCTO_NOMBRE, productoNombre)
            put(DbHelper.COL_VENTA_CANTIDAD, cantidad)
            put(DbHelper.COL_VENTA_TOTAL, total)
            put(DbHelper.COL_VENTA_FECHA, fecha)
            put(DbHelper.COL_VENTA_HORA, hora)
        }
        db.insert(DbHelper.TABLE_VENTAS, null, cv)

        // Si la venta corresponde a un producto del inventario, descuenta stock
        if (productoId != null) {
            val producto = obtenerProductos().find { it.id == productoId }
            if (producto != null) {
                val nuevoStock = (producto.stock - cantidad).coerceAtLeast(0)
                actualizarStock(productoId, nuevoStock)
            }
        }
    }

    fun obtenerVentasDeHoy(): List<Venta> {
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return obtenerVentasPorFecha(fechaHoy)
    }

    fun obtenerVentasPorFecha(fecha: String): List<Venta> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<Venta>()
        val cursor = db.query(
            DbHelper.TABLE_VENTAS, null,
            "${DbHelper.COL_VENTA_FECHA}=?", arrayOf(fecha),
            null, null, "${DbHelper.COL_VENTA_HORA} ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    Venta(
                        id = it.getLong(it.getColumnIndexOrThrow(DbHelper.COL_VENTA_ID)),
                        productoId = it.getLong(it.getColumnIndexOrThrow(DbHelper.COL_VENTA_PRODUCTO_ID)),
                        productoNombre = it.getString(it.getColumnIndexOrThrow(DbHelper.COL_VENTA_PRODUCTO_NOMBRE)) ?: "",
                        cantidad = it.getInt(it.getColumnIndexOrThrow(DbHelper.COL_VENTA_CANTIDAD)),
                        total = it.getDouble(it.getColumnIndexOrThrow(DbHelper.COL_VENTA_TOTAL)),
                        fecha = it.getString(it.getColumnIndexOrThrow(DbHelper.COL_VENTA_FECHA)) ?: "",
                        hora = it.getString(it.getColumnIndexOrThrow(DbHelper.COL_VENTA_HORA)) ?: ""
                    )
                )
            }
        }
        return lista
    }

    /** Devuelve cada fecha con ventas registradas junto con el total de ese día, más reciente primero. */
    fun obtenerHistorialPorDia(): List<Pair<String, Double>> {
        val db = dbHelper.readableDatabase
        val resultado = mutableListOf<Pair<String, Double>>()
        val sql = """
            SELECT ${DbHelper.COL_VENTA_FECHA}, SUM(${DbHelper.COL_VENTA_TOTAL})
            FROM ${DbHelper.TABLE_VENTAS}
            GROUP BY ${DbHelper.COL_VENTA_FECHA}
            ORDER BY ${DbHelper.COL_VENTA_FECHA} DESC
        """.trimIndent()
        val cursor = db.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                resultado.add(it.getString(0) to it.getDouble(1))
            }
        }
        return resultado
    }

    fun totalVendidoHoy(): Double = obtenerVentasDeHoy().sumOf { it.total }

    fun productosMasVendidosHoy(): List<Pair<String, Int>> {
        return obtenerVentasDeHoy()
            .groupBy { it.productoNombre }
            .map { (nombre, ventas) -> nombre to ventas.sumOf { it.cantidad } }
            .sortedByDescending { it.second }
    }
}
