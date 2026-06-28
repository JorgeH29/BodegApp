package com.bodegapp.app.data

data class Producto(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val precio: Double,
    val stock: Int,
    val stockMin: Int
) {
    val stockBajo: Boolean get() = stock <= stockMin
    val sinStock: Boolean get() = stock == 0
}

data class Cliente(
    val id: Long,
    val nombre: String
)

data class Fiado(
    val id: Long,
    val clienteId: Long,
    val clienteNombre: String,
    val monto: Double,
    val fecha: String,
    val pagado: Boolean
)

data class Venta(
    val id: Long,
    val productoId: Long?,
    val productoNombre: String,
    val cantidad: Int,
    val total: Double,
    val fecha: String,
    val hora: String
)
