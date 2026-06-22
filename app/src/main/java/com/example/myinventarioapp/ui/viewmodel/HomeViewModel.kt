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

// 🔹 Estado de las métricas del dashboard. Se agrupan en una sola data class
// para que la UI observe un solo StateFlow en vez de varios sueltos.
data class HomeMetrics(
    val ventasTotales: Double = 0.0,
    val gananciaTotal: Double = 0.0,
    val cantidadVentas: Int = 0
)

class HomeViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // Lista de sucursales para el selector
    private val _locales = MutableStateFlow<List<Local>>(emptyList())
    val locales: StateFlow<List<Local>> = _locales

    // Sucursal actualmente elegida por el usuario (null = todavía no eligió)
    private val _sucursalSeleccionada = MutableStateFlow<String?>(null)
    val sucursalSeleccionada: StateFlow<String?> = _sucursalSeleccionada

    // Métricas calculadas para la sucursal elegida
    private val _metrics = MutableStateFlow(HomeMetrics())
    val metrics: StateFlow<HomeMetrics> = _metrics

    // Estado de carga mientras se consulta Firestore
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        cargarLocales()
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
        cargarMetricasDelDia(sucursal)
    }

    // 🔹 Calcula el rango de "hoy": desde medianoche hasta este momento
    private fun obtenerInicioDelDia(): Timestamp {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Timestamp(calendar.time)
    }

    private fun cargarMetricasDelDia(sucursal: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val inicioDelDia = obtenerInicioDelDia()

                // 🔹 Query base: ventas de hoy en adelante
                var query = db.collection("ventas")
                    .whereGreaterThanOrEqualTo("fecha", inicioDelDia)

                // 🔹 Si eligió una sucursal específica (no "Todos los locales"), filtramos también por ella
                if (sucursal.isNotBlank()) {
                    query = query.whereEqualTo("sucursal", sucursal)
                }

                val snapshot = query.get().await()

                var totalVentas = 0.0
                var totalGanancia = 0.0

                snapshot.documents.forEach { doc ->
                    totalVentas += doc.getDouble("totalGen") ?: 0.0
                    totalGanancia += doc.getDouble("ganancia") ?: 0.0
                }

                _metrics.value = HomeMetrics(
                    ventasTotales = totalVentas,
                    gananciaTotal = totalGanancia,
                    cantidadVentas = snapshot.size()
                )
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