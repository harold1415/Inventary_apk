package com.example.myinventarioapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import com.example.myinventarioapp.ui.viewmodel.Venta
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.text.contains
import kotlin.text.ifEmpty
import kotlin.text.isBlank


fun formatFecha(fecha: Timestamp?): String {
    return if (fecha != null) {
        val date = fecha.toDate()
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        format.format(date)
    } else {
        "Sin fecha"
    }
}


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VentaScreen(onNavigateToDetailVenta: (String) -> Unit, ventaViewModel: VentaViewModel) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var ventas by remember { mutableStateOf(listOf<Venta>()) }
    var locales by remember { mutableStateOf(listOf<Local>()) }
    var mostrarEditDialogo by remember { mutableStateOf(false) }
    var ventaSeleccionada by remember { mutableStateOf<Venta?>(null) } // 👈 venta para el diálogo de editar
    var eliminarDialog by remember { mutableStateOf(false) }
    val insuficientes by ventaViewModel.insuficientes.collectAsState()
    val stockActual by ventaViewModel.stockActual.collectAsState()
    var filtredLocal by remember { mutableStateOf(false) }
    var selectedLocal by remember { mutableStateOf("") }

    // Leer ventas desde Firestore
    LaunchedEffect(Unit) {
        db.collection("ventas").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                ventas = it.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Venta::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        // 👇 Si un documento no coincide, lo ignoramos
                        Log.e("VentaScreen", "Error parseando venta: ${e.message}")
                        null
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                locales = it.documents.mapNotNull { doc ->
                    doc.toObject(Local::class.java)?.copy(id = doc.id)
                }
            }
        }
    }
