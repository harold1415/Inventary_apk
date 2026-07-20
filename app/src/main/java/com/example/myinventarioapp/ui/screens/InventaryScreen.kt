package com.example.myinventarioapp.ui.screens


import androidx.compose.animation.*
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.filled.Edit
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.navigation.NavHostController
import android.Manifest
import android.graphics.BitmapFactory
import java.util.UUID
import java.io.ByteArrayOutputStream
import coil.compose.AsyncImage
import android.net.Uri
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.zIndex
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.StockLowColor


data class Producto(
    val id: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val tipo: String = "",
    val talla: String = "",
    val stock: Int = 0,
    val material: String = "",
    val marca: String = "",
    val color: String = "",
    val diseno: String = "",
    val precioxMayor: Double = 0.0,
    val modeloCod: String = "",
    val corte: String = "",
    val local: String = "",
    val manga: String = "",
    val costo: Double = 0.0,
    val precio: Double = 0.0,
    val fecha: Timestamp? = null,
    val imagenUrl: String = ""
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(navController: NavHostController, codigoEscaneado: String = "") {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var productos by remember { mutableStateOf(listOf<Producto>()) }
    var locales by remember { mutableStateOf(listOf<Local>()) }
    var datoimg by remember { mutableStateOf("") }

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showVerDialog by remember { mutableStateOf(false) }
    var showImageDialog by rememberSaveable { mutableStateOf(false) }
    var showIMG by rememberSaveable { mutableStateOf(false) }
    var showContinueDialog by remember { mutableStateOf(false) }
    // Estado de carga inicial — empieza en true y
// se pone en false cuando Firestore responde por primera vez
    var isLoadingProductos by remember { mutableStateOf(true) }

    var selectedProductId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedProduct = productos.find { it.id == selectedProductId }

    var miBitmapSeleccionado by remember { mutableStateOf<Bitmap?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var nombre by rememberSaveable(showDialog) { mutableStateOf("") }
    var talla by rememberSaveable(showDialog) { mutableStateOf("") }
    var stock by rememberSaveable(showDialog) { mutableStateOf("") }
    var style by rememberSaveable(showDialog) { mutableStateOf("") }
    var local by rememberSaveable(showDialog) { mutableStateOf("") }
    var costo by rememberSaveable(showDialog) { mutableStateOf("") }
    var precioMay by rememberSaveable(showDialog) { mutableStateOf("") }
    var precio by rememberSaveable(showDialog) { mutableStateOf("") }
    var type by rememberSaveable(showDialog) { mutableStateOf("") }
    var brand by rememberSaveable(showDialog) { mutableStateOf("") }
    var design by rememberSaveable(showDialog) { mutableStateOf("") }
    var material by rememberSaveable(showDialog) { mutableStateOf("") }
    var color by rememberSaveable(showDialog) { mutableStateOf("") }
    var sleeve by rememberSaveable(showDialog) { mutableStateOf("") }
    var modeloCod by rememberSaveable(showDialog) { mutableStateOf("") }
    var showmodeloCod by rememberSaveable(showDialog) { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) navController.navigate("scanner")
            else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    )

    LaunchedEffect(Unit) {
        db.collection("productos").addSnapshotListener { snapshot, _ ->
            snapshot?.let { productos = it.documents.mapNotNull { doc -> doc.toObject(Producto::class.java)?.copy(id = doc.id) } }
            isLoadingProductos = false // 👈 ya llegaron los datos
        }
    }
    LaunchedEffect(Unit) {
        db.collection("locales").addSnapshotListener { snapshot, _ ->
            snapshot?.let { locales = it.documents.mapNotNull { doc -> doc.toObject(Local::class.java)?.copy(id = doc.id) } }
        }
    }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
            } else {
                @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }
            miBitmapSeleccionado = bitmap
        }
    }

    if (isUploading) LoadingDialog(message = loadingMessage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Inventario", color = BrandWarmWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack),
            )
        },
        containerColor = BrandWarmBackground,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = BrandBlack, contentColor = BrandWarmWhite) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        var showFilterPanel by remember { mutableStateOf(false) }
        var filtredLocal by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        var query by remember { mutableStateOf(codigoEscaneado) }
        var selectedLocal by remember { mutableStateOf("") }

        val productosFiltrados = productos.filter { producto ->
            val coincideTexto = query.isBlank() || producto.nombre.contains(query, ignoreCase = true) || producto.codigo.contains(query, ignoreCase = true) || producto.modeloCod.contains(query, ignoreCase = true)
            val coincideLocal = selectedLocal.isBlank() || producto.local.equals(selectedLocal, ignoreCase = true)
            val coincideTalla = talla.isBlank() || producto.talla.equals(talla, ignoreCase = true)
            val coincideColor = color.isBlank() || producto.color.contains(color, ignoreCase = true)
            coincideTexto && coincideLocal && coincideTalla && coincideColor
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                stickyHeader {
                    Surface(color = BrandWarmBackground, modifier = Modifier.fillMaxWidth().zIndex(1f), shadowElevation = 4.dp) {
                        Column(
                            modifier = Modifier.padding(horizontal = 18.dp)
                        ){
                            Spacer(Modifier.height(12.dp))
                            // 🔍 Buscador
                            OutlinedTextField(
                                value = query, onValueChange = { query = it },
                                placeholder = { Text("Buscar…") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = BrandWoodMedium) },
                                modifier = Modifier.fillMaxWidth().height(56.dp).shadow(2.dp, RoundedCornerShape(28.dp)),
                                shape = RoundedCornerShape(28.dp), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium, focusedContainerColor = BrandWarmWhite, unfocusedContainerColor = BrandWarmWhite, cursorColor = BrandBlack),
                                trailingIcon = {
                                    Row {
                                        if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, contentDescription = "Borrar", tint = BrandWoodMedium) }
                                        IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) { Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR", tint = BrandWoodMedium) }
                                    }
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                            // 🏪 Sucursal + filtro
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                ExposedDropdownMenuBox(expanded = filtredLocal, onExpandedChange = { filtredLocal = !filtredLocal }, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = selectedLocal.ifEmpty { "Todos los locales" }, onValueChange = {}, readOnly = true,
                                        shape = RoundedCornerShape(16.dp), label = { Text("Sucursal") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filtredLocal) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium, focusedContainerColor = BrandWarmWhite, unfocusedContainerColor = BrandWarmWhite),
                                        modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                                    )
                                    DropdownMenu(expanded = filtredLocal, onDismissRequest = { filtredLocal = false }) {
                                        DropdownMenuItem(text = { Text("Todos los locales") }, onClick = { selectedLocal = ""; filtredLocal = false })
                                        locales.forEach { local -> DropdownMenuItem(text = { Text("Sucursal ${local.nombre}") }, onClick = { selectedLocal = local.nombre; filtredLocal = false }) }
                                    }
                                }
                                IconButton(onClick = { showFilterPanel = true }) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = BrandBlack)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
                // Indicador de carga mientras Firestore responde
                item {
                    StateChargePorducts(
                        isLoading = isLoadingProductos,
                        listaVacia = productosFiltrados.isEmpty()
                    )}
                ///Lista de productos
                items(productosFiltrados) { producto ->
                    Spacer(Modifier.height(12.dp))
                    val stockBajo = producto.stock <= 3
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).border(1.dp, BrandWoodLight, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            // Nombre + chip stock
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Text(text = producto.nombre, fontSize = 17.sp, maxLines = 1, fontWeight = FontWeight.Bold, color = BrandBlack, modifier = Modifier.weight(1f))
                                Surface(color = if (stockBajo) StockLowColor.copy(alpha = 0.12f) else BrandWoodLight.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp)) {
                                    Text(text = "Stock: ${producto.stock}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = if (stockBajo) StockLowColor else BrandTextSecondary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Sucursal: ${producto.local}", style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                            Text("Código: ${producto.modeloCod}", style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Talla: ${producto.talla}", style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                                Text(producto.color, style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                            }
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = BrandWoodLight.copy(alpha = 0.6f))
                            Spacer(Modifier.height(4.dp))
                            // Fila de acciones
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { datoimg = producto.imagenUrl; showIMG = true }) { Icon(Icons.Default.Image, "Ver imagen", tint = BrandWoodMedium) }
                                IconButton(onClick = { selectedProductId = producto.id; showVerDialog = true }) { Icon(Icons.Default.Description, "Ver detalle", tint = BrandWoodMedium) }
                                IconButton(onClick = { selectedProductId = producto.id; showEditDialog = true }) { Icon(Icons.Default.Edit, "Editar", tint = BrandBlack) }
                                var eliminarDialog by remember { mutableStateOf(false) }
                                IconButton(onClick = { eliminarDialog = true }) { Icon(Icons.Default.Delete, "Eliminar", tint = StockLowColor) }
                                if (eliminarDialog) {
                                    AlertDialog(
                                        onDismissRequest = { eliminarDialog = false },
                                        containerColor = BrandWarmWhite,
                                        title = { Text("ELIMINAR PRODUCTO", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                        text = {
                                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("¿Desea eliminar este producto?")
                                                Spacer(Modifier.height(16.dp))
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                    Button(onClick = { loadingMessage = "Eliminando producto..."; isUploading = true; db.collection("productos").document(producto.id).delete().addOnSuccessListener { scope.launch { delay(3000); isUploading = false; eliminarDialog = false; Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show() } } }, colors = ButtonDefaults.buttonColors(containerColor = StockLowColor)) { Text("Eliminar") }
                                                    Spacer(Modifier.width(16.dp))
                                                    TextButton(onClick = { eliminarDialog = false }) { Text("Cerrar") }
                                                }
                                            }
                                        },
                                        confirmButton = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Panel lateral de filtros
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = showFilterPanel, enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 3 }), exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 3 }), modifier = Modifier.align(Alignment.CenterEnd)) {
                Surface(modifier = Modifier.width(280.dp).fillMaxHeight(), tonalElevation = 4.dp, color = BrandWarmWhite) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Spacer(Modifier.height(16.dp))
                        Text("Filtrar por", style = MaterialTheme.typography.titleMedium, color = BrandBlack)
                        Spacer(Modifier.height(8.dp))
                        Text("Talla", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary)
                        TallaDropdown(talla = talla, onTallaChange = { talla = it })
                        Text("Color", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary)
                        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium))
                        Spacer(Modifier.weight(1f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { showFilterPanel = false }, colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWarmWhite), modifier = Modifier.weight(1f)) { Text("Aplicar") }
                            val hayFiltros = talla.isNotBlank() || color.isNotBlank()
                            OutlinedButton(onClick = { if (hayFiltros) { talla = ""; color = "" }; showFilterPanel = false }, modifier = Modifier.weight(1f)) { Text(if (hayFiltros) "Limpiar" else "Cerrar") }
                        }
                    }
                }
            }
        }

        // Dialog imagen
        if (showIMG) {
            AlertDialog(onDismissRequest = { showIMG = false },
                containerColor = BrandWarmWhite,
                text = { Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp), horizontalAlignment = Alignment.CenterHorizontally) { if (datoimg != "") ImagenDesdePosibleBase64OUrl(datoimg, modifier = Modifier.size(400.dp)) else Text("No existe imagen"); Spacer(Modifier.height(12.dp)) } },
                confirmButton = { TextButton(onClick = { showIMG = false }) { Text("Cerrar") } }, dismissButton = {}
            )
        }

        // Dialog detalle
