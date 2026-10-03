package com.example.myinventarioapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import com.example.myinventarioapp.ui.viewmodel.Venta
import com.google.firebase.Timestamp
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Store
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.StockLowColor
import java.util.Calendar
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.Instant
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color



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
fun VentaScreen(onNavigateToDetailVenta: (String) -> Unit, ventaViewModel: VentaViewModel, userRole:String) {

    // Controla los íconos de la Status Bar — negro con íconos blancos
    AjustarBarraEstado(darkIcons = false)
    val locales by ventaViewModel.locales.collectAsState()
    val ventas by ventaViewModel.ventasTo.collectAsState()
    val context = LocalContext.current

    // Estados de UI — estos pueden quedarse en el Composable
    var mostrarEditDialogo by remember { mutableStateOf(false) }
    var ventaSeleccionada by remember { mutableStateOf<Venta?>(null) }
    var eliminarDialog by remember { mutableStateOf(false) }

    val insuficientes by ventaViewModel.insuficientes.collectAsState()
    val stockActual by ventaViewModel.stockActual.collectAsState()

    var filtredLocal by remember { mutableStateOf(false) }
    var selectedLocal by remember { mutableStateOf("") }

    //PARA LA FECHA
    var mostrarDatePicker by remember { mutableStateOf(false) }
//    val hoy = remember {
//        Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//    }
//    var fechaSeleccionada by remember {
//        mutableLongStateOf(hoy.timeInMillis)
//    }
    var fechaSeleccionada by remember {
        mutableStateOf(LocalDate.now())
    }


    // TODO: ViewModel — estas consultas a Firestore deberían estar en VentaListViewModel
    // usando addSnapshotListener dentro de init{} o en una función cargarVentas()
//    LaunchedEffect(Unit) {
//        db.collection("ventas").addSnapshotListener { snapshot, _ ->
//            snapshot?.let {
//                ventas = it.documents.mapNotNull { doc ->
//                    try {
//                        doc.toObject(Venta::class.java)?.copy(id = doc.id)
//                    } catch (e: Exception) {
//                        Log.e("VentaScreen", "Error parseando venta: ${e.message}")
//                        null
//                    }
//                }
//            }
//        }
//    }

    // TODO: ViewModel — el filtrado y ordenamiento también debería ir en VentaListViewModel
//    val ventasFiltro = ventas
//        .sortedByDescending { it.fecha }
//        .filter { venta ->
//            val coincideLocal = selectedLocal.isBlank() ||
//                    venta.sucursal.equals(selectedLocal, ignoreCase = true)
//            coincideLocal
//        }
    val ventasFiltro = ventaViewModel.filtrarVentas(
        ventas = ventas,
        selectedLocal = selectedLocal,
        fechaSeleccionada = fechaSeleccionada
    )

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
                        //FECHA Y LOCALES
                        Row( modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        mostrarDatePicker = true
                                    },
                                shape = RoundedCornerShape(50),
                                color = BrandWarmWhite,
                                border = BorderStroke(
                                    1.dp,
                                    BrandWoodLight
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 18.dp,
                                        vertical = 9.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Fecha",
                                        tint = BrandWoodMedium,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(7.dp))

                                    Text(
                                        text = textoFechaChip(fechaSeleccionada),
                                        color = BrandBlack,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = "▼",
                                        color = BrandTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            ExposedDropdownMenuBox(
                                expanded = filtredLocal,
                                onExpandedChange = {
                                    filtredLocal = !filtredLocal
                                }
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .menuAnchor(
                                            type = MenuAnchorType.PrimaryNotEditable,
                                            enabled = true
                                        ),
                                    shape = RoundedCornerShape(50),
                                    color = BrandWarmWhite,
                                    border = BorderStroke(
                                        1.dp,
                                        BrandWoodLight
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 14.dp,
                                            vertical = 9.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Sucursal",
                                            tint = BrandWoodMedium,
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Spacer(modifier = Modifier.width(7.dp))

                                        Text(
                                            text = selectedLocal.ifBlank {
                                                "Todas"
                                            },
                                            color = BrandBlack,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )

                                        Spacer(modifier = Modifier.width(5.dp))

                                        Text(
                                            text = "▼",
                                            color = BrandTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = filtredLocal,
                                    onDismissRequest = {
                                        filtredLocal = false
                                    }
                                ) {

                                    // Opción: todas
                                    DropdownMenuItem(
                                        text = {
                                            Text("Todas las sucursales")
                                        },
                                        onClick = {
                                            selectedLocal = ""
                                            filtredLocal = false
                                        }
                                    )

                                    // Sucursales de Firebase
                                    locales.forEach { local ->

                                        DropdownMenuItem(
                                            text = {
                                                Text(local.nombre)
                                            },
                                            onClick = {
                                                selectedLocal = local.nombre
                                                filtredLocal = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            items(ventasFiltro) { venta ->
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
                                if (userRole == "admin") {
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
            }

            // Espaciado al final de la lista
            item { Spacer(Modifier.height(8.dp)) }
        }

//        // Dialog: ver detalle completo de la venta
//        if (mostrarEditDialogo && ventaSeleccionada != null) {
//            Log.d("venta", ":${ventaSeleccionada!!.productos}")
//            AlertDialog(
//                onDismissRequest = { mostrarEditDialogo = false },
//                containerColor = BrandWarmWhite,
//                title = { Text("Detalle de la venta", color = BrandBlack) },
//                text = {
//                    Column {
//                        Text("Vendedor: ${ventaSeleccionada!!.vendedor ?: "Sin nombre"}", fontWeight = FontWeight.Bold)
//                        Text("Sucursal: ${ventaSeleccionada!!.sucursal}", fontWeight = FontWeight.Bold)
//                        Text("Cliente: ${ventaSeleccionada!!.cliente ?: "Sin nombre"}", fontWeight = FontWeight.Bold)
//                        Text("DNI: ${ventaSeleccionada!!.dni ?: "No registrado"}", fontWeight = FontWeight.Bold)
//                        Text("Fecha: ${formatFecha(ventaSeleccionada!!.fecha)}", fontWeight = FontWeight.Bold)
//
//                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)
//
//                        Text("Productos:", fontWeight = FontWeight.Bold)
//                        Text(
//                            "Talla     Cant.    Precio    Desc.   Total  ",
//                            fontSize = 15.sp,
//                            color = BrandTextSecondary,
//                            modifier = Modifier.fillMaxWidth(),
//                            textAlign = TextAlign.End
//                        )
//                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)
//
//                        ventaSeleccionada!!.productos.forEach {
//                            Column {
//                                Text("• " + run {
//                                    val palabras = it.nombre.split(" ")
//                                    if (palabras.size <= 2 || it.nombre.length <= 35) it.nombre
//                                    else {
//                                        val ultimas = palabras.takeLast(2).joinToString(" ")
//                                        val resto = palabras.dropLast(2).joinToString(" ")
//                                        val restoCortado = if (resto.length > (25 - ultimas.length - 4))
//                                            resto.take(25 - ultimas.length - 4) + "..."
//                                        else resto
//                                        "$restoCortado $ultimas"
//                                    }
//                                })
//                                val detalle = String.format(
//                                    "%3s %3dUND %6.2f -%2.2f %6.2f",
//                                    it.talla, it.cantidad, it.precio, it.descuento, it.total
//                                )
//                                Text(
//                                    text = detalle,
//                                    fontFamily = FontFamily.Monospace,
//                                    fontSize = 14.sp,
//                                    modifier = Modifier.fillMaxWidth(),
//                                    textAlign = TextAlign.End,
//                                    color = BrandTextSecondary
//                                )
//                            }
//                        }
//
//                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)
//
//                        val totalDescuento = ventaSeleccionada!!.productos.sumOf { it.descuento }
//                        Text(
//                            text = "Descuento: S/${totalDescuento}",
//                            textAlign = TextAlign.End,
//                            modifier = Modifier.fillMaxWidth(),
//                            color = BrandTextSecondary
//                        )
//                        Text(
//                            text = "Total: S/${"%.2f".format(ventaSeleccionada!!.totalGen)}",
//                            textAlign = TextAlign.End,
//                            modifier = Modifier.fillMaxWidth(),
//                            fontWeight = FontWeight.Bold,
//                            color = BrandBlack
//                        )
//                        Text(
//                            text = "Ganancia: S/${"%.2f".format(ventaSeleccionada!!.ganancia)}",
//                            textAlign = TextAlign.End,
//                            modifier = Modifier.fillMaxWidth(),
//                            color = BrandWoodMedium
//                        )
//                    }
//                },
//                confirmButton = {
//                    TextButton(onClick = { mostrarEditDialogo = false }) {
//                        Text("Cerrar")
//                    }
//                }
//            )
//        }

        // Dialog: ver detalle completo de la venta
        if (mostrarEditDialogo && ventaSeleccionada != null) {

            val venta = ventaSeleccionada!!
            val totalDescuento = venta.productos.sumOf { it.descuento }

            Dialog(
                onDismissRequest = { mostrarEditDialogo = false }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = BrandWarmWhite,
                    tonalElevation = 8.dp
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {

                        // ─────────────────────────────
                        // CABECERA
                        // ─────────────────────────────

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(BrandWoodLight.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = BrandWoodMedium,
                                    modifier = Modifier.size(27.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Detalle de venta",
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlack
                                )

                                Text(
                                    text = formatFecha(venta.fecha),
                                    fontSize = 13.sp,
                                    color = BrandTextSecondary
                                )
                            }

                            IconButton(
                                onClick = { mostrarEditDialogo = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = BrandTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))


                        // ─────────────────────────────
                        // INFORMACIÓN DE LA VENTA
                        // ─────────────────────────────

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            InfoItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Person,
                                label = "Cliente",
                                value = venta.cliente ?: "Sin nombre"
                            )

                            InfoItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Badge,
                                label = "DNI",
                                value = venta.dni ?: "No registrado"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            InfoItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.Store,
                                label = "Sucursal",
                                value = venta.sucursal
                            )

                            InfoItem(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.PersonOutline,
                                label = "Vendedor",
                                value = venta.vendedor ?: "Sin nombre"
                            )
                        }


                        Spacer(modifier = Modifier.height(22.dp))


                        // ─────────────────────────────
                        // PRODUCTOS
                        // ─────────────────────────────

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Productos",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlack
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BrandWoodLight.copy(alpha = 0.35f)
                            ) {
                                Text(
                                    text = "${venta.productos.size}",
                                    modifier = Modifier.padding(
                                        horizontal = 9.dp,
                                        vertical = 3.dp
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandWoodMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))


                        // Cabecera tabla

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    BrandWoodLight.copy(alpha = 0.20f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Producto",
                                modifier = Modifier.weight(1.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTextSecondary
                            )

                            Text(
                                text = "Cant.",
                                modifier = Modifier.weight(0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Total",
                                modifier = Modifier.weight(0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandTextSecondary,
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))


                        // Lista de productos

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            venta.productos.forEach { producto ->

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 8.dp,
                                            vertical = 10.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Column(
                                        modifier = Modifier.weight(1.7f)
                                    ) {

                                        Text(
                                            text = producto.nombre,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandBlack,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = "Talla ${producto.talla}  •  S/${"%.2f".format(producto.precio)}",
                                            fontSize = 11.sp,
                                            color = BrandTextSecondary
                                        )
                                    }

                                    Text(
                                        text = "${producto.cantidad}",
                                        modifier = Modifier.weight(0.6f),
                                        fontSize = 13.sp,
                                        color = BrandTextSecondary,
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "S/${"%.2f".format(producto.total)}",
                                        modifier = Modifier.weight(0.8f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlack,
                                        textAlign = TextAlign.End
                                    )
                                }

                                HorizontalDivider(
                                    color = BrandWoodLight.copy(alpha = 0.35f)
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(18.dp))


                        // ─────────────────────────────
                        // TOTALES
                        // ─────────────────────────────

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = BrandWoodLight.copy(alpha = 0.18f)
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                SummaryRow(
                                    label = "Descuento",
                                    value = "- S/${"%.2f".format(totalDescuento)}",
                                    valueColor = BrandTextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                SummaryRow(
                                    label = "Total",
                                    value = "S/${"%.2f".format(venta.totalGen)}",
                                    labelSize = 18.sp,
                                    valueSize = 20.sp,
                                    labelWeight = FontWeight.Bold,
                                    valueWeight = FontWeight.Bold,
                                    valueColor = BrandBlack
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                SummaryRow(
                                    label = "Ganancia",
                                    value = "S/${"%.2f".format(venta.ganancia)}",
                                    valueColor = Color(0xFF2E7D32)
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(18.dp))


                        // ─────────────────────────────
                        // BOTÓN
                        // ─────────────────────────────

                        Button(
                            onClick = { mostrarEditDialogo = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlack
                            )
                        ) {
                            Text(
                                text = "Cerrar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
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
                                        ventaSeleccionada = null
                                        ventaViewModel.borrarVenta(venta) {
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

        if (mostrarDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = fechaSeleccionada
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = {
                    mostrarDatePicker = false
                },
                confirmButton = {
                    Row {
                        TextButton(
                            onClick = {
                                fechaSeleccionada = LocalDate.now()
                                mostrarDatePicker = false
                            }
                        ) {
                            Text("Hoy")
                        }
                        TextButton(
                            onClick = {

                                datePickerState.selectedDateMillis?.let { millis ->
                                    fechaSeleccionada = Instant
                                        .ofEpochMilli(millis)
                                        .atZone(ZoneOffset.UTC)
                                        .toLocalDate()
                                }


                                mostrarDatePicker = false
                            }
                        ) {
                            Text(
                                text = "Aplicar",
                                color = BrandBlack
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarDatePicker = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {

                DatePicker(
                    state = datePickerState
                )
            }
        }

    }
}

// TODO: ViewModel — borrarVenta() debería estar en VentaListViewModel
//fun borrarVenta(venta: Venta, onComplete: () -> Unit) {
//    val db = FirebaseFirestore.getInstance()
//    val batch = db.batch()
//
//    venta.productos.forEach { p ->
//
//        val ref = db.collection("productos").document(p.productoId)
//
//        batch.update(
//            ref,
//            "stock",
//            FieldValue.increment(p.cantidad)
//        )
//    }
//
//    val ventaRef = db.collection("ventas").document(venta.id)
//    batch.delete(ventaRef)
//
//    batch.commit()
//        .addOnSuccessListener {
//            onComplete()
//        }
//        .addOnFailureListener { e ->
//            Log.e(
//                "VentaScreen",
//                "Error borrando venta: ${e.message}"
//            )
//        }
//}

//fun textoFechaChip(timestamp: Long): String {
//    val calendario = Calendar.getInstance()
//    val seleccionada = Calendar.getInstance().apply {
//        timeInMillis = timestamp
//    }
//
//    val esHoy =
//        calendario.get(Calendar.YEAR) == seleccionada.get(Calendar.YEAR) &&
//                calendario.get(Calendar.DAY_OF_YEAR) == seleccionada.get(Calendar.DAY_OF_YEAR)
//
//    return if (esHoy) {
//        "Hoy"
//    } else {
//        SimpleDateFormat(
//            "dd MMM",
//            Locale("es", "ES")
//        ).format(timestamp)
//            .replaceFirstChar { it.uppercase() }
//    }
//}
fun textoFechaChip(fecha: LocalDate): String {

    val hoy = LocalDate.now()

    return if (fecha == hoy) {
        "Hoy"
    } else {
        fecha.format(
            DateTimeFormatter.ofPattern(
                "dd MMM",
                Locale("es", "ES")
            )
        ).replaceFirstChar { it.uppercase() }
    }
}
@Composable
private fun InfoItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.65f)
    ) {

        Row(
            modifier = Modifier.padding(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandWoodMedium,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = BrandTextSecondary
                )

                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelSize: TextUnit = 13.sp,
    valueSize: TextUnit = 13.sp,
    labelWeight: FontWeight = FontWeight.Normal,
    valueWeight: FontWeight = FontWeight.SemiBold,
    valueColor: Color = BrandBlack
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = labelSize,
            fontWeight = labelWeight,
            color = BrandTextSecondary
        )

        Text(
            text = value,
            fontSize = valueSize,
            fontWeight = valueWeight,
            color = valueColor
        )
    }
}