//    val ventasOrdenadas = ventas.sortedByDescending { it.fecha }
    val productosFiltrados = ventas
        .sortedByDescending { it.fecha } //ordena por fecha primero
        .filter { venta ->
        val coincideLocal = selectedLocal.isBlank() ||
                venta.sucursal.equals(selectedLocal, ignoreCase = true)
        coincideLocal
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("\uD83E\uDDFE Registro de Ventas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                ventaViewModel.clearProductos()
                ventaViewModel.resetProduct()
                onNavigateToDetailVenta("New")
            }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Venta")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stickyHeader {
                // Poner un fondo (Surface o background) evita que el contenido scrolleado
                // se vea "por debajo" del header pegado.
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ExposedDropdownMenuBox(
                                        expanded = filtredLocal,
                                        onExpandedChange = { filtredLocal = !filtredLocal }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedLocal.ifEmpty { "Todos los locales" },
                                            onValueChange = {},
                                            readOnly = true,
                                            shape = RoundedCornerShape(16.dp), // Aquí defines el redondeo
                                            label = { Text("Seleccionar sucursal") },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = filtredLocal)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(
                                                    type = MenuAnchorType.PrimaryNotEditable,
                                                    enabled = true
                                                )
                                        )

                                        DropdownMenu(
                                            expanded = filtredLocal,
                                            onDismissRequest = { filtredLocal = false }
                                        ) {
                                            // 🔹 Opción para mostrar todos los locales
                                            DropdownMenuItem(
                                                text = { Text("Todos los locales") },
                                                onClick = {
                                                    selectedLocal = ""
                                                    filtredLocal = false
                                                }
                                            )
                                            locales.forEach { local ->
                                                DropdownMenuItem(
                                                    text = { Text("Sucursal ${local.nombre}") },
                                                    onClick = {
                                                        selectedLocal = local.nombre
                                                        filtredLocal = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }

                        }
                    }
                }
            }
            items(productosFiltrados) { venta ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f))
                        {
                            Text(
                                "Venta: #${venta.id.take(6)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Sucursal: ${venta.sucursal}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Cliente: ${venta.cliente ?: "Sin nombre"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Vendedor: ${venta.vendedor ?: "Sin nombre"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Fecha: ${formatFecha(venta.fecha)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                        }
                        //BOTONES DE ACCION
                        Column(
                            modifier = Modifier.wrapContentWidth(),
                            verticalArrangement = Arrangement.spacedBy(0.dp) // separación entre botones
                        ) {
                            IconButton(
                                onClick = {
                                    ventaSeleccionada = venta
                                    mostrarEditDialogo = true
                                },
                                modifier = Modifier.size(36.dp) // ⬅️ tamaño del botón
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = "Ver",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp) // ⬅️ tamaño del ícono
                                )
                            }
                            IconButton(
                                onClick = {
                                    // Pasamos el ID como argumento de la ruta
                                    Log.d("VentaScreen", "ID de venta al editar: ${venta.id}")
                                    ventaViewModel.productosModificados = false
                                    ventaViewModel.ventaYaCargada = false
                                    onNavigateToDetailVenta(venta.id) // 👈 solo el ID
                                },
                                modifier = Modifier.size(36.dp) // ⬅️ tamaño del botón
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp) // ⬅️ tamaño del ícono
                                )
                            }
                            IconButton(
                                onClick = {
                                    eliminarDialog = true
                                    ventaSeleccionada = venta
                                },
                                modifier = Modifier.size(36.dp) // ⬅️ tamaño del botón
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp) // ⬅️ tamaño del ícono
                                )
                            }
                        }
                    }
                }
            }
        }
        // 👀 Dialogo para ver detalle de la venta
        if (mostrarEditDialogo && ventaSeleccionada != null) {
            Log.d("venta", ":${ventaSeleccionada!!.productos}")
            AlertDialog(
                onDismissRequest = { mostrarEditDialogo = false },
                title = { Text("Detalle de la venta") },
                text = {
                    Column {
                        Text(
                            "Vendedor: ${ventaSeleccionada!!.vendedor ?: "Sin nombre"}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Sucursal: ${ventaSeleccionada!!.sucursal}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Cliente: ${ventaSeleccionada!!.cliente ?: "Sin nombre"}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "DNI: ${ventaSeleccionada!!.dni ?: "No registrado"}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Fecha: ${formatFecha(ventaSeleccionada!!.fecha)}",
                            fontWeight = FontWeight.Bold
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Productos:", fontWeight = FontWeight.Bold)
                        Text(
                            "Talla     Cant.    Precio    Desc.   Total  ",
                            fontSize = 15.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ventaSeleccionada!!.productos.forEach {
                            Column {

                                Text("• " + run {
                                    val palabras = it.nombre.split(" ")
                                    if (palabras.size <= 2 || it.nombre.length <= 35) it.nombre
                                    else {
                                        val ultimas = palabras.takeLast(2).joinToString(" ")
                                        val resto = palabras.dropLast(2).joinToString(" ")
                                        val restoCortado =
                                            if (resto.length > (25 - ultimas.length - 4))
                                                resto.take(25 - ultimas.length - 4) + "..."
                                            else resto
                                        "$restoCortado $ultimas"
                                    }
                                })
                                // Formato alineado tipo ticket (usando monoespaciado)
                                val detalle = String.format(
                                    "%3s %3dUND %6.2f -%2.2f %6.2f",
                                    it.talla,
                                    it.cantidad,
                                    it.precio,
                                    it.descuento,
                                    it.total
                                )

                                Text(
                                    text = detalle,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    color = Color.Gray
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        val totalDescuento =
                            ventaSeleccionada!!.productos.sumOf { it.descuento }
                        Text(
                            text = "Descuento: S/${totalDescuento}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Total: S/${ventaSeleccionada!!.totalGen}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Ganancia: S/${ventaSeleccionada!!.ganancia}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarEditDialogo = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        // Dialgo para borrar la venta
        if (eliminarDialog && ventaSeleccionada != null) {
            AlertDialog(
                onDismissRequest = { eliminarDialog = false },
                title = {
                    Text(
                        "ELIMINAR VENTA",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("¿Desea eliminar esta venta?")
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    eliminarDialog = false
                                    ventaSeleccionada?.let { venta ->
                                        // 🔹 Actualizar la lista local inmediatamente para desaparecer la tarjeta
                                        ventas = ventas.filter { it.id != venta.id }
                                        ventaSeleccionada = null
                                        borrarVenta(venta) {
                                            Toast.makeText(
                                                context,
                                                "Venta eliminada y stock restaurado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                            ) {
                                Text("Eliminar")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            TextButton(onClick = {
                                eliminarDialog = false
                            }) {
                                Text("Cerrar")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
        //Alert Dialog para el mensaje de stock insuficiente
        if (insuficientes.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { ventaViewModel.limpiarInsuficientes() },
                title = { Text("Stock insuficiente") },
                text = {
                    Column {
                        Text("Los siguientes productos no tienen stock suficiente:")
                        Spacer(modifier = Modifier.height(8.dp))
                        insuficientes.forEach { producto ->
                            Text("• ${producto.nombre} (pedido: ${producto.cantidad}, disp: ${stockActual[producto.nombre] ?: 0})")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { ventaViewModel.limpiarInsuficientes() }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

fun borrarVenta(venta: Venta, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Obtenemos los documentos de los productos
    db.collection("productos")
        .whereIn("nombre", venta.productos.map { it.nombre })
        .get()
        .addOnSuccessListener { snapshot ->
            val batch = db.batch()

            // Restaurar stock
            venta.productos.forEach { p ->
                val prodDoc = snapshot.documents.firstOrNull { it.getString("nombre") == p.nombre }
                if (prodDoc != null) {
                    val ref = db.collection("productos").document(prodDoc.id)
                    batch.update(ref, "stock", FieldValue.increment(p.cantidad.toLong()))
                }
            }

            // Borrar la venta
            val ventaRef = db.collection("ventas").document(venta.id)
            batch.delete(ventaRef)

            batch.commit()
                .addOnSuccessListener {
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e("VentaScreen", "Error borrando venta: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            Log.e("VentaScreen", "Error obteniendo productos: ${e.message}")
        }
}

