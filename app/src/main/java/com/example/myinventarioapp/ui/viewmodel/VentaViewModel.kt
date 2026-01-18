package com.example.myinventarioapp.ui.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// ----------------------------------
// MODELOS DE DATOS
// ----------------------------------
//clase que contruye al escojer un producto
data class ProductoU(
    val id: String = "",
    val nombre: String = "",
    val local:String ="",
    val talla: String = "",
    val stock: Long = 0,
    val precio: Double = 0.0,
    val codigo: String = "",
    val precioXMayor: Double = 0.0,
    val costo: Double = 0.0,
)

//CLASE PARA AGREGAR PRODUCTO A LA LISTA DE DETAILVENTA
data class Producto(
    val codigo: String = "",
    val nombre: String = "",
    val talla:String="",
    val local: String = "",
    val cantidad: Long = 0,
    val descuento: Double = 0.0,
    val costo: Double = 0.0,
    val precio: Double = 0.0,
    val ganancia: Double =0.0,
    val total: Double = 0.0,
)

data class Venta(
    val id: String = "",
    val cliente: String? = null,
    val dni: String? = null,
    val productos: List<Producto> = emptyList(),
    val totalGen: Double = 0.0,
    val vendedor: String? = null,
    val ganancia: Double = 0.0,
    val sucursal : String ="",
    val fecha: Timestamp? = null
)

// ----------------------------------
// VIEWMODEL
// ----------------------------------
class VentaViewModel : ViewModel() {

    // Lista observable de productos en la venta
    val productos = mutableStateListOf<Producto>()

    // Producto seleccionado desde Search
    private val _oneproduct = mutableStateOf<ProductoU?>(null)
    val oneproduct: State<ProductoU?> = _oneproduct

    private val db = FirebaseFirestore.getInstance()

    private val _insuficientes = MutableStateFlow<List<Producto>>(emptyList())
    val insuficientes: StateFlow<List<Producto>> = _insuficientes

    private val _stockActual = MutableStateFlow<Map<String, Long>>(emptyMap())
    val stockActual: StateFlow<Map<String, Long>> = _stockActual

    private val _ventaActual = MutableStateFlow<Venta?>(null)
    val ventaActual: StateFlow<Venta?> = _ventaActual

    private var productosOriginales: List<Producto> = emptyList()
    var ventaYaCargada = false
    var productosModificados = false
    var ventaActualId: String? = null
    var ventaLocal:String = ""
    var hola : String =""

    // ----------------------------------
    // FUNCIONES AUXILIARES
    // ----------------------------------
    fun resetearCarga() {
        ventaYaCargada = false
    }

    fun chooseProduct(
        id: String,
        nombre: String,
        local:String,
        talla: String,
        stock: Long,
        precio: Double,
        codigo: String,
        precioXMayor: Double,
        costo: Double,
    ) {
        _oneproduct.value = ProductoU(id, nombre,local, talla, stock, precio, codigo, precioXMayor, costo)
        Log.d("VentaViewModel", "Producto seleccionado: $_oneproduct")
    }

    fun resetProduct() {
        _oneproduct.value = null
    }

    fun agregarProducto(
        codigo: String,
        nombre: String,
        talla: String,
        local: String,
        cantidad: Long,
        descuento: Double,
        costo: Double,
        precio: Double,
        ganancia:Double,
        total: Double
    ) {
        ventaLocal = local
        val nuevoProducto = Producto(codigo, nombre,talla,local, cantidad, descuento, costo, precio,ganancia, total)
        productos.add(nuevoProducto)
        productosModificados = true
        Log.d("VentaViewModel", "Producto agregado: $productos")
    }

    fun eliminarProducto(index: Int) {
        if (index in productos.indices) {
            productos.removeAt(index)
            productosModificados = true
        }
    }

    fun updateProducto(index: Int, nombre: String, cantidad: Long) {
        if (index in productos.indices) {
            val productoActual = productos[index]
            productos[index] = productoActual.copy(nombre = nombre, cantidad = cantidad)
            productosModificados = true
        }
    }