//        if (showVerDialog) {
//            AlertDialog(onDismissRequest = { showVerDialog = false },
//                containerColor = BrandWarmWhite,
//                text = {
//                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
//                        selectedProduct?.let { product ->
//                            Text(product.nombre, style = MaterialTheme.typography.titleLarge, color = BrandBlack)
//                            Spacer(Modifier.height(8.dp))
//                            val detalles = buildList {
//                                add("Codigo" to product.codigo); add("Modelo" to product.modeloCod); add("Tipo" to product.tipo); add("Material" to product.material); add("Marca" to product.marca); add("Color" to product.color); add("Diseño" to product.diseno)
//                                if (product.manga != "") add("Manga" to product.manga)
//                                add("Talla" to product.talla); add("Stock" to product.stock.toString()); add("Corte" to product.corte); add("Local" to product.local); add("Costo" to "S/${product.costo}"); add("Precio Unit" to "S/${product.precio}"); add("Precio x Mayor" to "S/${product.precioxMayor}"); add("Fecha Ingreso" to formatFecha(product.fecha))
//                            }
//                            detalles.forEach { (label, value) ->
//                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//                                    Text("$label:", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), color = BrandTextSecondary)
//                                    Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
//                                }
//                            }
//                        } ?: Text("Sin producto seleccionado")
//                    }
//                },
//                confirmButton = { TextButton(onClick = { showVerDialog = false }) { Text("Cerrar") } }
//            )
//        }
        if (showVerDialog) {

            AlertDialog(
                onDismissRequest = { showVerDialog = false },
                containerColor = BrandWarmWhite,
                shape = RoundedCornerShape(24.dp),

                title = {
                    selectedProduct?.let { product ->

                        Column {

                            Text(
                                text = product.nombre,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlack
                            )

                            Text(
                                text = "Código: ${product.codigo}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BrandTextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            HorizontalDivider()
                        }
                    }
                },

                text = {

                    selectedProduct?.let { product ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 500.dp)
                                .verticalScroll(rememberScrollState())
                        ) {

                            //======================
                            // Información General
                            //======================

                            Text(
                                "📦 Información General",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlack //
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoCard("Modelo", product.modeloCod)
                            InfoCard("Tipo", product.tipo)
                            InfoCard("Material", product.material)
                            InfoCard("Marca", product.marca)
                            InfoCard("Color", product.color)
                            InfoCard("Diseño", product.diseno)

                            if (product.manga.isNotBlank()) {
                                InfoCard("Manga", product.manga)
                            }

                            InfoCard("Talla", product.talla)
                            InfoCard("Corte", product.corte)

                            Spacer(modifier = Modifier.height(16.dp))

                            //======================
                            // Inventario
                            //======================

                            Text(
                                "📦 Inventario",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlack //
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoCard("Stock", product.stock.toString())
                            InfoCard("Local", product.local)
                            InfoCard("Fecha Ingreso", formatFecha(product.fecha))

                            Spacer(modifier = Modifier.height(16.dp))

                            //======================
                            // Precios
                            //======================

                            Text(
                                "💰 Precios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlack //
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            InfoCard("Costo", "S/ ${product.costo}")
                            InfoCard("Precio Unitario", "S/ ${product.precio}")
                            InfoCard("Precio por Mayor", "S/ ${product.precioxMayor}")

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {

                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(30.dp)
                                ) {

                                    Text(
                                        text = "Stock: ${product.stock}",
                                        modifier = Modifier.padding(
                                            horizontal = 18.dp,
                                            vertical = 8.dp
                                        ),
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                    } ?: Text("Sin producto seleccionado")
                },

                confirmButton = {

                    FilledTonalButton(
                        onClick = { showVerDialog = false }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text("Cerrar")
                    }
                }
            )
        }

        // Dialog nuevo producto
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = BrandWarmWhite,
                confirmButton = {
                    Button(
                        onClick = {
                            val nuevoCodigo = if (showmodeloCod) "MOD$modeloCod" else modeloCod
                            modeloCod = nuevoCodigo
                            nombre = "$type $material $brand $color"
                            val stockInt = stock.toIntOrNull() ?: 0
                            val costoInt = costo.toDoubleOrNull() ?: 0.0
                            val precioInt = precio.toDoubleOrNull() ?: 0.0
                            val precioMayInt = precioMay.toDoubleOrNull() ?: 0.0
                            val codigo = "PRD" + System.currentTimeMillis().toString().takeLast(4) + (10..99).random()
                            if (nombre.isBlank()) { Toast.makeText(context, "Ingresa nombre", Toast.LENGTH_SHORT).show(); return@Button }
                            loadingMessage = "Añadiendo producto..."; isUploading = true
                            if (miBitmapSeleccionado != null) {
                                val compressedBytes = bitmapToWebPBytes(miBitmapSeleccionado!!, quality = 65)
                                if (compressedBytes.size > 900_000) { isUploading = false; Toast.makeText(context, "Imagen demasiado grande", Toast.LENGTH_LONG).show(); return@Button }
                                val imagenBase64 = Base64.encodeToString(compressedBytes, Base64.DEFAULT)
                                val nuevoProducto = hashMapOf("codigo" to codigo, "nombre" to nombre, "tipo" to type, "talla" to talla, "stock" to stockInt, "modeloCod" to modeloCod, "color" to color, "corte" to style, "manga" to sleeve, "local" to local, "diseno" to design, "material" to material, "marca" to brand, "precioxMayor" to precioMayInt, "costo" to costoInt, "precio" to precioInt, "fecha" to FieldValue.serverTimestamp(), "imagenUrl" to imagenBase64)
                                db.collection("productos").add(nuevoProducto).addOnSuccessListener { scope.launch { delay(3000); isUploading = false; showContinueDialog = true; Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show(); nombre = ""; type = ""; talla = ""; stock = ""; color = " "; style = ""; sleeve = ""; local = ""; design = ""; material = ""; brand = ""; precioMay = ""; costo = ""; precio = ""; miBitmapSeleccionado = null } }.addOnFailureListener { scope.launch { delay(3000); isUploading = false; showDialog = false; Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show() } }
                            } else {
                                val nuevoProducto = hashMapOf("codigo" to codigo, "nombre" to nombre, "tipo" to type, "talla" to talla, "stock" to stockInt, "color" to color, "modeloCod" to modeloCod, "diseno" to design, "manga" to sleeve, "material" to material, "marca" to brand, "precioxMayor" to precioMayInt, "corte" to style, "local" to local, "costo" to costoInt, "precio" to precioInt, "fecha" to FieldValue.serverTimestamp())
                                db.collection("productos").add(nuevoProducto).addOnSuccessListener { scope.launch { delay(3000); isUploading = false; showContinueDialog = true; Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show() } }.addOnFailureListener { scope.launch { delay(3000); isUploading = false; showDialog = false; Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show() } }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlack, contentColor = BrandWarmWhite)
                    ) { Text("Guardar") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancelar") } },
                title = { Text("➕ Nuevo producto") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 470.dp).verticalScroll(rememberScrollState())) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = showmodeloCod, onCheckedChange = { showmodeloCod = it }, colors = CheckboxDefaults.colors(checkedColor = BrandBlack))
                            Text("Añadir código del modelo")
                        }
                        LaunchedEffect(showmodeloCod) { modeloCod = if (!showmodeloCod && modeloCod.isEmpty()) "MOD" + UUID.randomUUID().toString().take(4) else "" }
                        OutlinedTextField(value = modeloCod, onValueChange = { modeloCod = it }, label = { Text("Codigo del modelo") }, modifier = Modifier.fillMaxWidth(), enabled = showmodeloCod, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClothingItem(type = type, onTypeChange = { type = it }, modifier = Modifier.weight(1f))
                            MaterialItem(material = material, onMaterialChange = { material = it }, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Color") }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), modifier = Modifier.focusRequester(focusRequester), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium))
                        Spacer(Modifier.height(8.dp))
                        BrandItem(brand = brand, onBrandChange = { brand = it })
                        Spacer(Modifier.height(8.dp))
                        DesingItem(desing = design, onDesingChange = { design = it })
                        Spacer(Modifier.height(8.dp))
                        CutItem(corte = style, onCorteChange = { style = it })
                        if (type == "Camisa" || type == "Polo") TypeSleeve(sleeve = sleeve, onSleeveChange = { sleeve = it })
                        Spacer(Modifier.height(8.dp))
                        TallaDropdown(talla = talla, onTallaChange = { talla = it })
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium))
                        Spacer(Modifier.height(8.dp))
                        LocalOption(listLocal = locales, local = local, onLocalChange = { local = it })
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = costo, onValueChange = { costo = it }, label = { Text("Costo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio Unit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = precioMay, onValueChange = { precioMay = it }, label = { Text("Precio Mayor") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandBlack, unfocusedBorderColor = BrandWoodMedium))
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { launcher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = BrandWoodMedium, contentColor = BrandBlack)) { Text("Seleccionar imagen") }
                        miBitmapSeleccionado?.let { bmp -> Image(bitmap = bmp.asImageBitmap(), contentDescription = "Preview", modifier = Modifier.size(120.dp).padding(bottom = 8.dp)) }
                    }
                }
            )
        }

        if (showContinueDialog) {
            AlertDialog(
                onDismissRequest = { showContinueDialog = false },
                title = { Text("Producto guardado") },
                text = { Text("¿Deseas agregar otro producto?") },
                confirmButton = { TextButton(onClick = { nombre = ""; color = ""; stock = ""; showContinueDialog = false; showDialog = true; focusRequester.requestFocus() }) { Text("Sí") } },
                dismissButton = { TextButton(onClick = { nombre = ""; type = ""; talla = ""; stock = ""; color = ""; style = ""; sleeve = ""; local = ""; design = ""; material = ""; brand = ""; precioMay = ""; costo = ""; precio = ""; showContinueDialog = false; showDialog = false }) { Text("No") } }
            )
        }

        // EDITAR UN PRODUCTO
        if (showEditDialog && selectedProduct != null) {
            var showTallaDialog by remember {
                mutableStateOf(false)
            }
            var editNombre by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.nombre) }
            var editTalla by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.talla) }
            var editType by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.tipo) }
            var editColor by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.color) }
            var editManga by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.manga) }
            var editBrand by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.marca) }
            var editDesign by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.diseno) }
            var editStyle by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.corte) }
            var editcodmodel by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.modeloCod) }
            var editMaterial by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.material) }
            var editStock by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.stock.toString()) }
            var editLocal by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.local) }
            var editCosto by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.costo.toString()) }
            var editPrecio by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.precio.toString()) }
            var editprecioMay by rememberSaveable(showEditDialog) { mutableStateOf(selectedProduct.precioxMayor.toString()) }
            Dialog(
                onDismissRequest = { showEditDialog = false }
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = BrandWarmWhite,
                    shadowElevation = 12.dp
                ) {

                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    ) {


                        // TITULO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column {

                                Text(
                                    text = "Editar producto",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlack
                                )

                                Text(
                                    text = selectedProduct.modeloCod,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )

                            }


                            IconButton(
                                onClick = { showEditDialog = false }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar"
                                )

                            }

                        }


                        Spacer(
                            Modifier.height(12.dp)
                        )


                        Divider(
                            color = BrandWoodMedium
                        )


                        Spacer(
                            Modifier.height(12.dp)
                        )


                        Column(
                            modifier = Modifier
                                .heightIn(max = 550.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {


                            Text(
                                "Información del producto",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandBlack
                            )


                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = editcodmodel,
                                onValueChange = { editcodmodel = it },
                                label = {
                                    Text("Código del modelo")
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlack,
                                    unfocusedBorderColor = BrandWoodMedium
                                )
                            )


                            ClothingItem(
                                type = editType,
                                onTypeChange = { editType = it }
                            )


                            MaterialItem(
                                material = editMaterial,
                                onMaterialChange = { editMaterial = it }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        Log.d("TALLA", "CLICK")

                                        showTallaDialog = true

                                    }
                            ){
                                OutlinedTextField(
                                    value = editTalla,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    label = {
                                        Text("Talla")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = BrandWoodMedium,
                                        disabledTextColor = BrandBlack,
                                        disabledLabelColor = BrandBlack
                                    )
                                )

                            }

                            Text(
                                "Detalles",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandBlack
                            )


                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {


                                OutlinedTextField(
                                    modifier = Modifier.weight(1f),
                                    value = editColor,
                                    onValueChange = { editColor = it },
                                    label = { Text("Color") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandBlack,
                                        unfocusedBorderColor = BrandWoodMedium
                                    )
                                )


                                OutlinedTextField(
                                    modifier = Modifier.weight(1f),
                                    value = editStock,
                                    onValueChange = { editStock = it },
                                    label = { Text("Stock") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandBlack,
                                        unfocusedBorderColor = BrandWoodMedium
                                    )
                                )

                            }



                            BrandItem(
                                brand = editBrand,
                                onBrandChange = { editBrand = it }
                            )


                            DesingItem(
                                desing = editDesign,
                                onDesingChange = { editDesign = it }
                            )


                            CutItem(
                                corte = editStyle,
                                onCorteChange = { editStyle = it }
                            )


                            if(
                                editType == "Camisa" ||
                                editType == "Polo"
                            ){

                                TypeSleeve(
                                    sleeve = editManga,
                                    onSleeveChange = { editManga = it }
                                )

                            }



                            Text(
                                "Ubicación",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandBlack
                            )


                            LocalOption(
                                listLocal = locales,
                                local = editLocal,
                                onLocalChange = { editLocal = it }
                            )



                            Text(
                                "Precios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandBlack
                            )


                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = editCosto,
                                onValueChange = { editCosto = it },
                                label = { Text("Costo") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlack,
                                    unfocusedBorderColor = BrandWoodMedium
                                )
                            )


                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = editprecioMay,
                                onValueChange = { editprecioMay = it },
                                label = { Text("Precio Mayor") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlack,
                                    unfocusedBorderColor = BrandWoodMedium
                                )
                            )


                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = editPrecio,
                                onValueChange = { editPrecio = it },
                                label = { Text("Precio") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandBlack,
                                    unfocusedBorderColor = BrandWoodMedium
                                )
                            )



                            Text(
                                "Imagen",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = BrandBlack
                            )



                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    launcher.launch("image/*")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandWoodMedium,
                                    contentColor = BrandBlack
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ){

                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null
                                )

                                Spacer(
                                    Modifier.width(8.dp)
                                )

                                Text("Seleccionar imagen")

                            }



                            miBitmapSeleccionado?.let { bmp ->

                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Preview",
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(
                                            RoundedCornerShape(16.dp)
                                        )
                                        .align(
                                            Alignment.CenterHorizontally
                                        )
                                )

                            }


                        }



                        Spacer(
                            Modifier.height(16.dp)
                        )



                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ){

                            TextButton(
                                onClick = {
                                    showEditDialog=false
                                }
                            ){
                                Text("Cancelar")
                            }


                            Spacer(
                                Modifier.width(8.dp)
                            )


                            Button(
                                onClick = {

                                    editNombre =
                                        "$editType $editMaterial $editBrand $editColor"

                                    val stockInt =
                                        editStock.toIntOrNull() ?: 0

                                    val costoInt =
                                        editCosto.toDoubleOrNull() ?: 0.0

                                    val precioInt =
                                        editPrecio.toDoubleOrNull() ?: 0.0

                                    val precioMayor =
                                        editprecioMay.toDoubleOrNull() ?: 0.0


                                    loadingMessage =
                                        "Editando producto..."

                                    isUploading = true


                                    val actualizacion = mapOf(
                                        "nombre" to editNombre,
                                        "tipo" to editType,
                                        "material" to editMaterial,
                                        "talla" to editTalla,
                                        "diseno" to editDesign,
                                        "color" to editColor,
                                        "modeloCod" to editcodmodel,
                                        "marca" to editBrand,
                                        "manga" to editManga,
                                        "stock" to stockInt,
                                        "corte" to editStyle,
                                        "local" to editLocal,
                                        "costo" to costoInt,
                                        "precio" to precioInt,
                                        "precioXMayor" to precioMayor
                                    )


                                    db.collection("productos")
                                        .document(selectedProduct.id)
                                        .update(actualizacion)
                                        .addOnSuccessListener {

                                            scope.launch {

                                                delay(3000)

                                                isUploading=false
                                                showEditDialog=false

                                                Toast.makeText(
                                                    context,
                                                    "Producto actualizado",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                        }

                                        .addOnFailureListener {

                                            Toast.makeText(
                                                context,
                                                "Error al actualizar: ${it.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }


                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandBlack,
                                    contentColor = BrandWarmWhite
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ){

                                Text("Guardar")

                            }

                        }

                    }

                }

            }
            if(showTallaDialog){

                TallaSelectorDialog(
                    tallaActual = editTalla,

                    onTallaSeleccionada = {
                        editTalla = it
                        showTallaDialog = false
                    },

                    onCerrar = {
                        showTallaDialog = false
                    }
                )

            }
        }
        // Dialog imagen zoom
        if (showImageDialog) {
            Dialog(onDismissRequest = { showImageDialog = false }) {
                var scale by remember { mutableFloatStateOf(3f) }
                var offset by remember { mutableStateOf(Offset.Zero) }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)).clickable { showImageDialog = false }, contentAlignment = Alignment.Center) {
                    selectedProduct?.imagenUrl?.let { urlOrBase64 ->
                        ImagenDesdePosibleBase64OUrl(urlOrBase64, modifier = Modifier.fillMaxWidth()
                            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { scale = if (scale > 1f) 1f else 3f; offset = Offset.Zero }) }
                            .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(1f, 4f); if (scale > 1f) { val maxX = (size.width * (scale - 1)) / 2; val maxY = (size.height * (scale - 1)) / 2; offset = Offset(x = (offset.x + pan.x).coerceIn(-maxX, maxX), y = (offset.y + pan.y).coerceIn(-maxY, maxY)) } else { offset = Offset.Zero } } }
                            .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                        )
                    }
                }
            }
        }
    }
}

