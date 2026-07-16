package com.example.myinventarioapp.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlin.text.ifEmpty
import kotlin.text.isBlank
import androidx.compose.foundation.border
import com.example.myinventarioapp.ui.theme.AjustarBarraEstado
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.StockLowColor


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

    // Controla los íconos de la Status Bar — negro con íconos blancos
    AjustarBarraEstado(darkIcons = false)

    val context = LocalContext.current

    // TODO: ViewModel — db debería instanciarse en VentaListViewModel, no en el Composable
    val db = FirebaseFirestore.getInstance()

    // TODO: ViewModel — ventas y locales deberían ser StateFlow en VentaListViewModel
    var ventas by remember { mutableStateOf(listOf<Venta>()) }
    var locales by remember { mutableStateOf(listOf<Local>()) }

    // Estados de UI — estos pueden quedarse en el Composable
    var mostrarEditDialogo by remember { mutableStateOf(false) }
    var ventaSeleccionada by remember { mutableStateOf<Venta?>(null) }
    var eliminarDialog by remember { mutableStateOf(false) }

    val insuficientes by ventaViewModel.insuficientes.collectAsState()
    val stockActual by ventaViewModel.stockActual.collectAsState()

    var filtredLocal by remember { mutableStateOf(false) }
    var selectedLocal by remember { mutableStateOf("") }

    // TODO: ViewModel — estas consultas a Firestore deberían estar en VentaListViewModel
    // usando addSnapshotListener dentro de init{} o en una función cargarVentas()
    LaunchedEffect(Unit) {
        db.collection("ventas").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                ventas = it.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Venta::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
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

    // TODO: ViewModel — el filtrado y ordenamiento también debería ir en VentaListViewModel
    val productosFiltrados = ventas
        .sortedByDescending { it.fecha }
        .filter { venta ->
            val coincideLocal = selectedLocal.isBlank() ||
                    venta.sucursal.equals(selectedLocal, ignoreCase = true)
            coincideLocal
        }

    Scaffold(
        containerColor = BrandWarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🧾 Registro de Ventas",
                        color = BrandWarmWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBlack
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    ventaViewModel.clearProductos()
                    ventaViewModel.resetProduct()
                    onNavigateToDetailVenta("New")
                },
                containerColor = BrandBlack,
                contentColor = BrandWarmWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Venta")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selector de sucursal sticky
            stickyHeader {
                Surface(
                    color = BrandWarmBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                        Spacer(Modifier.height(12.dp))
                        ExposedDropdownMenuBox(
                            expanded = filtredLocal,
                            onExpandedChange = { filtredLocal = !filtredLocal }
                        ) {
                            OutlinedTextField(
                                value = selectedLocal.ifEmpty { "Todos los locales" },
                                onValueChange = {},
                                readOnly = true,
                                shape = RoundedCornerShape(16.dp),
                                label = { Text("Seleccionar sucursal") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = filtredLocal)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlack,
                                    unfocusedBorderColor = BrandWoodMedium,
                                    focusedContainerColor = BrandWarmWhite,
                                    unfocusedContainerColor = BrandWarmWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            DropdownMenu(
                                expanded = filtredLocal,
                                onDismissRequest = { filtredLocal = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos los locales") },
                                    onClick = { selectedLocal = ""; filtredLocal = false }
                                )
                                locales.forEach { local ->
                                    DropdownMenuItem(
                                        text = { Text("Sucursal ${local.nombre}") },
                                        onClick = { selectedLocal = local.nombre; filtredLocal = false }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            items(productosFiltrados) { venta ->
                // Card de venta rediseñada con la paleta de marca
                Card(
                    modifier = Modifier
                        .fillMaxWidth().padding(horizontal = 18.dp)
                        .border(1.dp, BrandWoodLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Fila superior: ID de venta + sucursal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                "Venta #${venta.id.take(6)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandBlack,
                                modifier = Modifier.weight(1f)
                            )
                            // Chip de sucursal
                            Surface(
                                color = BrandWoodLight.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = venta.sucursal.ifEmpty { "Sin sucursal" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Datos de la venta
                        Text(
                            "Cliente: ${venta.cliente ?: "Sin nombre"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandTextSecondary
                        )
                        Text(
                            "Vendedor: ${venta.vendedor ?: "Sin nombre"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandTextSecondary
                        )
                        Text(
                            "Fecha: ${formatFecha(venta.fecha)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandWoodMedium
                        )

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = BrandWoodLight.copy(alpha = 0.6f))
                        Spacer(Modifier.height(4.dp))

                        // Fila de acciones + total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Total de la venta
                            Text(
                                "S/ ${"%.2f".format(venta.totalGen)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandBlack
                            )

                            // Botones de acción
                            Row {
                                // Ver detalle
                                IconButton(
                                    onClick = {
                                        ventaSeleccionada = venta
                                        mostrarEditDialogo = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Visibility,
                                        contentDescription = "Ver",
                                        tint = BrandWoodMedium,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                // Editar
                                IconButton(
                                    onClick = {
                                        Log.d("VentaScreen", "ID de venta al editar: ${venta.id}")
                                        ventaViewModel.productosModificados = false
                                        ventaViewModel.ventaYaCargada = false
                                        onNavigateToDetailVenta(venta.id)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = BrandBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                // Eliminar
                                IconButton(
                                    onClick = {
                                        eliminarDialog = true
                                        ventaSeleccionada = venta
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = StockLowColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Espaciado al final de la lista
            item { Spacer(Modifier.height(8.dp)) }
        }

        // Dialog: ver detalle completo de la venta
        if (mostrarEditDialogo && ventaSeleccionada != null) {
            Log.d("venta", ":${ventaSeleccionada!!.productos}")
            AlertDialog(
                onDismissRequest = { mostrarEditDialogo = false },
                containerColor = BrandWarmWhite,
                title = { Text("Detalle de la venta", color = BrandBlack) },
                text = {
                    Column {
                        Text("Vendedor: ${ventaSeleccionada!!.vendedor ?: "Sin nombre"}", fontWeight = FontWeight.Bold)
                        Text("Sucursal: ${ventaSeleccionada!!.sucursal}", fontWeight = FontWeight.Bold)
                        Text("Cliente: ${ventaSeleccionada!!.cliente ?: "Sin nombre"}", fontWeight = FontWeight.Bold)
                        Text("DNI: ${ventaSeleccionada!!.dni ?: "No registrado"}", fontWeight = FontWeight.Bold)
                        Text("Fecha: ${formatFecha(ventaSeleccionada!!.fecha)}", fontWeight = FontWeight.Bold)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)

                        Text("Productos:", fontWeight = FontWeight.Bold)
                        Text(
                            "Talla     Cant.    Precio    Desc.   Total  ",
                            fontSize = 15.sp,
                            color = BrandTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)

                        ventaSeleccionada!!.productos.forEach {
                            Column {
                                Text("• " + run {
                                    val palabras = it.nombre.split(" ")
                                    if (palabras.size <= 2 || it.nombre.length <= 35) it.nombre
                                    else {
                                        val ultimas = palabras.takeLast(2).joinToString(" ")
                                        val resto = palabras.dropLast(2).joinToString(" ")
                                        val restoCortado = if (resto.length > (25 - ultimas.length - 4))
                                            resto.take(25 - ultimas.length - 4) + "..."
                                        else resto
                                        "$restoCortado $ultimas"
                                    }
                                })
                                val detalle = String.format(
                                    "%3s %3dUND %6.2f -%2.2f %6.2f",
                                    it.talla, it.cantidad, it.precio, it.descuento, it.total
                                )
                                Text(
                                    text = detalle,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End,
                                    color = BrandTextSecondary
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)

                        val totalDescuento = ventaSeleccionada!!.productos.sumOf { it.descuento }
                        Text(
                            text = "Descuento: S/${totalDescuento}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandTextSecondary
                        )
                        Text(
                            text = "Total: S/${"%.2f".format(ventaSeleccionada!!.totalGen)}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack
                        )
                        Text(
                            text = "Ganancia: S/${"%.2f".format(ventaSeleccionada!!.ganancia)}",
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandWoodMedium
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

        // Dialog: confirmar eliminación de venta
        if (eliminarDialog && ventaSeleccionada != null) {
            AlertDialog(
                onDismissRequest = { eliminarDialog = false },
                containerColor = BrandWarmWhite,
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
                            // TODO: ViewModel — borrarVenta() debería estar en VentaListViewModel
                            Button(
                                onClick = {
                                    eliminarDialog = false
                                    ventaSeleccionada?.let { venta ->
                                        ventas = ventas.filter { it.id != venta.id }
                                        ventaSeleccionada = null
                                        borrarVenta(venta) {
                                            Toast.makeText(context, "Venta eliminada y stock restaurado", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StockLowColor)
                            ) { Text("Eliminar") }
                            Spacer(modifier = Modifier.width(16.dp))
                            TextButton(onClick = { eliminarDialog = false }) { Text("Cerrar") }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
        }

        // Dialog: aviso de stock insuficiente
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

// TODO: ViewModel — borrarVenta() debería estar en VentaListViewModel
// para separar la lógica de negocio de la UI
fun borrarVenta(venta: Venta, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("productos")
        .whereIn("nombre", venta.productos.map { it.nombre })
        .get()
        .addOnSuccessListener { snapshot ->
            val batch = db.batch()
            venta.productos.forEach { p ->
                val prodDoc = snapshot.documents.firstOrNull { it.getString("nombre") == p.nombre }
                if (prodDoc != null) {
                    val ref = db.collection("productos").document(prodDoc.id)
                    batch.update(ref, "stock", FieldValue.increment(p.cantidad.toLong()))
                }
            }
            val ventaRef = db.collection("ventas").document(venta.id)
            batch.delete(ventaRef)
            batch.commit()
                .addOnSuccessListener { onComplete() }
                .addOnFailureListener { e -> Log.e("VentaScreen", "Error borrando venta: ${e.message}") }
        }
        .addOnFailureListener { e -> Log.e("VentaScreen", "Error obteniendo productos: ${e.message}") }
}