    fun clearProductos() {
        productos.clear()
    }

    // ----------------------------------
    // CARGAR VENTA
    // ----------------------------------
    var fechaOriginal: Timestamp? = null

    fun cargarVenta(ventaId: String) {
        if (ventaYaCargada && ventaActualId == ventaId) {
            if (!productosModificados) {
                productos.clear()
                productos.addAll(productosOriginales)
            }
            return
        }

        viewModelScope.launch {
            try {
                productos.clear()
                _ventaActual.value = null
                ventaYaCargada = true
                productosModificados = false
                ventaActualId = ventaId

                val document = db.collection("ventas").document(ventaId).get().await()
                if (document.exists()) {
                    val venta = document.toObject(Venta::class.java)
                    _ventaActual.value = venta
                    fechaOriginal = venta?.fecha
                    ventaLocal = venta?.sucursal ?:""
                    Log.d("truefecha", "fechaOriginal: $fechaOriginal")
                    productosOriginales = venta?.productos?.toList() ?: emptyList()
                    productos.addAll(productosOriginales)
                    Log.d("VentaViewModel", "Venta cargada correctamente: ${venta?.id}")
                } else {
                    Log.w("VentaViewModel", "No existe la venta con ID: $ventaId")
                }
            } catch (e: Exception) {
                Log.e("VentaViewModel", "Error al cargar venta", e)
            }
        }
    }

    // ----------------------------------
    // FUNCION CALCULAR TOTAL
    // ----------------------------------
//    fun calcularTotal(
//        precio: Double,
//        cantidad: Long,
//        tipoDescuento: String?,
//        descuentoUnit: Double,
//        descuentoGeneral: Double
//    ): Pair<Double, Double> {
//        return when (tipoDescuento) {
//            "unit" -> {
//                val desc = cantidad * descuentoUnit
//                val total = cantidad * (precio - descuentoUnit)
//                Pair(desc, total)
//            }
//            "general" -> {
//                val total = (cantidad * precio) - descuentoGeneral
//                Pair(descuentoGeneral, total)
//            }
//            else -> {
//                val total = cantidad * precio
//                Pair(0.0, total)
//            }
//        }
//    }

