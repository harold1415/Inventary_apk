package com.example.myinventarioapp.ui.screens


import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.style.TextAlign
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.mutableDoubleStateOf
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.myinventarioapp.ui.theme.AjustarBarraEstado
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.StockLowColor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailVenta(
    onVentaScreen: () -> Unit,
    ventaViewModel: VentaViewModel,
    onSearch: () -> Unit,
    ventaId: String = "New",
    navController: NavController
) {
//    // Controla los íconos de la Status Bar — negro con íconos blancos
//    AjustarBarraEstado(darkIcons = false)

    // Estados de UI del formulario — pueden quedarse en el Composable
    var incluirCliente by remember { mutableStateOf(false) }
    var nombreCliente by remember { mutableStateOf("") }
    var dniCliente by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember {
        mutableStateOf<com.example.myinventarioapp.ui.viewmodel.Producto?>(null)
    }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    // Observamos estados del ViewModel
    val insuficientes by ventaViewModel.insuficientes.collectAsState()
    val stockActual by ventaViewModel.stockActual.collectAsState()
    val ventaActual by ventaViewModel.ventaActual.collectAsState()
    val fechaVenta = remember { mutableStateOf("") }
    var totalGan by remember { mutableDoubleStateOf(0.0) }

    // TODO: ViewModel — la obtención del username del usuario autenticado debería
    // estar en un UserViewModel o en el mismo VentaViewModel como un StateFlow
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance().currentUser
    val uid = auth?.uid
    var username by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        uid?.let {
            db.collection("usuarios").document(it).get()
                .addOnSuccessListener { document ->
                    username = document.getString("nombre") ?: ""
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Carga o limpia la venta según si es nueva o existente
    LaunchedEffect(ventaId) {
        Log.d("DetailVenta", "Recibido ventaId: $ventaId")
        if (ventaId == "New") {
            ventaViewModel.limpiarVenta()
        } else {
            if (ventaViewModel.ventaActualId != ventaId) {
                ventaViewModel.resetearCarga()
            }
            ventaViewModel.cargarVenta(ventaId)
        }
    }

    // Rellena los campos cuando carga una venta existente
    LaunchedEffect(ventaActual) {
        if (ventaActual != null) {
            incluirCliente = ventaActual?.cliente != null
            nombreCliente = ventaActual?.cliente ?: ""
            dniCliente = ventaActual?.dni ?: ""
        }
    }

    // Intercepta el botón físico de retroceso
    BackHandler {
        navController.navigate("ventas") {
            popUpTo("ventas") { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = BrandWarmBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSearch,
                containerColor = BrandBlack,
                contentColor = BrandWarmWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (ventaId == "New") "📝 Nueva Venta" else "✏️ Editar Venta",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = BrandWarmWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBlack
                ),
                actions = {
                    // Botón guardar venta
                    IconButton(
                        onClick = {
                            // TODO: ViewModel — guardarVenta() y actualizarVenta() ya están en
                            // VentaViewModel, esto está bien. Solo el username debería venir
                            // del ViewModel en vez de consultarse aquí
                            if (ventaActual == null) {
                                ventaViewModel.guardarVenta(
                                    context = context,
                                    cliente = if (incluirCliente) nombreCliente else null,
                                    dni = if (incluirCliente) dniCliente else null,
                                    vendedor = username,
                                    ganancia = totalGan,
                                    sucursal = ventaViewModel.ventaLocal
                                ) { onVentaScreen() }
                            } else {
                                ventaActual?.let { venta ->
                                    ventaViewModel.actualizarVenta(
                                        context = context,
                                        ventaId = venta.id,
                                        cliente = if (incluirCliente) nombreCliente else null,
                                        dni = if (incluirCliente) dniCliente else null,
                                        vendedor = username,
                                        fecha = fechaVenta.value,
                                        ganancia = totalGan,
                                        sucursal = ventaViewModel.ventaLocal
                                    ) { onVentaScreen() }
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar venta",
                            tint = BrandWoodMedium,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("ventas") {
                            popUpTo("ventas") { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = BrandWarmWhite)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Sección cliente y fecha
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // ── Sección Cliente ──────────────────────────────────
                        Text(
                            "👨‍💼 Cliente",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandBlack
                        )
                        HorizontalDivider(
                            Modifier.alpha(0.3f),
                            color = BrandWoodLight
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = incluirCliente,
                                onCheckedChange = { incluirCliente = it },
                                colors = CheckboxDefaults.colors(checkedColor = BrandBlack)
                            )
                            Text(
                                "Agregar datos del cliente",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BrandTextSecondary,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (incluirCliente) {
                            OutlinedTextField(
                                value = nombreCliente,
                                onValueChange = { nombreCliente = it },
                                label = { Text("Nombre del cliente") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlack,
                                    unfocusedBorderColor = BrandWoodMedium
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dniCliente,
                                onValueChange = { dniCliente = it },
                                label = { Text("DNI/RUC") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlack,
                                    unfocusedBorderColor = BrandWoodMedium
                                )
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        // ── Sección Fecha ────────────────────────────────────
                        Text(
                            "📅 Fecha",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandBlack
                        )
                        HorizontalDivider(
                            Modifier.alpha(0.3f),
                            color = BrandWoodLight
                        )
                        val fechaModificada = remember { mutableStateOf(false) }
                        LaunchedEffect(ventaActual?.id) {
                            if (!fechaModificada.value) {
                                val fecha = ventaActual?.fecha?.toDate()?.let {
                                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                                } ?: SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                                fechaVenta.value = fecha
                                Log.d("fecha", ":$fecha")
                            }
                        }
                        Log.d("fecha venta", ":${ventaActual?.fecha}")
                        Spacer(Modifier.height(8.dp))
                        FechaEditable(
                            fechaSeleccionada = fechaVenta,
                            onFechaCambiada = { nuevaFecha ->
                                fechaVenta.value = nuevaFecha
                                fechaModificada.value = true
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Sección tabla de productos
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(
                            "📦 Productos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BrandBlack
                        )
                        HorizontalDivider(Modifier.alpha(0.3f), color = BrandWoodLight)
                        Spacer(Modifier.height(8.dp))
                        // Encabezados de la tabla
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text("Producto", Modifier.width(120.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandTextSecondary)
                            Text("Can.", Modifier.width(50.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center, color = BrandTextSecondary)
                            Text("Desc.", Modifier.width(60.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center, color = BrandTextSecondary)
                            Text("S/.", Modifier.width(60.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center, color = BrandTextSecondary)
                            Text("Total", Modifier.width(70.dp), fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center, color = BrandTextSecondary)
                            Spacer(Modifier.width(60.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            // TODO: ViewModel — ventaViewModel.productos ya viene del ViewModel, esto está bien.
            // El cálculo de totales (sumOf) podría moverse al ViewModel como StateFlow derivado
            val listaventa = ventaViewModel.productos
            val totalDescuento = listaventa.sumOf { it.descuento }
            totalGan = listaventa.sumOf { it.ganancia }
            val totalFinal = listaventa.sumOf { it.total }

            // Lista de productos con animación de entrada
            itemsIndexed(listaventa, key = { _, prod -> prod.idDetalle }) { index, prod ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 80L)
                    visible = true
                }

                // Colores alternados de fila con la paleta de marca
                val backgroundColor = if (index % 2 == 0)
                    BrandWarmWhite
                else
                    BrandWoodLight.copy(alpha = 0.2f)

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 3 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .background(backgroundColor),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                prod.nombre,
                                modifier = Modifier.width(120.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = BrandBlack.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Normal
                                ),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(prod.cantidad.toString(), modifier = Modifier.width(50.dp), fontSize = 14.sp, textAlign = TextAlign.Center, color = BrandTextSecondary)
                            Text(prod.descuento.toString(), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, fontSize = 14.sp, color = BrandTextSecondary)
                            Text(prod.precio.toString(), modifier = Modifier.width(60.dp), textAlign = TextAlign.Center, fontSize = 14.sp, color = BrandTextSecondary)
                            Text(prod.total.toString(), modifier = Modifier.width(70.dp), textAlign = TextAlign.Center, fontSize = 14.sp, color = BrandBlack, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.width(80.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Botón editar (deshabilitado por ahora — lógica comentada en original)
                                IconButton(onClick = { }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = BrandWoodMedium, modifier = Modifier.size(20.dp))
                                }
                                // TODO: ViewModel — eliminarProducto() ya está en VentaViewModel, está bien
                                IconButton(onClick = { ventaViewModel.eliminarProducto(prod.idDetalle) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StockLowColor, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Totales de la venta
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)
                Surface(
                    color = BrandWarmWhite,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "Total descuento: S/. ${"%.2f".format(totalDescuento)}",
                            fontSize = 14.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandTextSecondary
                        )
                        Text(
                            "Total final: S/. ${"%.2f".format(totalFinal)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandBlack
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = BrandWoodLight)
                        Text(
                            "Ganancia: S/. ${"%.2f".format(totalGan)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = BrandWoodMedium
                        )
                    }
                }
            }
        }

        // Dialog: EDITA UN PRODUCTO AÑADIDO AL DETAILVENTA- POR AHORA NO SE USA
        if (showEditDialog && selectedProduct != null && selectedIndex != null) {
            var editNombre by rememberSaveable(selectedProduct) { mutableStateOf(selectedProduct!!.nombre) }
            var editCant by rememberSaveable(selectedProduct) { mutableStateOf(selectedProduct!!.cantidad.toString()) }
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar Producto", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editNombre,
                            onValueChange = { editNombre = it },
                            label = { Text("Nombre") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editCant,
                            onValueChange = { editCant = it },
                            label = { Text("Cantidad") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val nuevaCantidad = editCant.toLongOrNull() ?: 0
                            ventaViewModel.updateProducto(index = selectedIndex!!, nombre = editNombre, cantidad = nuevaCantidad)
                            showEditDialog = false
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWarmWhite)
                    ) { Text("Guardar") }
                },
                dismissButton = {
                    Button(
                        onClick = { showEditDialog = false },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandWoodMedium, contentColor = BrandBlack)
                    ) { Text("Cancelar") }
                }
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

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FechaEditable(
    fechaSeleccionada: MutableState<String> = remember {
        mutableStateOf(
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        )
    },
    onFechaCambiada: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = fechaSeleccionada.value,
        onValueChange = {},
        label = { Text("Fecha de venta") },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Seleccionar fecha",
                modifier = Modifier.clickable { showDialog = true },
                tint = BrandWoodMedium
            )
        },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandBlack,
            unfocusedBorderColor = BrandWoodMedium
        )
    )

    if (showDialog) {
        val calendario = Calendar.getInstance()
        val year = calendario.get(Calendar.YEAR)
        val month = calendario.get(Calendar.MONTH)
        val day = calendario.get(Calendar.DAY_OF_MONTH)

        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
                    val minutoActual = calendario.get(Calendar.MINUTE)
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val nuevaFechaHora = String.format("%02d/%02d/%04d %02d:%02d", d, m + 1, y, hour, minute)
                            fechaSeleccionada.value = nuevaFechaHora
                            onFechaCambiada(nuevaFechaHora)
                        },
                        horaActual, minutoActual, true
                    ).show()
                },
                year, month, day
            ).apply {
                setOnDismissListener { showDialog = false }
            }.show()
        }
    }
}