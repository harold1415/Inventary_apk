package com.example.myinventarioapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myinventarioapp.ui.model.Local
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// CONSTRUCTOR PARA CREAR LA LISTA DE VARIANTES
data class VarianteProducto(
    val id: String,
    val nombre: String,
    val modeloCod: String,
    val talla: String,
    val color: String,
    val local: String,
    val stock: Int,
    val codigo: String
)

class TranferViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // ─────────────────────────────────────────────────────────────
    // LOCALES
    // ─────────────────────────────────────────────────────────────

    private val _locales = MutableStateFlow<List<Local>>(emptyList())
    val locales = _locales.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // ESTADOS DE LA TRANSFERENCIA
    // ─────────────────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isTransferring = MutableStateFlow(false)
    val isTransferring = _isTransferring.asStateFlow()

    private val _todasLasVariantes =
        MutableStateFlow<List<VarianteProducto>>(emptyList())
    val todasLasVariantes = _todasLasVariantes.asStateFlow()

    private val _modeloNombre = MutableStateFlow("")
    val modeloNombre = _modeloNombre.asStateFlow()

    private val _tallaSeleccionada = MutableStateFlow("")
    val tallaSeleccionada = _tallaSeleccionada.asStateFlow()

    private val _colorSeleccionado = MutableStateFlow("")
    val colorSeleccionado = _colorSeleccionado.asStateFlow()

    private val _sucursalOrigenSeleccionada = MutableStateFlow("")
    val sucursalOrigenSeleccionada = _sucursalOrigenSeleccionada.asStateFlow()

    // ─────────────────────────────────────────────────────────────
    // INICIO
    // ─────────────────────────────────────────────────────────────

    init {

        // Cargar locales
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            val lista = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Local::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            _locales.value = lista
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR PRODUCTO
    // ─────────────────────────────────────────────────────────────

    fun buscarPorCodigo(
        codigo: String,
        onProductoNoEncontrado: () -> Unit = {}
    ) {
        if (codigo.isBlank()) return

        _isLoading.value = true
        _todasLasVariantes.value = emptyList()
        _modeloNombre.value = ""

        db.collection("productos")
            .whereEqualTo("codigo", codigo)
            .get()
            .addOnSuccessListener { skuSnapshot ->

                if (!skuSnapshot.isEmpty) {

                    val doc = skuSnapshot.documents.first()

                    val modeloCod = doc.getString("modeloCod") ?: ""
                    _tallaSeleccionada.value = doc.getString("talla") ?: ""
                    _colorSeleccionado.value = doc.getString("color") ?: ""
                    _sucursalOrigenSeleccionada.value = doc.getString("local") ?: ""

                    buscarVariantesPorModelo(
                        modeloCod = modeloCod,
                        onProductoNoEncontrado = onProductoNoEncontrado
                    )

                } else {

                    // Si no encontró SKU, busca directamente por modeloCod
                    db.collection("productos")
                        .whereEqualTo("modeloCod", codigo)
                        .get()
                        .addOnSuccessListener { modeloSnapshot ->

                            if (!modeloSnapshot.isEmpty) {

                                val variantes =
                                    modeloSnapshot.documents.mapNotNull { d ->
                                        VarianteProducto(
                                            id = d.id,
                                            nombre = d.getString("nombre") ?: "",
                                            modeloCod = d.getString("modeloCod") ?: "",
                                            talla = d.getString("talla") ?: "",
                                            color = d.getString("color") ?: "",
                                            local = d.getString("local") ?: "",
                                            stock = (d.getLong("stock") ?: 0L).toInt(),
                                            codigo = d.getString("codigo") ?: ""
                                        )
                                    }

                                _todasLasVariantes.value = variantes
                                _modeloNombre.value =
                                    variantes.firstOrNull()?.nombre ?: ""

                            } else {

                                onProductoNoEncontrado()
                            }

                            _isLoading.value = false
                        }
                        .addOnFailureListener {
                            _isLoading.value = false
                        }
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    // ─────────────────────────────────────────────────────────────
    // BUSCAR VARIANTES DEL MODELO
    // ─────────────────────────────────────────────────────────────

    private fun buscarVariantesPorModelo(
        modeloCod: String,
        onProductoNoEncontrado: () -> Unit = {}
    ) {

        db.collection("productos")
            .whereEqualTo("modeloCod", modeloCod)
            .get()
            .addOnSuccessListener { modeloSnapshot ->

                if (!modeloSnapshot.isEmpty) {

                    val variantes =
                        modeloSnapshot.documents.mapNotNull { d ->
                            VarianteProducto(
                                id = d.id,
                                nombre = d.getString("nombre") ?: "",
                                modeloCod = d.getString("modeloCod") ?: "",
                                talla = d.getString("talla") ?: "",
                                color = d.getString("color") ?: "",
                                local = d.getString("local") ?: "",
                                stock = (d.getLong("stock") ?: 0L).toInt(),
                                codigo = d.getString("codigo") ?: ""
                            )
                        }

                    _todasLasVariantes.value = variantes
                    _modeloNombre.value =
                        variantes.firstOrNull()?.nombre ?: ""

                } else {

                    onProductoNoEncontrado()
                }

                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIRMAR TRANSFERENCIA
    // ─────────────────────────────────────────────────────────────

    fun confirmarTransferencia(
        cantidad: String,
        varianteOrigen: VarianteProducto?,
        sucursalDestinoSeleccionada: String,
        onSuccess: (cantidadInt: Int, sucursalDestino: String) -> Unit = { _, _ -> },
        onError: (mensaje: String) -> Unit = {}
    ) {

        val cantidadInt = cantidad.toIntOrNull() ?: 0

        if (cantidadInt <= 0) {
            onError("Ingresa una cantidad válida")
            return
        }

        if (varianteOrigen == null) {
            onError("Selecciona el producto origen")
            return
        }

        if (sucursalDestinoSeleccionada.isBlank()) {
            onError("Selecciona la sucursal destino")
            return
        }

        if (varianteOrigen.local == sucursalDestinoSeleccionada) {
            onError("Origen y destino no pueden ser iguales")
            return
        }

        if (cantidadInt > varianteOrigen.stock) {
            onError(
                "Stock insuficiente (disponible: ${varianteOrigen.stock})"
            )
            return
        }

        _isTransferring.value = true

        db.collection("productos")
            .whereEqualTo("modeloCod", varianteOrigen.modeloCod)
            .whereEqualTo("talla", varianteOrigen.talla)
            .whereEqualTo("color", varianteOrigen.color)
            .whereEqualTo("local", sucursalDestinoSeleccionada)
            .get()
            .addOnSuccessListener { destinoSnapshot ->

                val batch = db.batch()

                val origenRef =
                    db.collection("productos").document(varianteOrigen.id)

                batch.update(
                    origenRef,
                    "stock",
                    FieldValue.increment(-cantidadInt.toLong())
                )

                if (!destinoSnapshot.isEmpty) {

                    val destinoRef =
                        db.collection("productos")
                            .document(destinoSnapshot.documents.first().id)

                    batch.update(
                        destinoRef,
                        "stock",
                        FieldValue.increment(cantidadInt.toLong())
                    )

                } else {

                    val nuevoDoc =
                        db.collection("productos").document()

                    val nuevaVariante = hashMapOf(
                        "codigo" to "PRD${
                            System.currentTimeMillis()
                                .toString()
                                .takeLast(4)
                        }${(10..99).random()}",
                        "nombre" to varianteOrigen.nombre,
                        "modeloCod" to varianteOrigen.modeloCod,
                        "talla" to varianteOrigen.talla,
                        "color" to varianteOrigen.color,
                        "local" to sucursalDestinoSeleccionada,
                        "stock" to cantidadInt
                    )

                    batch.set(nuevoDoc, nuevaVariante)
                }

                batch.commit()
                    .addOnSuccessListener {

                        _isTransferring.value = false

                        onSuccess(
                            cantidadInt,
                            sucursalDestinoSeleccionada
                        )
                    }
                    .addOnFailureListener {

                        _isTransferring.value = false

                        onError(
                            "Error: ${it.message}"
                        )
                    }
            }
            .addOnFailureListener {

                _isTransferring.value = false

                onError("Error al verificar destino")
            }
    }
}