    // ----------------------------------
    // GUARDAR VENTA
    // ----------------------------------
    fun guardarVenta(
        context: Context,
        cliente: String?,
        dni: String?,
        vendedor: String?,
        ganancia: Double,
        sucursal: String,
        onSuccess: () -> Unit
    ) {
        if (productos.isEmpty()) {
            Toast.makeText(context, "Agrega al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        val codigos = productos.map { it.codigo }
        db.collection("productos").whereIn("codigo", codigos).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(context, "No se encontraron productos en Firestore", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val stockActualMap = snapshot.documents.associate { doc ->
                    val codigo = doc.getString("codigo") ?: ""
                    val stock = doc.getLong("stock") ?: 0L
                    codigo to stock
                }

                val insuficientes = productos.filter { p ->
                    val disponible = stockActualMap[p.codigo] ?: 0L
                    p.cantidad > disponible
                }

                if (insuficientes.isNotEmpty()) {
                    _insuficientes.value = insuficientes
                    return@addOnSuccessListener
                }

                val totalVenta = productos.sumOf { it.total }

                val ventaRef = db.collection("ventas").document()
                val ventaData = hashMapOf(
                    "id" to ventaRef.id,
                    "cliente" to cliente,
                    "dni" to dni,
                    "productos" to productos.toList(),
                    "vendedor" to vendedor,
                    "totalGen" to totalVenta,
                    "sucursal" to sucursal,
                    "ganancia" to ganancia,
                    "fecha" to FieldValue.serverTimestamp()
                )

                val batch = db.batch()
                productos.forEach { p ->
                    val prodDoc = snapshot.documents.firstOrNull { it.getString("codigo") == p.codigo }
                    if (prodDoc != null) {
                        val ref = db.collection("productos").document(prodDoc.id)
                        batch.update(ref, "stock", FieldValue.increment(-p.cantidad))
                    }
                }

                batch.set(ventaRef, ventaData)
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Venta registrada y stock actualizado", Toast.LENGTH_SHORT).show()
                        clearProductos()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al verificar stock", Toast.LENGTH_SHORT).show()
            }
    }

    // ----------------------------------
    // ACTUALIZAR VENTA
    // ----------------------------------
    fun actualizarVenta(
        context: Context,
        ventaId: String,
        cliente: String?,
        dni: String?,
        vendedor: String?,
        fecha: String?,
        ganancia:Double,
        sucursal: String,
        onSuccess: () -> Unit
    ) {
        Log.d("truefecha1", "fechaOriginal: $fechaOriginal")
        if (productos.isEmpty()) {
            Toast.makeText(context, "Agrega al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        val codigos = productos.map { it.codigo }
        db.collection("productos").whereIn("codigo", codigos).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener

                val stockActualMap = snapshot.documents.associate { doc ->
                    val codigo = doc.getString("codigo") ?: ""
                    val stock = doc.getLong("stock") ?: 0L
                    codigo to stock
                }

//                val insuficientes = productos.filter { p ->
//                    val disponible = stockActualMap[p.codigo] ?: 0L
//                    p.cantidad > disponible
//                }
                // 🔹 Validar solo los que aumentan cantidad
                val insuficientes = productos.filter { p ->
                    val original = productosOriginales.firstOrNull { it.codigo == p.codigo }?.cantidad ?: 0L
                    val diferencia = p.cantidad - original
                    val disponible = stockActualMap[p.codigo] ?: 0L
                    diferencia > 0 && diferencia > disponible
                }

                if (insuficientes.isNotEmpty()) {
                    _insuficientes.value = insuficientes
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                productos.forEach { p ->
                    val prodDoc = snapshot.documents.firstOrNull { it.getString("codigo") == p.codigo }
                    if (prodDoc != null) {
                        val ref = db.collection("productos").document(prodDoc.id)
                        val cantidadOriginal = productosOriginales.firstOrNull { it.codigo == p.codigo }?.cantidad ?: 0L
                        val diferencia = p.cantidad - cantidadOriginal
                        if (diferencia != 0L) {
                            batch.update(ref, "stock", FieldValue.increment(-diferencia))
                        }
                    }
                }
//                // Manejo de fecha usando Timestamp directamente  _ventaActual.value?.fecha
//                val fechaTimestamp: Timestamp = try {
//                    fecha?.let {
//                        // Si el usuario cambió la fecha desde la UI
//                        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//                        val date = formato.parse(it.trim())
//                        hola = "modificamos"
//                        Log.d("hola", ":$hola")
//                        Timestamp(date!!)
//                    } ?: fechaOriginal ?: Timestamp.now() // usa Timestamp original
//
//                } catch (e: Exception) {
//                    hola = "no hay nada"
//                    Log.d("hola", ":$hola")
//                    Timestamp.now() // fallback seguro
//                }
                Log.d("tras", ":$fecha")
                val fechaTimestamp: Timestamp = if (!fecha.isNullOrBlank()) {
                    // El usuario cambió la fecha: parse seguro
                    val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val date = formato.parse(fecha.trim())
                    if (date != null) Timestamp(date) else fechaOriginal ?: Timestamp.now()
                } else {
                    // No se cambió: usa la fecha original directamente
                    fechaOriginal ?: Timestamp.now()
                }

                val totalVenta = productos.sumOf { it.total }
                val ventaRef = db.collection("ventas").document(ventaId)
                val ventaData = hashMapOf(
                    "cliente" to cliente,
                    "dni" to dni,
                    "productos" to productos.toList(),
                    "totalGen" to totalVenta,
                    "vendedor" to vendedor,
                    "sucursal" to sucursal,
                    "ganancia" to ganancia,
                    "fecha" to fechaTimestamp
                )

                batch.update(ventaRef, ventaData)
                batch.commit()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Venta actualizada", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    fun limpiarVenta() {
        _ventaActual.value = null
    }

    fun limpiarInsuficientes() {
        _insuficientes.value = emptyList()
    }
}