fun bitmapToWebPBytes(bitmap: Bitmap, quality: Int = 80): ByteArray {
    val stream = ByteArrayOutputStream()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, stream)
    else { @Suppress("DEPRECATION")
    bitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream) }
    return stream.toByteArray()
}

@Composable
fun ImagenDesdePosibleBase64OUrl(data: String?, modifier: Modifier = Modifier) {
    if (data.isNullOrEmpty()) return
    if (data.startsWith("http://") || data.startsWith("https://")) {
        AsyncImage(model = data, contentDescription = null, modifier = modifier.size(150.dp).padding(bottom = 12.dp))
    } else {
        val bitmap: Bitmap? = try { val imageBytes = Base64.decode(data, Base64.DEFAULT); BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) } catch (e: Exception) { Log.e("ImagenDecode", "Error: ${e.message}"); null }
        bitmap?.let { Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = modifier.size(150.dp).padding(bottom = 12.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalOption(listLocal: List<Local>, local: String, onLocalChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = local, onValueChange = {}, readOnly = true, label = { Text("Local") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) {
                listLocal.forEach { opcion -> Log.d("datos", "es:$opcion"); DropdownMenuItem(text = { Text(opcion.nombre) }, onClick = { onLocalChange(opcion.nombre); expanded = false }) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallaDropdown(talla: String, onTallaChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val tallasLetras = listOf("S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL", "6XL")
    val tallasNumeros = listOf("26", "28", "30", "32", "34", "36", "38", "40", "42")
    var tipo by rememberSaveable(talla) { mutableStateOf(when (talla) { in tallasLetras -> "Letras"; in tallasNumeros -> "Numeros"; else -> "" }) }
    val opciones = listOf("Letras", "Numeros")
    val valores = if (tipo == "Letras") tallasLetras else tallasNumeros
    var showError by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp), horizontalAlignment = Alignment.Start) {
            opciones.forEach { opcion -> Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { tipo = opcion }) { RadioButton(selected = tipo == opcion, onClick = { tipo = opcion }); Text(opcion, modifier = Modifier.padding(start = 1.dp)) } }
        }
        Column {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { if (tipo.isNotEmpty()) { expanded = !expanded; showError = false } else showError = true }) {
                OutlinedTextField(value = talla, onValueChange = {}, readOnly = true, enabled = tipo.isNotEmpty(), label = { Text("Talla") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, isError = showError, modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).width(120.dp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) { valores.forEach { opcion -> Log.d("datos", "es:$opcion"); DropdownMenuItem(text = { Text(opcion) }, onClick = { onTallaChange(opcion); expanded = false }) } }
                if (showError) Text("Selecciona si es letras o N°", color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp, top = 2.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandItem(brand: String, onBrandChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val brandList = listOf("H.BOSS","POLO.R.L","THE.NORTH.FACE","LACOSTE","DOCKER","COLUMBIA","TOMMY","L'GANTS","LA.MARTINA","ARMANI","NIKE","ADIDAS","MR.GIORGIO","FLSZ","FASHION","PEPUÑO","DIANA RK.","MAXFLER")
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = brand, onValueChange = {}, readOnly = true, label = { Text("Marca") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
            Box { ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) { brandList.forEach { opcion -> DropdownMenuItem(text = { Text(opcion, fontWeight = if (opcion == brand) FontWeight.Bold else FontWeight.Normal, color = if (opcion == brand) BrandBlack else Color.Unspecified) }, onClick = { onBrandChange(opcion); expanded = false }) } } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeSleeve(sleeve: String, onSleeveChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val sleeveList = listOf("M. Corta", "M. Larga")
    ExposedDropdownMenuBox(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp), expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = sleeve, onValueChange = {}, readOnly = true, label = { Text("Manga") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) { sleeveList.forEach { opcion -> DropdownMenuItem(text = { Text(opcion, fontWeight = if (opcion == sleeve) FontWeight.Bold else FontWeight.Normal, color = if (opcion == sleeve) BrandBlack else Color.Unspecified) }, onClick = { onSleeveChange(opcion); expanded = false }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialItem(material: String, onMaterialChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val materialList = listOf("Drill","Satinada","Oxford","Jean","Mezclilla","Seda","Pique","Pima","Chalis","Lino","Hilo","Dralon.Bayer","Acolchado","Pluma","Tazlan","Cordelina","Gamuza","Cuero Guante","Cuero","Algodón","Nylon","Poliester")
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(value = material, onValueChange = {}, readOnly = true, singleLine = true, label = { Text("Tela") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) { materialList.forEach { opcion -> DropdownMenuItem(text = { Text(opcion, fontWeight = if (opcion == material) FontWeight.Bold else FontWeight.Normal, color = if (opcion == material) BrandBlack else Color.Unspecified) }, onClick = { onMaterialChange(opcion); expanded = false }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesingItem(desing: String, onDesingChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val desingList = listOf("C.Entero", "Bicolor", "Floreada", "Rayada", "Miniaturas")
    ExposedDropdownMenuBox(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp), expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = desing, onValueChange = {}, readOnly = true, label = { Text("Estilo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) { desingList.forEach { opcion -> DropdownMenuItem(text = { Text(opcion, fontWeight = if (opcion == desing) FontWeight.Bold else FontWeight.Normal, color = if (opcion == desing) BrandBlack else Color.Unspecified) }, onClick = { onDesingChange(opcion); expanded = false }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CutItem(corte: String, onCorteChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val styleList = listOf("Regular", "Clasico", "Slim Fit")
    ExposedDropdownMenuBox(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp), expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(value = corte, onValueChange = {}, readOnly = true, label = { Text("Corte") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) { styleList.forEach { opcion -> DropdownMenuItem(text = { Text(opcion, fontWeight = if (opcion == corte) FontWeight.Bold else FontWeight.Normal, color = if (opcion == corte) BrandBlack else Color.Unspecified) }, onClick = { onCorteChange(opcion); expanded = false }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingItem(type: String, onTypeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val typeList = listOf("Camisa","Camisa dama","Casaca","Casaca Bomber","Casaca Cortaviento","Casaca acolchada","Casaca polar","Casaca plush","Blusas","Blusones","Polar","Polo","Polo Camisero","Chaleco","Chompas","Blaizer","Pantalon","Short","Gorro","Llavero","Billetera","Correa")
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(value = type, onValueChange = {}, singleLine = true, readOnly = true, label = { Text("Tipo") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 200.dp)) { typeList.forEach { opcion -> DropdownMenuItem(text = { Text(opcion, fontWeight = if (opcion == type) FontWeight.Bold else FontWeight.Normal, color = if (opcion == type) BrandBlack else Color.Unspecified) }, onClick = { onTypeChange(opcion); expanded = false }) } }
    }
}

@Composable
fun LoadingDialog(message: String, onDismiss: (() -> Unit)? = null) {
    Dialog(onDismissRequest = { onDismiss?.invoke() }) {
        Box(modifier = Modifier.size(200.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = BrandBlack)
                Spacer(Modifier.height(12.dp))
                Text(message, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun StateChargePorducts(isLoading: Boolean, listaVacia:Boolean, modifier: Modifier = Modifier){
    when{
        isLoading ->{
            Box(
                modifier = Modifier.fillMaxWidth().padding(top=48.dp),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator(color = BrandBlack)
            }
        }
        listaVacia ->{
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "No se encontraron productos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandTextSecondary
                )
            }
        }
    }
}

// DISEÑO DE CARD PARA DIALOG- INFORMACION DEL PRODUCTO
@Composable
fun InfoCard(
    titulo: String,
    valor: String
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF7F7F7)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = BrandTextSecondary
            )

            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )
        }
    }
}

// DISEÑO DEL SELECTOR DE TALLAS
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TallaSelectorDialog(
    tallaActual: String,
    onTallaSeleccionada: (String) -> Unit,
    onCerrar: () -> Unit
){

    val tallasLetras = listOf(
        "S",
        "M",
        "L",
        "XL",
        "2XL",
        "3XL",
        "4XL",
        "5XL",
        "6XL"
    )


    val tallasNumeros = listOf(
        "26",
        "28",
        "30",
        "32",
        "34",
        "36",
        "38",
        "40",
        "42"
    )


    var tipoTalla by remember {

        mutableStateOf(

            if(tallaActual in tallasLetras)
                "Letras"
            else
                "Números"

        )

    }


    val opciones = if(tipoTalla == "Letras")
        tallasLetras
    else
        tallasNumeros



    Dialog(
        onDismissRequest = {
            onCerrar()
        }
    ){


        Surface(

            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),

            shape = RoundedCornerShape(24.dp),

            color = BrandWarmWhite

        ){


            Column(

                modifier = Modifier
                    .padding(20.dp)

            ){


                Text(

                    text = "Seleccionar talla",

                    fontSize = 20.sp,

                    fontWeight = FontWeight.Bold,

                    color = BrandBlack

                )


                Spacer(
                    Modifier.height(16.dp)
                )



                Text(

                    text = "Tipo de talla",

                    fontWeight = FontWeight.SemiBold,

                    color = BrandBlack

                )


                Spacer(
                    Modifier.height(8.dp)
                )



                Row(

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)

                ){


                    listOf(
                        "Letras",
                        "Números"
                    ).forEach { tipo ->


                        val seleccionado =
                            tipoTalla == tipo



                        Surface(

                            modifier = Modifier
                                .weight(1f)
                                .clickable {

                                    tipoTalla = tipo

                                },

                            shape =
                                RoundedCornerShape(12.dp),


                            color =
                                if(seleccionado)
                                    BrandBlack
                                else
                                    BrandWarmWhite,


                            border =
                                BorderStroke(
                                    1.dp,
                                    BrandWoodMedium
                                )

                        ){


                            Box(

                                modifier =
                                    Modifier.height(42.dp),

                                contentAlignment =
                                    Alignment.Center

                            ){


                                Text(

                                    text = tipo,

                                    color =
                                        if(seleccionado)
                                            BrandWarmWhite
                                        else
                                            BrandBlack

                                )


                            }


                        }


                    }


                }



                Spacer(
                    Modifier.height(18.dp)
                )



                Text(

                    text = "Seleccione una talla",

                    fontWeight = FontWeight.SemiBold,

                    color = BrandBlack

                )



                Spacer(
                    Modifier.height(10.dp)
                )



                FlowRow(

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)

                ){


                    opciones.forEach { talla ->


                        val seleccionado =
                            tallaActual == talla



                        Surface(

                            modifier =
                                Modifier
                                    .clickable {

                                        onTallaSeleccionada(talla)

                                    },


                            shape =
                                RoundedCornerShape(12.dp),


                            color =
                                if(seleccionado)
                                    BrandBlack
                                else
                                    BrandWarmWhite,


                            border =
                                BorderStroke(
                                    1.dp,
                                    BrandWoodMedium
                                )


                        ){


                            Box(

                                modifier =
                                    Modifier
                                        .width(55.dp)
                                        .height(42.dp),

                                contentAlignment =
                                    Alignment.Center

                            ){


                                Text(

                                    text = talla,

                                    color =
                                        if(seleccionado)
                                            BrandWarmWhite
                                        else
                                            BrandBlack,


                                    fontWeight =
                                        FontWeight.SemiBold

                                )


                            }


                        }


                    }


                }



                Spacer(
                    Modifier.height(20.dp)
                )



                TextButton(

                    modifier =
                        Modifier.align(
                            Alignment.End
                        ),

                    onClick = {
                        onCerrar()
                    }

                ){

                    Text(
                        "Cancelar"
                    )

                }


            }


        }


    }


}