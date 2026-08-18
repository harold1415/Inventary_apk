package com.example.myinventarioapp.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myinventarioapp.ui.theme.AjustarBarraEstado
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.theme.StockLowColor
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    navController: NavHostController,
    codigoEscaneado: String = ""
) {
    AjustarBarraEstado(darkIcons = false)

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    // 🔹 Solo una card abierta a la vez — null = ninguna
    var seccionAbierta by remember { mutableStateOf<String?>(null) }

    // ── Estados de la transferencia ──────────────────────────────────────
    var query by remember { mutableStateOf(codigoEscaneado) }
    var isLoading by remember { mutableStateOf(false) }
    var isTransferring by remember { mutableStateOf(false) }
    var todasLasVariantes by remember { mutableStateOf(listOf<VarianteProducto>()) }
    var modeloNombre by remember { mutableStateOf("") }
    var tallaSeleccionada by remember { mutableStateOf("") }
    var colorSeleccionado by remember { mutableStateOf("") }
    var sucursalOrigenSeleccionada by remember { mutableStateOf("") }
    var sucursalDestinoSeleccionada by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var locales by remember { mutableStateOf(listOf<Local>()) }
    var dropdownDestinoExpanded by remember { mutableStateOf(false) }

    // Carga locales
    LaunchedEffect(Unit) {
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                locales = it.documents.mapNotNull { doc ->
                    doc.toObject(Local::class.java)?.copy(id = doc.id)
                }
            }
        }
    }

    // Permiso cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) navController.navigate("scanner/transferencia")
            else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    )

    // ── Lógica de búsqueda ───────────────────────────────────────────────
    fun buscarPorCodigo(codigo: String) {
        if (codigo.isBlank()) return
        isLoading = true
        todasLasVariantes = emptyList()
        tallaSeleccionada = ""
        colorSeleccionado = ""
        sucursalOrigenSeleccionada = ""
        sucursalDestinoSeleccionada = ""
        cantidad = ""

        db.collection("productos")
            .whereEqualTo("codigo", codigo)
            .get()
            .addOnSuccessListener { skuSnapshot ->
                if (!skuSnapshot.isEmpty) {
                    val doc = skuSnapshot.documents.first()
                    val modeloCod = doc.getString("modeloCod") ?: ""
                    tallaSeleccionada = doc.getString("talla") ?: ""
                    colorSeleccionado = doc.getString("color") ?: ""
                    sucursalOrigenSeleccionada = doc.getString("local") ?: ""

                    db.collection("productos")
                        .whereEqualTo("modeloCod", modeloCod)
                        .get()
                        .addOnSuccessListener { modeloSnapshot ->
                            todasLasVariantes = modeloSnapshot.documents.mapNotNull { d ->
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
                            modeloNombre = todasLasVariantes.firstOrNull()?.nombre ?: ""
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                } else {
                    db.collection("productos")
                        .whereEqualTo("modeloCod", codigo)
                        .get()
                        .addOnSuccessListener { modeloSnapshot ->
                            if (!modeloSnapshot.isEmpty) {
                                todasLasVariantes = modeloSnapshot.documents.mapNotNull { d ->
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
                                modeloNombre = todasLasVariantes.firstOrNull()?.nombre ?: ""
                            } else {
                                Toast.makeText(context, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                }
            }
            .addOnFailureListener { isLoading = false }
    }

    // Si viene código escaneado, abre la sección y busca
    LaunchedEffect(codigoEscaneado) {
        if (codigoEscaneado.isNotBlank()) {
            query = codigoEscaneado
            seccionAbierta = "transferencia"
            buscarPorCodigo(codigoEscaneado)
        }
    }

    // ── Chips y variante origen ──────────────────────────────────────────
    val tallasDisponibles = todasLasVariantes.map { it.talla }.distinct().sorted()
    val coloresDisponibles = todasLasVariantes.map { it.color }.distinct().sorted()
    val sucursalesConStock = todasLasVariantes
        .filter { it.talla == tallaSeleccionada && it.color == colorSeleccionado && it.stock > 0 }
    val varianteOrigen = todasLasVariantes.firstOrNull {
        it.talla == tallaSeleccionada &&
                it.color == colorSeleccionado &&
                it.local == sucursalOrigenSeleccionada
    }

    // ── Lógica de transferencia ──────────────────────────────────────────
    fun confirmarTransferencia() {
        val cantidadInt = cantidad.toIntOrNull() ?: 0
        if (cantidadInt <= 0) { Toast.makeText(context, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show(); return }
        if (varianteOrigen == null) { Toast.makeText(context, "Selecciona el producto origen", Toast.LENGTH_SHORT).show(); return }
        if (sucursalDestinoSeleccionada.isBlank()) { Toast.makeText(context, "Selecciona la sucursal destino", Toast.LENGTH_SHORT).show(); return }
        if (sucursalOrigenSeleccionada == sucursalDestinoSeleccionada) { Toast.makeText(context, "Origen y destino no pueden ser iguales", Toast.LENGTH_SHORT).show(); return }
        if (cantidadInt > varianteOrigen.stock) { Toast.makeText(context, "Stock insuficiente (disponible: ${varianteOrigen.stock})", Toast.LENGTH_SHORT).show(); return }

        isTransferring = true

        db.collection("productos")
            .whereEqualTo("modeloCod", varianteOrigen.modeloCod)
            .whereEqualTo("talla", varianteOrigen.talla)
            .whereEqualTo("color", varianteOrigen.color)
            .whereEqualTo("local", sucursalDestinoSeleccionada)
            .get()
            .addOnSuccessListener { destinoSnapshot ->
                val batch = db.batch()
                val origenRef = db.collection("productos").document(varianteOrigen.id)
                batch.update(origenRef, "stock", FieldValue.increment(-cantidadInt.toLong()))

                if (!destinoSnapshot.isEmpty) {
                    val destinoRef = db.collection("productos").document(destinoSnapshot.documents.first().id)
                    batch.update(destinoRef, "stock", FieldValue.increment(cantidadInt.toLong()))
                } else {
                    val nuevoDoc = db.collection("productos").document()
                    val nuevaVariante = hashMapOf(
                        "codigo" to "PRD${System.currentTimeMillis().toString().takeLast(4)}${(10..99).random()}",
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
                        isTransferring = false
                        Toast.makeText(context, "✅ $cantidadInt unidades transferidas a $sucursalDestinoSeleccionada", Toast.LENGTH_LONG).show()
                        buscarPorCodigo(varianteOrigen.modeloCod)
                        cantidad = ""
                        sucursalDestinoSeleccionada = ""
                    }
                    .addOnFailureListener {
                        isTransferring = false
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                isTransferring = false
                Toast.makeText(context, "Error al verificar destino", Toast.LENGTH_SHORT).show()
            }
    }

    // ── UI ───────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = BrandWarmBackground,
        topBar = {
            TopAppBar(
                title = { Text("Movimientos", color = BrandWarmWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Card Transferencia ────────────────────────────────────────
            AccordionCard(
                title = "Transferencia",
                description = "Mover productos entre locales",
                icon = Icons.Default.CompareArrows,
                isOpen = seccionAbierta == "transferencia",
                onClick = {
                    seccionAbierta = if (seccionAbierta == "transferencia") null else "transferencia"
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // Buscador
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("SKU o código de modelo…") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = BrandWoodMedium) },
                        trailingIcon = {
                            Row {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = {
                                        query = ""
                                        todasLasVariantes = emptyList()
                                        tallaSeleccionada = ""
                                        colorSeleccionado = ""
                                        sucursalOrigenSeleccionada = ""
                                        sucursalDestinoSeleccionada = ""
                                        cantidad = ""
                                    }) { Icon(Icons.Default.Close, null, tint = BrandWoodMedium) }
                                }
                                IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Icon(Icons.Default.QrCodeScanner, null, tint = BrandWoodMedium)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(2.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlack,
                            unfocusedBorderColor = BrandWoodMedium,
                            focusedContainerColor = BrandWarmWhite,
                            unfocusedContainerColor = BrandWarmWhite,
                            cursorColor = BrandBlack
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                    )

                    Button(
                        onClick = { buscarPorCodigo(query) },
                        enabled = query.isNotBlank() && !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWarmWhite)
                    ) {
                        Text(if (isLoading) "Buscando..." else "Buscar producto")
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandBlack, modifier = Modifier.size(32.dp))
                        }
                    }

                    if (todasLasVariantes.isNotEmpty()) {

                        // Nombre del modelo
                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, BrandWoodLight, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(modeloNombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandBlack)
                                Text("${todasLasVariantes.size} variantes", style = MaterialTheme.typography.labelSmall, color = BrandTextSecondary)
                            }
                        }

                        // Chips Talla
                        Text("Talla", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(tallasDisponibles.size) { i ->
                                val talla = tallasDisponibles[i]
                                val sel = talla == tallaSeleccionada
                                Surface(
                                    onClick = { tallaSeleccionada = talla; sucursalOrigenSeleccionada = "" },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (sel) BrandBlack else BrandWarmWhite,
                                    border = BorderStroke(1.dp, if (sel) BrandBlack else BrandWoodLight)
                                ) {
                                    Text(talla, style = MaterialTheme.typography.labelMedium, color = if (sel) BrandWarmWhite else BrandTextSecondary, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                                }
                            }
                        }

                        // Chips Color
                        Text("Color", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(coloresDisponibles.size) { i ->
                                val color = coloresDisponibles[i]
                                val sel = color == colorSeleccionado
                                Surface(
                                    onClick = { colorSeleccionado = color; sucursalOrigenSeleccionada = "" },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (sel) BrandBlack else BrandWarmWhite,
                                    border = BorderStroke(1.dp, if (sel) BrandBlack else BrandWoodLight)
                                ) {
                                    Text(color, style = MaterialTheme.typography.labelMedium, color = if (sel) BrandWarmWhite else BrandTextSecondary, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                                }
                            }
                        }

                        // Sucursales origen
                        if (tallaSeleccionada.isNotBlank() && colorSeleccionado.isNotBlank()) {
                            Text("Sucursal origen", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary)

                            if (sucursalesConStock.isEmpty()) {
                                Text("Sin stock para esta variante", style = MaterialTheme.typography.bodySmall, color = StockLowColor)
                            } else {
                                sucursalesConStock.forEach { variante ->
                                    val isSelected = sucursalOrigenSeleccionada == variante.local
                                    Card(
                                        modifier = Modifier.fillMaxWidth().border(if (isSelected) 2.dp else 1.dp, if (isSelected) BrandBlack else BrandWoodLight, RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (isSelected) BrandBlack else BrandWarmWhite),
                                        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp),
                                        onClick = { sucursalOrigenSeleccionada = variante.local; sucursalDestinoSeleccionada = ""; cantidad = "" }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(variante.local, fontWeight = FontWeight.Medium, color = if (isSelected) BrandWarmWhite else BrandBlack)
                                            Surface(
                                                color = if (variante.stock <= 3) StockLowColor.copy(alpha = if (isSelected) 0.3f else 0.12f)
                                                else BrandWoodLight.copy(alpha = if (isSelected) 0.2f else 0.5f),
                                                shape = RoundedCornerShape(20.dp)
                                            ) {
                                                Text(
                                                    "Stock: ${variante.stock}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (variante.stock <= 3) StockLowColor else if (isSelected) BrandWoodMedium else BrandTextSecondary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }

                        // Sucursal destino
                        if (sucursalOrigenSeleccionada.isNotBlank()) {
                            Text("Sucursal destino", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary)
                            ExposedDropdownMenuBox(
                                expanded = dropdownDestinoExpanded,
                                onExpandedChange = { dropdownDestinoExpanded = !dropdownDestinoExpanded }
                            ) {
                                OutlinedTextField(
                                    value = sucursalDestinoSeleccionada.ifEmpty { "Selecciona destino" },
                                    onValueChange = {},
                                    readOnly = true,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownDestinoExpanded) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandBlack,
                                        unfocusedBorderColor = BrandWoodMedium,
                                        focusedContainerColor = BrandWarmWhite,
                                        unfocusedContainerColor = BrandWarmWhite
                                    ),
                                    modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                )
                                ExposedDropdownMenu(expanded = dropdownDestinoExpanded, onDismissRequest = { dropdownDestinoExpanded = false }) {
                                    locales.filter { it.nombre != sucursalOrigenSeleccionada }.forEach { local ->
                                        DropdownMenuItem(
                                            text = { Text(local.nombre) },
                                            onClick = { sucursalDestinoSeleccionada = local.nombre; dropdownDestinoExpanded = false }
                                        )
                                    }
                                }
                            }
                        }

                        // Cantidad + resumen + botón
                        if (sucursalDestinoSeleccionada.isNotBlank()) {
                            Text("Cantidad a transferir", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary)
                            OutlinedTextField(
                                value = cantidad,
                                onValueChange = { cantidad = it },
                                label = { Text(varianteOrigen?.let { "Máximo: ${it.stock}" } ?: "Cantidad") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium),
                                isError = cantidad.isNotBlank() && ((cantidad.toIntOrNull() == null) || (cantidad.toIntOrNull() ?: 0) > (varianteOrigen?.stock ?: 0))
                            )

                            // Card resumen
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandBlack),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Resumen", style = MaterialTheme.typography.labelSmall, color = BrandWoodMedium)
                                    Text(modeloNombre, fontWeight = FontWeight.Bold, color = BrandWarmWhite)
                                    Text("$tallaSeleccionada · $colorSeleccionado", style = MaterialTheme.typography.bodySmall, color = BrandWoodMedium)
                                    HorizontalDivider(color = BrandWoodMedium.copy(alpha = 0.3f))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Origen", style = MaterialTheme.typography.labelSmall, color = BrandWoodMedium)
                                            Text(sucursalOrigenSeleccionada, color = BrandWarmWhite, fontWeight = FontWeight.Medium)
                                        }
                                        Text("→", color = BrandWoodMedium, fontSize = 18.sp)
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Destino", style = MaterialTheme.typography.labelSmall, color = BrandWoodMedium)
                                            Text(sucursalDestinoSeleccionada, color = BrandWarmWhite, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    if (cantidad.isNotBlank() && cantidad.toIntOrNull() != null) {
                                        HorizontalDivider(color = BrandWoodMedium.copy(alpha = 0.3f))
                                        Text("$cantidad unidades", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandWarmWhite)
                                    }
                                }
                            }

                            Button(
                                onClick = { confirmarTransferencia() },
                                enabled = cantidad.isNotBlank() &&
                                        (cantidad.toIntOrNull() ?: 0) > 0 &&
                                        (cantidad.toIntOrNull() ?: 0) <= (varianteOrigen?.stock ?: 0) &&
                                        !isTransferring,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandWoodMedium,
                                    contentColor = BrandBlack,
                                    disabledContainerColor = BrandWoodLight,
                                    disabledContentColor = BrandTextSecondary
                                )
                            ) {
                                Text(
                                    if (isTransferring) "Transfiriendo..." else "✅ Confirmar transferencia",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Card Devolución ───────────────────────────────────────────
            AccordionCard(
                title = "Devolución",
                description = "Registrar productos devueltos",
                icon = Icons.Default.KeyboardReturn,
                isOpen = seccionAbierta == "devolucion",
                onClick = { seccionAbierta = if (seccionAbierta == "devolucion") null else "devolucion" }
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Próximamente", style = MaterialTheme.typography.bodyMedium, color = BrandTextSecondary)
                }
            }

            // ── Card Cambio ───────────────────────────────────────────────
            AccordionCard(
                title = "Cambio",
                description = "Cambiar un producto por otro",
                icon = Icons.Default.SwapHoriz,
                isOpen = seccionAbierta == "cambio",
                onClick = { seccionAbierta = if (seccionAbierta == "cambio") null else "cambio" }
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text("Próximamente", style = MaterialTheme.typography.bodyMedium, color = BrandTextSecondary)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Componente de card acordeón ───────────────────────────────────────────────
@Composable
fun AccordionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isOpen: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
        elevation = CardDefaults.cardElevation(if (isOpen) 6.dp else 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera clickeable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono con fondo negro si está abierta
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isOpen) BrandBlack else BrandWoodLight.copy(alpha = 0.3f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isOpen) BrandWarmWhite else BrandBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, color = BrandBlack)
                    Spacer(Modifier.height(2.dp))
                    Text(description, style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                }

                // Flecha indicadora
                Text(
                    if (isOpen) "▲" else "▼",
                    color = BrandTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Contenido animado
            AnimatedVisibility(
                visible = isOpen,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = BrandWoodLight, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}