package com.example.myinventarioapp.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myinventarioapp.ui.screens.Local
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

// 🔹 Estado de las métricas del dashboard. Se agrupan en una sola data class
// para que la UI observe un solo StateFlow en vez de varios sueltos.
data class HomeMetrics(
    val ventasTotales: Double = 0.0,
    val gananciaTotal: Double = 0.0,
    val cantidadVentas: Int = 0,
    val unidadesVendidas: Long = 0,
    val ticketPromedio: Double = 0.0,
    val totalDescuentos:Double = 0.0
)

// Una fila del ranking de suscursales
data class SucursalMetric(
    val nombre: String,
    val total: Double,
    val porcentaje: Float
)

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Lista de sucursales para el selector
    private val _locales = MutableStateFlow<List<Local>>(emptyList())
    val locales: StateFlow<List<Local>> = _locales

    // Sucursal actualmente elegida por el usuario (null = todavía no eligió)
    private val _sucursalSeleccionada = MutableStateFlow<String?>(null)
    val sucursalSeleccionada: StateFlow<String?> = _sucursalSeleccionada

    //Fecha seleccionada - por defecto hoy
    private val _fechaSeleccionada = MutableStateFlow(Date())
    val fechaSeleccionada: StateFlow<Date> = _fechaSeleccionada

    // Métricas calculadas para la sucursal elegida
    private val _metrics = MutableStateFlow(HomeMetrics())
    val metrics: StateFlow<HomeMetrics> = _metrics

    //Top sucursales calculado por firebase
    private val _topSucursales = MutableStateFlow<List<SucursalMetric>>(emptyList())
    val topSucursales: StateFlow<List<SucursalMetric>> =  _topSucursales

    // Estado de carga mientras se consulta Firestore
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        cargarLocales()
        // Al iniciar carga con todos los locales y en fecha HOY por defecto
        cargarMetricas("",Date())
    }

    // 🔹 Carga la lista de sucursales (mismo patrón que usas en LocalScreen/InventarioScreen)
    private fun cargarLocales() {
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _locales.value = it.documents.mapNotNull { doc ->
                    doc.toObject(Local::class.java)?.copy(id = doc.id)
                }
            }
        }
    }

    // 🔹 Se llama cuando el usuario elige una sucursal en el dropdown.
    // "Todos los locales" se representa con sucursal = "" (string vacío),
    // igual que en InventarioScreen y ReportScreen, para mantener consistencia.
    fun seleccionarSucursal(sucursal: String) {
        _sucursalSeleccionada.value = sucursal
        cargarMetricas(sucursal, _fechaSeleccionada.value)
    }
    fun seleccionarFecha(fecha:Date){
        _fechaSeleccionada.value =fecha
        cargarMetricas(_sucursalSeleccionada.value ?: "", fecha)
    }

    // 🔹 Calcula el rango de "hoy": desde medianoche hasta este momento
    private fun inicioDelDia(fecha: Date): Timestamp {
        val cal = Calendar.getInstance()
        cal.time =fecha
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return Timestamp(cal.time)
    }
    // Fin del dia (23:59:59) de la feha elegida
    private fun finDelDia(fecha: Date): Timestamp{
        val cal= Calendar.getInstance()
        cal.time = fecha
        cal.set(Calendar.HOUR_OF_DAY,23)
        cal.set(Calendar.MINUTE,59)
        cal.set(Calendar.SECOND,59)
        cal.set(Calendar.MILLISECOND,999)
        return Timestamp(cal.time)
    }

    private fun cargarMetricas(sucursal: String,fecha: Date) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val inicio = inicioDelDia(fecha)
                val fin = finDelDia(fecha)

                // 🔹 Query base: ventas dentro del rango del dia elegido
                var query = db.collection("ventas")
                    .whereGreaterThanOrEqualTo("fecha", inicio)
                    .whereLessThanOrEqualTo("fecha", fin)

                // 🔹 Si eligió una sucursal específica (no "Todos los locales"), filtramos también por ella
                if (sucursal.isNotBlank()) {
                    query = query.whereEqualTo("sucursal", sucursal)
                }

                val snapshot = query.get().await()

                var totalVentas = 0.0
                var totalGanancia = 0.0
                var totalUnidades = 0L
                var totalDescuentos = 0.0
                val porSucursal = mutableMapOf<String, Double>()

                snapshot.documents.forEach { doc ->
                    val montoVenta = doc.getDouble("totalGen") ?: 0.0
                    totalVentas += montoVenta
                    totalGanancia += doc.getDouble("ganancia") ?: 0.0

                    //Suma unidades y descuentos de cada producto dentro de la venta
                    val productos = doc.get("productos") as? List<Map<String, Any>> ?: emptyList()
                    productos.forEach { prod ->
                        totalUnidades += (prod["cantidad"] as? Long) ?: 0L
                        totalDescuentos += (prod["descuento"] as? Double) ?: 0.0
                    }

                    // Acumula por sucursal para el raking
                    val suc = doc.getString("sucursal") ?: "Sin sucursal"
                    porSucursal[suc] = (porSucursal[suc] ?: 0.0) + montoVenta
                }
                val cantVentas = snapshot.size()
                val ticketProm = if(cantVentas > 0 ) totalVentas / cantVentas else 0.0


                _metrics.value = HomeMetrics(
                    ventasTotales = totalVentas,
                    gananciaTotal = totalGanancia,
                    cantidadVentas = cantVentas,
                    unidadesVendidas = totalUnidades,
                    ticketPromedio = ticketProm,
                    totalDescuentos = totalDescuentos
                )
                // construir top sucursales ordenado  de mayor a menor
                _topSucursales.value = porSucursal.entries
                    .sortedByDescending { it.value }
                    .map { (nombre, total) ->
                        SucursalMetric(
                            nombre = nombre,
                            total = total,
                            porcentaje = if(totalVentas > 0)(total / totalVentas).toFloat() else 0f
                        )
                    }
            } catch (e: Exception) {
                // 🔹 Si falla (ej. falta un índice compuesto en Firestore para
                // fecha + sucursal), dejamos las métricas en 0 en vez de crashear.
                _metrics.value = HomeMetrics()
            } finally {
                _isLoading.value = false
            }
        }
    }
}