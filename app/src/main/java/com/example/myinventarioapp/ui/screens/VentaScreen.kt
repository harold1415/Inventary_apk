package com.example.myinventarioapp.ui.screens

import android.annotation.SuppressLint
import android.graphics.drawable.Icon
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
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.automirrored.outlined.ArrowForward

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
fun VentaScreen(onNavigateToDetailVenta: (String) -> Unit, onNavigateToQuickSale: (String) ->Unit,ventaViewModel: VentaViewModel, userRole:String) {

    // Controla los íconos de la Status Bar — negro con íconos blancos
    AjustarBarraEstado(darkIcons = false)
    val locales by ventaViewModel.locales.collectAsState()
    val ventas by ventaViewModel.ventasTo.collectAsState()
    val context = LocalContext.current

    // Estados de UI — estos pueden quedarse en el Composable
    var mostrarEditDialogo by remember { mutableStateOf(false) }
    var typeSale by remember {mutableStateOf(false)}
    var ventaSeleccionada by remember { mutableStateOf<Venta?>(null) }
    var eliminarDialog by remember { mutableStateOf(false) }

    val insuficientes by ventaViewModel.insuficientes.collectAsState()
    val stockActual by ventaViewModel.stockActual.collectAsState()

    var filtredLocal by remember { mutableStateOf(false) }
    var selectedLocal by remember { mutableStateOf("") }

    //PARA LA FECHA
    var mostrarDatePicker by remember { mutableStateOf(false) }

    var fechaSeleccionada by remember {
        mutableStateOf(LocalDate.now())
    }

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
                    typeSale = true
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

            if(ventasFiltro.isEmpty()){
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 24.dp,
                                vertical = 48.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(112.dp).background(
                                color = BrandWoodMedium.copy(alpha = 0.12f),
                                shape = CircleShape
                            ),
                            contentAlignment = Alignment.Center
                        ){
                            Box(
                                modifier = Modifier.size(78.dp).background(
                                    color = BrandWoodMedium.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(22.dp)
                                ),
                                contentAlignment = Alignment.Center
                            ){
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = BrandWoodMedium,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "¡Aún no hay ventas!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = BrandBlack,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Tus ventas aparecerán aquí cuando registres " +
                                    "tu primera operación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }else{
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
            }
            // Espaciado al final de la lista
            item { Spacer(Modifier.height(8.dp)) }
        }

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

        if (typeSale) {
            AlertDialog(
                onDismissRequest = {
                    typeSale = false
                },
                containerColor = BrandWarmWhite,
                shape = RoundedCornerShape(28.dp),
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nueva venta",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Selecciona una modalidad",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Venta normal
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    typeSale = false
                                    ventaViewModel.clearProductos()
                                    ventaViewModel.limpiarVenta()
                                    onNavigateToDetailVenta("New")

                                    // TODO: abrir el flujo de venta normal
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                Color.LightGray.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = StockLowColor
                                )

                                Spacer(Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Venta normal",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Agrega productos paso a paso",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }

                        // Venta rápida
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    typeSale = false
                                    onNavigateToQuickSale("New")
                                    // TODO: navegar a VentaRapidaScreen
                                },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BrandBlack
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QrCodeScanner,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.White
                                )

                                Spacer(Modifier.width(14.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "Venta rápida",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Escanea y agrega al instante",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }

                                Icon(
                                    imageVector =
                                        Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
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


