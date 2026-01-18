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
import androidx.compose.ui.text.style.TextAlign
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailVenta(
    onVentaScreen: () -> Unit,
    ventaViewModel: VentaViewModel,
    onSearch: () -> Unit,
    ventaId: String = "New",
    navController: NavController // 👈 recibido desde el NavHost
) {

    var incluirCliente by remember { mutableStateOf(false) }
    var nombreCliente by remember { mutableStateOf("") }
    var dniCliente by remember { mutableStateOf("") }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember {
        mutableStateOf<com.example.myinventarioapp.ui.viewmodel.Producto?>(
            null
        )
    }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    val insuficientes by ventaViewModel.insuficientes.collectAsState()
    val stockActual by ventaViewModel.stockActual.collectAsState()
    val ventaActual by ventaViewModel.ventaActual.collectAsState()
    val fechaVenta = remember { mutableStateOf("") }
    var totalGan by remember { mutableDoubleStateOf(0.0) }


    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance().currentUser
    val uid = auth?.uid

    var username by remember { mutableStateOf("") }


    //CARGAR EL NOMBRE DEL USUSARIO COMO EL VENDEDOR
    LaunchedEffect(uid) {
        uid?.let {
            db.collection("usuarios").document(it).get()
                .addOnSuccessListener { document ->
                    username = document.getString("nombre") ?: ""
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Error al obtener datos del usuario",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

// Si viene un id => estamos editando
    LaunchedEffect(ventaId) {
        Log.d("DetailVenta", "Recibido ventaId: $ventaId")
        if (ventaId == "New") {
            ventaViewModel.limpiarVenta() // Nueva venta
        } else {
            // Solo resetear si se trata de una nueva ventaId distinta
            if (ventaViewModel.ventaActualId != ventaId) {
                ventaViewModel.resetearCarga() // ✅ resetea el flag
            }
            ventaViewModel.cargarVenta(ventaId) // carga una venta existente
        }
    }
    // 👉 Cuando cambia ventaActual, rellena los campos
    LaunchedEffect(ventaActual) {
        if (ventaActual != null) {
            incluirCliente = ventaActual?.cliente != null
            nombreCliente = ventaActual?.cliente ?: ""
            dniCliente = ventaActual?.dni ?: ""
        }
    }
    // 🔹 Intercepta el botón físico de retroceso
    BackHandler {
        navController.navigate("ventas") {
            popUpTo("ventas") { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onSearch) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text( text=if(ventaId =="New")"📝 Nueva Venta" else "Editar Venta",
                    style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface))
                        },
                actions = {
                    IconButton(
                        onClick = {
                            if (ventaActual == null) {
                                // Nueva venta
                                ventaViewModel.guardarVenta(
                                    context = context,
                                    cliente = if (incluirCliente) nombreCliente else null,
                                    dni = if (incluirCliente) dniCliente else null,
                                    vendedor = username,
                                    ganancia = totalGan,
                                    sucursal = ventaViewModel.ventaLocal
                                ) { onVentaScreen() }
                            } else {
                                // Editar venta existente
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
                        modifier = Modifier
                            .padding(end = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar venta",
                            tint = MaterialTheme.colorScheme.primary, // color del ícono adaptativo
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Respeta padding del Scaffold
                .padding(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors= CardDefaults.cardColors(
                        containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Sección de Cliente
                        Text(
                            "\uD83D\uDC68\u200D\uD83D\uDCBC Cliente",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        HorizontalDivider(
                            Modifier.alpha(0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        // Checkbox Cliente
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = incluirCliente,
                                onCheckedChange = { incluirCliente = it }
                            )
                            Text("Agregar datos del cliente",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.8f),
                                    fontWeight = FontWeight.Normal)
                                )
                        }
                        Spacer(Modifier.height(16.dp))
                        if (incluirCliente) {
                            OutlinedTextField(
                                value = nombreCliente,
                                onValueChange = { nombreCliente = it },
                                label = { Text("Nombre del cliente") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = dniCliente,
                                onValueChange = { dniCliente = it },
                                label = { Text("DNI/RUC") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        // Sección de la fecha
                        Text(
                            "\uD83D\uDCC5 Fecha",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        HorizontalDivider(
                            Modifier.alpha(0.3f)
                        )
                        val fechaModificada = remember { mutableStateOf(false) }
                        // 2️⃣ Efecto que se ejecuta cada vez que cambia la venta actual o su ID
                        LaunchedEffect(ventaActual?.id) {
                            if (!fechaModificada.value) { // ✅ Solo si el usuario no la cambió
                                val fecha = ventaActual?.fecha?.toDate()?.let {
                                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                                } ?: SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                                fechaVenta.value = fecha
                                Log.d("fecha", ":$fecha")
                            }

                        }
                        Log.d("fecha venta",":${ventaActual?.fecha}")

                        Spacer(Modifier.height(8.dp))
                        FechaEditable(
                            fechaSeleccionada = fechaVenta,
                            onFechaCambiada = { nuevaFecha ->
                                fechaVenta.value = nuevaFecha
                                fechaModificada.value = true // ✅ Marcamos que el usuario la cambió

                            }
                        )
                    }

                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors= CardDefaults.cardColors(
                        containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                    // Sección de productos
                    Text(
                        "📦 Productos", style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    HorizontalDivider(
                        Modifier.alpha(0.3f)
                    )
                    Spacer(Modifier.height(8.dp))
                    // Encabezados de la tabla
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            "Producto",
                            Modifier.width(120.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Can.", Modifier.width(50.dp), fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Desc.", Modifier.width(60.dp), fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "S/.", Modifier.width(60.dp), fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Total", Modifier.width(70.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(60.dp)) // espacio para botones
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(Modifier.height(8.dp))
                }
                }
            }

            val listaventa = ventaViewModel.productos
            val totalDescuento = listaventa.sumOf { it.descuento }
            totalGan = listaventa.sumOf {it.ganancia}
            val totalFinal = listaventa.sumOf { it.total }
//            val totalDescuento = listaventa.sumOf { it.descuento.toInt() }
//            val totalFinal = listaventa.sumOf { it.total.toInt() }

            ////////////////// NUeva lista de productos
            itemsIndexed(listaventa, key = { _, prod -> prod.codigo }) { index, prod ->

                var visible by remember { mutableStateOf(false) }

                // ✅ Retrasa la aparición de cada producto según su posición
                LaunchedEffect(Unit) {
                    delay(index * 80L) // cambia a 100L o 120L si quieres que se note más
                    visible = true
                }
                val backgroundColor = if (index % 2 == 0)
                    MaterialTheme.colorScheme.surface
                else
                    MaterialTheme.colorScheme.surfaceVariant
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(500)) +
                            slideInVertically(initialOffsetY = { it / 3 }), // efecto suave de entrada
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 })
                ) {
//                        Crossfade(targetState = producto, label = "ProductoCrossfade") { prod ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem() // suaviza el movimiento de la fila
                                .background(backgroundColor),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                prod.nombre,
                                modifier = Modifier.width(120.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.8f),
                                    fontWeight = FontWeight.Normal),
                                fontSize = 14.sp, // letra más pequeña
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                prod.cantidad.toString(),
                                modifier = Modifier.width(50.dp),
                                fontSize = 14.sp, // letra más pequeña
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                prod.descuento.toString(),
                                modifier = Modifier.width(60.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp, // letra más pequeña
                            )
                            Text(
                                prod.precio.toString(),
                                modifier = Modifier.width(60.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp, // letra más pequeña
                            )
                            Text(
                                prod.total.toString(),
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp, // letra más pequeña
                            )
                            Row(
                                modifier = Modifier.width(80.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp) // separación entre botones
                            ) {
                                //BOTON EDITAR
                                IconButton(onClick = {
//                                    showEditDialog = true
//                                    selectedIndex = index
//                                    selectedProduct = prod
                                }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { ventaViewModel.eliminarProducto(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
//                            }
                    }
                }
            }
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End // 🔥 Esto alinea todo el contenido a la derecha
                ) {
                    Text(
                        "Total descuento: S/. ${"%.2f".format(totalDescuento)}",
//                        modifier = Modifier.width(60.dp),
                        fontSize = 14.sp, // letra más pequeña
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()

                    )
                    Text(
                        "Total final: S/. ${"%.2f".format(totalFinal)}",
//                        modifier = Modifier.width(70.dp),
                        fontSize = 14.sp, // letra más pequeña
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()

                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Ganancia: S/. ${"%.2f".format(totalGan)}",
//                        modifier = Modifier.width(70.dp),
                        fontSize = 14.sp, // letra más pequeña
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()

                    )
                }
            }
        }
        //INTERFAZ EDITAR
        if (showEditDialog && selectedProduct != null && selectedIndex != null) {
            // Usa rememberSaveable para mantener los valores del formulario
            var editNombre by rememberSaveable(selectedProduct) { mutableStateOf(selectedProduct!!.nombre) }
            var editCant by rememberSaveable(selectedProduct) { mutableStateOf(selectedProduct!!.cantidad.toString()) }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = {
                    Text(
                        "Editar Producto",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editNombre,
                            onValueChange = { editNombre = it },
                            label = { Text("Nombre") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editCant,
                            onValueChange = { editCant = it },
                            label = { Text("Cantidad") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val nuevaCantidad = editCant.toLongOrNull() ?: 0
                        ventaViewModel.updateProducto(
                            index = selectedIndex!!,
                            nombre = editNombre,
                            cantidad = nuevaCantidad
                        )
                        showEditDialog = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showEditDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        //para el mensaje de insuficientes
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
//@SuppressLint("DefaultLocale")
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FechaEditable(
//    fechaSeleccionada: MutableState<String> = remember {
//        mutableStateOf(
//            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
//        )
//    },
//    onFechaCambiada: (String) -> Unit = {}
//) {
//    val context = LocalContext.current
//    var showDialog by remember { mutableStateOf(false) }
//
//    // Campo visual
//    OutlinedTextField(
//        value = fechaSeleccionada.value,
//        onValueChange = {},
//        label = { Text("Fecha de venta") },
//        trailingIcon = {
//            Icon(
//                imageVector = Icons.Default.CalendarToday,
//                contentDescription = "Seleccionar fecha",
//                modifier = Modifier.clickable { showDialog = true }
//            )
//        },
//        readOnly = true,
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { showDialog = true }
//    )
//
//    // Mostrar el DatePickerDialog nativo
//    if (showDialog) {
//        val calendario = Calendar.getInstance()
//        val year = calendario.get(Calendar.YEAR)
//        val month = calendario.get(Calendar.MONTH)
//        val day = calendario.get(Calendar.DAY_OF_MONTH)
//
//        // Usamos LaunchedEffect para abrir el diálogo una sola vez
//        LaunchedEffect(Unit) {
//            DatePickerDialog(
//                context,
//                { _, y, m, d ->
//                    val nuevaFecha = String.format("%02d/%02d/%04d", d, m + 1, y)
//                    fechaSeleccionada.value = nuevaFecha
//                    onFechaCambiada(nuevaFecha)
//                },
//                year, month, day
//            ).apply {
//                setOnDismissListener { showDialog = false }
//            }.show()
//        }
//    }
//}

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
                modifier = Modifier.clickable { showDialog = true }
            )
        },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
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
                    // Después de elegir la fecha, abrir el selector de hora
                    val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
                    val minutoActual = calendario.get(Calendar.MINUTE)

                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val nuevaFechaHora = String.format(
                                "%02d/%02d/%04d %02d:%02d",
                                d, m + 1, y, hour, minute
                            )
                            fechaSeleccionada.value = nuevaFechaHora
                            onFechaCambiada(nuevaFechaHora)
                        },
                        horaActual,
                        minutoActual,
                        true
                    ).show()
                },
                year, month, day
            ).apply {
                setOnDismissListener { showDialog = false }
            }.show()
        }
    }
}

