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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
//USAMOS UN ALIAS PORQUE HAY CONFLICTO CON EL OTRO .Color de abajo
import androidx.compose.ui.graphics.Color
//LIBRERIAS DE PRUEBA IMAGEN 64
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
////////////////////////////////////
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


data class Producto(
    val id: String = "", // ← ID del documento en Firestore
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
    val imagenUrl: String = "" // 🔥 Nuevo campo para la imagen
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(navController: NavHostController, codigoEscaneado: String = "") {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var productos by remember { mutableStateOf(listOf<Producto>()) }
    var locales by remember { mutableStateOf(listOf<Local>()) }
    var datoimg by remember { mutableStateOf("") }
    //DIALOGS
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showVerDialog by remember { mutableStateOf(false) }
    var showImageDialog by rememberSaveable { mutableStateOf(false) }
    var showIMG by rememberSaveable { mutableStateOf(false) }
    var showContinueDialog by remember { mutableStateOf(false) }


    // SELECCIONA EL PRODUCTO PARA SU VISUALIZACION/EDICION 0 DELETE
    // 🔹 Estado para guardar solo el ID del producto seleccionado
    var selectedProductId by rememberSaveable { mutableStateOf<String?>(null) }

// 🔹 Encuentra el producto seleccionado según el ID
    val selectedProduct = productos.find { it.id == selectedProductId }

    //para guardar la IMG
    var miBitmapSeleccionado by remember { mutableStateOf<Bitmap?>(null) }
    //if PARA LA ANIMACION DE CARGA
    var isUploading by remember { mutableStateOf(false) }
    //VARIABLE DEL MENSAJE DE LA ANIMACION
    var loadingMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope() // recordar un CoroutineScope

    // Estados para el formulario
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
    // 🔹 Objeto que controla el foco
    val focusRequester = remember { FocusRequester() }

    // Permiso de la camara para escanear qr
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                navController.navigate("scanner")
            } else {
                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Leer datos desde Firestore
    LaunchedEffect(Unit) {
        db.collection("productos").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                productos = it.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
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

    // LACUNCHER QUE PERMITE
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // API 28+: ImageDecoder
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            } else {
                // API <28: MediaStore
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            }

            miBitmapSeleccionado = bitmap
        }
    }

    // en el scope donde manejas showDialog/isUploading (por ejemplo dentro del if(showDialog) o al final del Composable)
    if (isUploading) {
        LoadingDialog(message = loadingMessage)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
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
            val coincideTexto = query.isBlank() ||
                    producto.nombre.contains(query, ignoreCase = true) ||
                    producto.codigo.contains(query, ignoreCase = true) ||
                    producto.modeloCod.contains(query, ignoreCase = true)

            val coincideLocal = selectedLocal.isBlank() ||
                    producto.local.equals(selectedLocal, ignoreCase = true)
            val coincideTalla = talla.isBlank() || producto.talla.equals(talla, ignoreCase = true)

            val coincideColor = color.isBlank() || producto.color.contains(color, ignoreCase = true)

            coincideTexto && coincideLocal && coincideTalla && coincideColor

        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp, top = 18.dp, 18.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 1) El título es un item normal: se desliza y desaparece al bajar
                item {
                    Text(
                        text = "\uD83D\uDCD1 Inventario",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), // más pequeño y equilibrado
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                }
                // 2) El buscador como stickyHeader: se "pega" arriba cuando scrolleas
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
                                    OutlinedTextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        placeholder = { Text("Buscar…") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = "Buscar"
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .shadow(
                                                2.dp,
                                                RoundedCornerShape(28.dp)
                                            ), // Sombra sutil
                                        shape = RoundedCornerShape(28.dp),
                                        singleLine = true,
                                        trailingIcon = {
                                            Row {
                                                // Mostrar ícono de limpiar solo si hay texto
                                                if (query.isNotEmpty()) {
                                                    IconButton(onClick = { query = "" }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Borrar texto"
                                                        )
                                                    }
                                                }
                                                IconButton(onClick = {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                }) {
                                                    Icon(
                                                        Icons.Default.QrCodeScanner,
                                                        contentDescription = "Escanear QR"
                                                    )
                                                }
                                            }
                                        }

                                    )
                                }
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
                                    IconButton(
                                        onClick = { showFilterPanel = true },
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = "Filtrar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                            }

                        }
                    }
                }
                items(productosFiltrados) { producto ->
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Izquierda: Info del producto
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = producto.nombre,
                                    fontSize = 18.sp,               // más grande
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold,   // en negrita
                                    color = MaterialTheme.colorScheme.primary // color llamativo
                                )
                                Text("Sucursal: ${producto.local}")
                                Text("Codigo: ${producto.modeloCod}")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Talla: ${producto.talla}")
                                    Text("Stock: ${producto.stock}")
                                    Text(producto.color)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    //Icono del a imagen del producto
                                    IconButton(onClick = {
                                        datoimg = producto.imagenUrl
                                        showIMG = true
                                    }) {
                                        Icon(
                                            Icons.Default.Image,
                                            "Ver",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    //Icono del detaller del producto
                                    IconButton(onClick = {
                                        selectedProductId = producto.id
                                        showVerDialog = true
                                    }) {
                                        Icon(
                                            Icons.Default.Description,
                                            "Ver",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            //COLUMN Iconos (ver , editar, borrar)
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(top = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                //BOTON DE EDITAR
                                IconButton(onClick = {
                                    // Mostrar diálogo de edición
                                    selectedProductId = producto.id
                                    showEditDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                //BOTOM DE ELIMINAR
                                var eliminarDialog by remember { mutableStateOf(false) }
                                IconButton(onClick = { eliminarDialog = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                                //VENTANA DE ELIMINAR
                                if (eliminarDialog) {
                                    AlertDialog(
                                        onDismissRequest = { eliminarDialog = false },
                                        title = {
                                            Text(
                                                "ELIMINAR PRODUCTO",
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        text = {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("¿Desea eliminar este producto?")
                                                Spacer(modifier = Modifier.height(16.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Button(onClick = {
                                                        loadingMessage = "Eliminando producto..."
                                                        isUploading = true
                                                        // Eliminar el producto por su código
                                                        db.collection("productos")
                                                            .document(producto.id).delete()
                                                            .addOnSuccessListener {
                                                                scope.launch {
                                                                    delay(3000) // espera 3 segundos
                                                                    isUploading = false
                                                                    eliminarDialog = false
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Producto eliminado",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                    }) {
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
                                        confirmButton = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            // Contenido principal (tu lista, buscador, etc.)
//            Column(modifier = Modifier.fillMaxSize()) {
//                // Aquí va tu LazyColumn, buscador, etc.
//            }
            // Panel lateral tipo "side sheet"
            AnimatedVisibility(
                visible = showFilterPanel,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it / 3 }), // 👈 se desliza un poco desde la derecha
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 3 }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
//                        .align(Alignment.CenterEnd), // 👈 se ancla al borde derecho
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(Modifier.height(16.dp)) // 👈 Espacio superior

                        Text("Filtrar por", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(16.dp)) // 👈 Espacio superior
                        // 🔹 Filtro por Talla
                        Text("Talla", style = MaterialTheme.typography.labelMedium)
                        TallaDropdown(
                            talla = talla,
                            onTallaChange = { talla = it }
                        )

                        // 🔹 Filtro por Color
                        Text("Color", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                        )
                        Spacer(Modifier.weight(1f)) // 👈 Empuja el botón hacia abajo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Aplica filtros aquí
                                    showFilterPanel = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Aplicar filtros")
                            }

                            val hayFiltrosActivos = talla.isNotBlank() || color.isNotBlank()

                            OutlinedButton(
                                onClick = {
                                    if (hayFiltrosActivos) {
                                        talla = ""
                                        color = ""
                                    }
                                    showFilterPanel = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (hayFiltrosActivos) "Limpiar filtros" else "Cerrar")
                            }
                        }


                    }


                }
            }
        }


        //MUESTRA LA IMG
        if (showIMG) {
            AlertDialog(
                onDismissRequest = { showIMG = false },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp), // Limita el alto del contenido
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Usamos la función que detecta Base64 o URL
                        if (datoimg != "") {
                            ImagenDesdePosibleBase64OUrl(
                                datoimg,
                                modifier = Modifier
                                    .size(400.dp)
                                    .clickable {
//                                        showImageDialog = true
                                    }) //👈 abrir al hacer clic
                        } else {
                            Text("No existe imagen")
                        }
                        Spacer(modifier = Modifier.height(12.dp)) // Espacio más compacto
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.Center
//                        ) {
//                            Spacer(modifier = Modifier.width(16.dp))
//                            TextButton(onClick = { showIMG = false }) {
//                                Text("Cerrar")
//                            }
//                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showIMG = false }) {
                        Text("Cerrar")
                    }
                },
                dismissButton = {}
            )
        }

        //ALGORITMO QUE MUESTRA EL DETALLE DEL PRODUCTO
        if (showVerDialog) {
            AlertDialog(
                onDismissRequest = { showVerDialog = false },
                text = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            selectedProduct?.let { product ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = product.nombre,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(8.dp))

                                    // Agrupamos los datos secundarios
                                    val detalles = buildList {
                                        add("Codigo" to product.codigo)
                                        add("Modelo" to product.modeloCod)
                                        add("Tipo" to product.tipo)
                                        add("Material" to product.material)
                                        add("Marca" to product.marca)
                                        add("Color" to product.color)
                                        add("Diseño" to product.diseno)

                                        //si hay manga que lo coloque
                                        if (product.manga != "") {
                                            add("Manga" to product.manga)
                                        }
                                        add("Talla" to product.talla)
                                        add("Stock" to product.stock.toString())
                                        add("Corte" to product.corte)
                                        add("Local" to product.local)
                                        add("Costo" to "S/${product.costo}")
                                        add("Precio Unit" to "S/${product.precio}")
                                        add("Precio x Mayor" to "S/${product.precioxMayor}")
                                        add("Fecha Ingreso" to formatFecha(product.fecha))
                                    }


                                    detalles.forEach { (label, value) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "$label:",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 15.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = value,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 15.sp
                                                ),
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                }

                            } ?: Text("Sin producto seleccionado")
                        }
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.padding(start = 16.dp)
//                        ) {
//                            // Usamos la función que detecta Base64 o URL
//                            ImagenDesdePosibleBase64OUrl(
//                                selectedProduct?.imagenUrl,
//                                modifier = Modifier
//                                    .size(120.dp)
//                                    .clickable { showImageDialog = true }) //👈 abrir al hacer clic
//                        }
                    }

                },
                confirmButton = {
                    TextButton(onClick = { showVerDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }

        // ALGORITMO QUE INGRESA NUEVO PRODUCTO
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { }, //showDialog = false
                confirmButton = {
                    Button(onClick = {
                        val nuevoCodigo = if (showmodeloCod) {
                            "MOD$modeloCod"
                        } else modeloCod

                        modeloCod = nuevoCodigo
                        nombre = "$type $material $brand $color"
                        val stockInt = stock.toIntOrNull() ?: 0
                        val costoInt = costo.toDoubleOrNull() ?: 0
                        val precioInt = precio.toDoubleOrNull() ?: 0
                        val precioMayInt = precioMay.toDoubleOrNull() ?: 0
                        val codigo = "PRD" + System.currentTimeMillis().toString()
                            .takeLast(4) + (10..99).random() // Genera código único
                        // validaciones simples
                        if (nombre.isBlank()) {
                            Toast.makeText(context, "Ingresa nombre", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        loadingMessage = "Añadiendo producto..."
                        isUploading = true // 🔹 Mostrar animacion de carga

                        // Si ya tienes un Bitmap cargado desde galería o cámara:
                        if (miBitmapSeleccionado != null) {

                            // Comprimimos a WebP (bytes) para control de tamaño y luego lo codificamos a base64
                            val compressedBytes =
                                bitmapToWebPBytes(miBitmapSeleccionado!!, quality = 65)
                            Log.d("pesoimg", "peso:$compressedBytes")
                            // Control simple del tamaño: si > 900 KB evitamos subirlo como Base64 (porque Firestore doc limit ~1MB)
                            if (compressedBytes.size > 900_000) {
                                isUploading = false
                                Toast.makeText(
                                    context,
                                    "Imagen demasiado grande para guardar en Firestore (usa Storage)",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val imagenBase64 =
                                Base64.encodeToString(compressedBytes, Base64.DEFAULT)
                            val nuevoProducto = hashMapOf(
                                "codigo" to codigo,
                                "nombre" to nombre,
                                "tipo" to type,
                                "talla" to talla,
                                "stock" to stockInt,
                                "modeloCod" to modeloCod,
                                "color" to color,
                                "corte" to style,
                                "manga" to sleeve,
                                "local" to local,
                                "diseno" to design,
                                "material" to material,
                                "marca" to brand,
                                "precioxMayor" to precioMayInt,
                                "costo" to costoInt,
                                "precio" to precioInt,
                                "fecha" to FieldValue.serverTimestamp(), // aquí se guarda la hora del servidor
                                "imagenUrl" to imagenBase64 // 🔥 Guarda img base64
                            )

                            db.collection("productos").add(nuevoProducto)
                                .addOnSuccessListener {
                                    scope.launch {
                                        delay(3000) // espera 3 segundos
                                        isUploading = false
                                        showContinueDialog = true  // 👈 mostrar el nuevo diálogo
//                                        showDialog = false
                                        Toast.makeText(
                                            context,
                                            "Producto agregado",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        nombre = ""
                                        type = ""
                                        talla = ""
                                        stock = ""
                                        color = " "
                                        style = ""
                                        sleeve = ""
                                        local = ""
                                        design = ""
                                        material = ""
                                        brand = ""
                                        precioMay = ""
                                        costo = ""
                                        precio = ""
                                        miBitmapSeleccionado = null
                                    }
                                }
                                .addOnFailureListener {
                                    scope.launch {
                                        delay(3000) // espera 3 segundos
                                        isUploading = false
                                        showDialog = false
                                        Toast.makeText(
                                            context,
                                            "Error: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            // Si no hay imagen, guardar normal
                            val nuevoProducto = hashMapOf(
                                "codigo" to codigo,
                                "nombre" to nombre,
                                "tipo" to type,
                                "talla" to talla,
                                "stock" to stockInt,
                                "color" to color,
                                "modeloCod" to modeloCod,
                                "diseno" to design,
                                "manga" to sleeve,
                                "material" to material,
                                "marca" to brand,
                                "precioxMayor" to precioMayInt,
                                "corte" to style,
                                "local" to local,
                                "costo" to costoInt,
                                "precio" to precioInt,
                                "fecha" to FieldValue.serverTimestamp() // aquí se guarda la hora del servidor
                            )
                            db.collection("productos").add(nuevoProducto)
                                .addOnSuccessListener {
                                    scope.launch {
                                        delay(3000) // espera 3 segundos
                                        isUploading = false
                                        showContinueDialog = true  // 👈 mostrar el nuevo diálogo
//                                        showDialog = false
                                        Toast.makeText(
                                            context,
                                            "Producto agregado",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
//                                        nombre = ""
//                                        type = ""
//                                        talla = ""
//                                        stock = ""
//                                        color = ""
//                                        style = ""
//                                        sleeve = ""
//                                        local = ""
//                                        design = ""
//                                        material = ""
//                                        brand = ""
//                                        precioMay = ""
//                                        costo = ""
//                                        precio = ""
                                    }
                                }
                                .addOnFailureListener {
                                    scope.launch {
                                        delay(3000) // espera 3 segundos
                                        isUploading = false
                                        showDialog = false
                                        Toast.makeText(
                                            context,
                                            "Error: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text(" ➕ Nuevo producto") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 470.dp) // límite de alto del diálogo
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = showmodeloCod,
                                onCheckedChange = { showmodeloCod = it }
                            )
                            Text("Añadir código del modelo")
                        }
                        LaunchedEffect(showmodeloCod) {
                            modeloCod = if (!showmodeloCod && modeloCod.isEmpty()) {
                                "MOD" + UUID.randomUUID().toString().take(4)
                            } else {
                                ""
                            }
                        }
                        OutlinedTextField(
                            value = modeloCod,
                            onValueChange = { modeloCod = it },
                            label = { Text("Codigo del modelo") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = showmodeloCod // 🔹 solo editable si showmodeloCod es true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp) // espacio entre dropdowns

                        ) {
                            ClothingItem(
                                type = type,
                                onTypeChange = { type = it },
                                modifier = Modifier.weight(1f)
                            )
                            MaterialItem(
                                material = material,
                                onMaterialChange = { material = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Color") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.focusRequester(focusRequester), // conectamos el FocusRequester aquí
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BrandItem(
                            brand = brand,
                            onBrandChange = { brand = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DesingItem(
                            desing = design,
                            onDesingChange = { design = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CutItem(
                            corte = style,
                            onCorteChange = { style = it }

                        )
                        if (type == "Camisa" || type == "Polo") {
                            TypeSleeve(
                                sleeve = sleeve,
                                onSleeveChange = { sleeve = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TallaDropdown(
                            talla = talla,
                            onTallaChange = { talla = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LocalOption(
                            listLocal = locales,
                            local = local,
                            onLocalChange = { local = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = costo,
                            onValueChange = { costo = it },
                            label = { Text("Costo") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = precio,
                            onValueChange = { precio = it },
                            label = { Text("Precio Unit") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = precioMay,
                            onValueChange = { precioMay = it },
                            label = { Text("Precio Mayor") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { launcher.launch("image/*") }) {
                            Text("Seleccionar imagen")
                        }
                        // Preview local (si eligió una imagen)
                        miBitmapSeleccionado?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Preview imagen seleccionada",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            )
        }
        if (showContinueDialog) {
            AlertDialog(
                onDismissRequest = { showContinueDialog = false },
                title = { Text("Producto guardado") },
                text = { Text("¿Deseas agregar otro producto?") },
                confirmButton = {
                    TextButton(onClick = {
                        // Vaciar solo color, talla y stock
                        nombre = ""
                        color = ""
                        stock = ""
                        showContinueDialog = false
                        showDialog = true // reabrir formulario
                        focusRequester.requestFocus() // 🔹 mueve el cursor a "nombre"
                    }) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        nombre = ""
                        type = ""
                        talla = ""
                        stock = ""
                        color = ""
                        style = ""
                        sleeve = ""
                        local = ""
                        design = ""
                        material = ""
                        brand = ""
                        precioMay = ""
                        costo = ""
                        precio = ""
                        showContinueDialog = false
                        showDialog = false // cerrar todo
                    }) {
                        Text("No")
                    }
                }
            )
        }


        //ALGORITMO QUE EDITA EL PRODUCTO
        if (showEditDialog && selectedProduct != null) {
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
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                confirmButton = {
                    Button(onClick = {
                        editNombre = "$editType $editMaterial $editBrand $editColor"
                        val stockInt = editStock.toIntOrNull() ?: 0
                        val costoInt = editCosto.toDoubleOrNull() ?: 0
                        val precioInt = editPrecio.toDoubleOrNull() ?: 0
                        val precioMayor = editprecioMay.toDoubleOrNull() ?: 0
                        loadingMessage = "Editando producto..."
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
                        db.collection("productos").document(selectedProduct.id)
                            .update(actualizacion)
                            .addOnSuccessListener {
                                scope.launch {
                                    delay(3000) // espera 3 segundos
                                    isUploading = false
                                    showEditDialog = false
                                    Toast.makeText(
                                        context,
                                        "Producto actualizado",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Error al actualizar: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showEditDialog = false
                            }
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Editar producto") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 470.dp) // límite de alto del diálogo
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editcodmodel,
                            onValueChange = { editcodmodel = it },
                            label = { Text("Codigo del modelo") },
                        )
                        ClothingItem(
                            type = editType,
                            onTypeChange = { editType = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        MaterialItem(
                            material = editMaterial,
                            onMaterialChange = { editMaterial = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TallaDropdown(
                            talla = editTalla,
                            onTallaChange = { editTalla = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editStock,
                            onValueChange = { editStock = it },
                            label = { Text("Stock") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editColor,
                            onValueChange = { editColor = it },
                            label = { Text("Color") },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BrandItem(
                            brand = editBrand,
                            onBrandChange = { editBrand = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DesingItem(
                            desing = editDesign,
                            onDesingChange = { editDesign = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CutItem(
                            corte = editStyle,
                            onCorteChange = { editStyle = it }

                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (editType == "Camisa" || editType == "Polo") {
                            TypeSleeve(
                                sleeve = editManga,
                                onSleeveChange = { editManga = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LocalOption(
                            listLocal = locales,
                            local = editLocal,
                            onLocalChange = { editLocal = it }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editCosto,
                            onValueChange = { editCosto = it },
                            label = { Text("Costo") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editprecioMay,
                            onValueChange = { editprecioMay = it },
                            label = { Text("Precio Mayor") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPrecio,
                            onValueChange = { editPrecio = it },
                            label = { Text("Precio") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { launcher.launch("image/*") }) {
                            Text("Seleccionar imagen")
                        }
                        // Preview local (si eligió una imagen)
                        miBitmapSeleccionado?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Preview imagen seleccionada",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            )
        }

        //PERMITE VER LA IMG MAS GRANDE E INTERACTUAR
        if (showImageDialog) {
            Dialog(onDismissRequest = { showImageDialog = false }) {
                // Estados
                var scale by remember { mutableFloatStateOf(3f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)) // fondo borroso tipo overlay
                        .clickable { showImageDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    selectedProduct?.imagenUrl?.let { urlOrBase64 ->
                        ImagenDesdePosibleBase64OUrl(
                            urlOrBase64,
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            scale = if (scale > 1f) 1f else 3f
                                            offset = Offset.Zero
                                        }
                                    )
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 4f)
                                        if (scale > 1f) {
                                            val maxX = (size.width * (scale - 1)) / 2
                                            val maxY = (size.height * (scale - 1)) / 2
                                            offset = Offset(
                                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                            )
                                        } else {
                                            offset = Offset.Zero
                                        }
                                    }
                                }
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                        )
                    }
                }
            }
        }

    }
}

// 🔹 Convierte Bitmap a formato WebP comprimido
fun bitmapToWebPBytes(bitmap: Bitmap, quality: Int = 80): ByteArray {
    val stream = ByteArrayOutputStream()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11 (API 30) o superior: usa WEBP_LOSSY / WEBP_LOSSLESS
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, stream)
    } else {
        // Android 10 y anteriores: usa WEBP (equivalente a LOSSY)
        @Suppress("DEPRECATION")
        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream)
    }

    return stream.toByteArray()
}

// PERMITE HACER ZOOM A LA IMAGEN
@Composable
fun ImagenDesdePosibleBase64OUrl(data: String?, modifier: Modifier = Modifier) {
    if (data.isNullOrEmpty()) return

    if (data.startsWith("http://") || data.startsWith("https://")) {
        AsyncImage(
            model = data,
            contentDescription = null,
            modifier = modifier
                .size(150.dp)
                .padding(bottom = 12.dp)
        )
    } else {
        // hacemos el try/catch fuera de la parte Composable
        val bitmap: Bitmap? = try {
            val imageBytes = Base64.decode(data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("ImagenDecode", "Error decodificando Base64: ${e.message}")
            null
        }

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = modifier
                    .size(150.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

//OPCIONES DE LOCALES
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalOption(
    listLocal: List<Local>,
    local: String,
    onLocalChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 🔽 Cajón desplegable de tallas según el tipo seleccionado
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = local,
                onValueChange = {},
                readOnly = true,
                label = { Text("Local") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
            ) {
                listLocal.forEach { opcion ->
                    Log.d("datos", "es:$opcion")
                    DropdownMenuItem(
                        text = { Text(opcion.nombre) },
                        onClick = {
                            onLocalChange(opcion.nombre)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

//OPCIONES DE TALLAS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TallaDropdown(
    talla: String,
    onTallaChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Detectamos automáticamente el tipo según la talla actual

    val tallasLetras = listOf("S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL", "6XL")
    val tallasNumeros = listOf("26", "28", "30", "32", "34", "36", "38", "40", "42")

    var tipo by rememberSaveable(talla) {
        mutableStateOf(
            when (talla) {
                in tallasLetras -> "Letras"
                in tallasNumeros -> "Numeros"
                else -> "" // por si viene vacío o raro
            }
        )
    }

//    val opciones = if( tipo=="Letras") tallasLetras else tallasNumeros
    val opciones = listOf("Letras", "Numeros")
    val valores = if (tipo == "Letras") tallasLetras else tallasNumeros
    var showError by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(

            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start
        ) {
            opciones.forEach { opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { tipo = opcion } // hace clic en toda la fila
                ) {
                    RadioButton(
                        selected = tipo == opcion,
                        onClick = { tipo = opcion }
                    )
                    Text(
                        text = opcion,
                        modifier = Modifier.padding(start = 1.dp)
                    )
                }
            }
        }
        Column {
            // 🔽 Cajón desplegable de tallas según el tipo seleccionado
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    if (tipo.isNotEmpty()) {
                        expanded = !expanded
                        showError = false
                    } else {
                        showError = true
                    }
                }
            ) {
                OutlinedTextField(
                    value = talla,
                    onValueChange = {},
                    readOnly = true,
                    enabled = tipo.isNotEmpty(),
                    label = { Text("Talla") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    isError = showError, // 🔴 marca error en el borde si aplica
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .width(120.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
                ) {
                    valores.forEach { opcion ->
                        Log.d("datos", "es:$opcion")
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                onTallaChange(opcion)
                                expanded = false
                            }
                        )
                    }
                }
                // ⚠️ Mensaje de error debajo del campo
                if (showError) {
                    Text(
                        text = "Selecciona si es letras o N°",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                }
            }
        }

    }
}

//OPCIONES DE MARCAS DE PRENDAS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandItem(
    brand: String,
    onBrandChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val brandList = listOf(
        "H.BOSS",
        "POLO.R.L",
        "THE.NORTH.FACE",
        "LACOSTE",
        "DOCKER",
        "COLUMBIA",
        "TOMMY",
        "L'GANTS",
        "LA.MARTINA",
        "ARMANI",
        "NIKE",
        "ADIDAS",
        "MR.GIORGIO",
        "FLSZ",
        "FASHION",
        "PEPUÑO",
        "DIANA RK.",
        "MAXFLER"

    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = brand,
                onValueChange = {},
                readOnly = true,
                label = { Text("Marca") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
            )
            Box {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
                ) {
                    brandList.forEach { opcion ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    opcion,
                                    fontWeight = if (opcion == brand) FontWeight.Bold else FontWeight.Normal,
                                    color = if (opcion == brand) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                            },
                            onClick = {
                                onBrandChange(opcion)
                                expanded = false
                            })
                    }
                }
            }
        }
    }
}

//TIPO DE MANGAS(CORTA O LARGA)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeSleeve(
    sleeve: String,
    onSleeveChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sleeveList = listOf("M. Corta", "M. Larga")
    ExposedDropdownMenuBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = sleeve,
            onValueChange = {},
            readOnly = true,
            label = { Text("Manga") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
        ) {
            sleeveList.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcion,
                            fontWeight = if (opcion == sleeve) FontWeight.Bold else FontWeight.Normal,
                            color = if (opcion == sleeve) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    onClick = {
                        onSleeveChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

//OPCIONES DE TELA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialItem(
    material: String,
    onMaterialChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val materialList = listOf(
        "Drill",
        "Satinada",
        "Oxford",
        "Jean",
        "Mezclilla",
        "Seda",
        "Pique",
        "Pima",
        "Chalis",
        "Lino",
        "Hilo",
        "Dralon.Bayer",
        "Acolchado",
        "Pluma",
        "Tazlan",
        "Cordelina",
        "Gamuza",
        "Cuero Guante",
        "Cuero",
        "Algodón",
        "Nylon",
        "Poliester"
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = material,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text("Tela") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
        ) {
            materialList.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcion,
                            fontWeight = if (opcion == material) FontWeight.Bold else FontWeight.Normal,
                            color = if (opcion == material) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    onClick = {
                        onMaterialChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

//OPCIONES DE ESTILOS
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesingItem(
    desing: String,
    onDesingChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val desingList = listOf("C.Entero", "Bicolor", "Floreada", "Rayada", "Miniaturas")
    ExposedDropdownMenuBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = desing,
            onValueChange = {},
            readOnly = true,
            label = { Text("Estilo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
        ) {
            desingList.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcion,
                            fontWeight = if (opcion == desing) FontWeight.Bold else FontWeight.Normal,
                            color = if (opcion == desing) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    onClick = {
                        onDesingChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

//TIPO DE CORTE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CutItem(
    corte: String,
    onCorteChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val styleList = listOf("Regular", "Clasico", "Slim Fit")

    ExposedDropdownMenuBox(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = corte,
            onValueChange = {},
            readOnly = true,
            label = { Text("Corte") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
        ) {
            styleList.forEach { opcion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcion,
                            fontWeight = if (opcion == corte) FontWeight.Bold else FontWeight.Normal,
                            color = if (opcion == corte) MaterialTheme.colorScheme.primary else Color.Unspecified
                        )
                    },
                    onClick = {
                        onCorteChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}

//OPCIONES DE TIPO DE PRENDA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClothingItem(
    type: String,
    onTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val typeList = listOf(
        "Camisa",
        "Camisa dama",
        "Casaca",
        "Casaca Bomber",
        "Casaca Cortaviento",
        "Casaca acolchada",
        "Casaca polar",
        "Casaca plush",
        "Casaca Cortaviento",
        "Blusas",
        "Blusones",
        "Polar",
        "Polo",
        "Polo Camisero",
        "Chaleco",
        "Chompas",
        "Blaizer",
        "Pantalon",
        "Short",
        "Gorro",
        "Llavero",
        "Billetera",
        "Correa"
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = type,
            onValueChange = {},
            singleLine = true,
            readOnly = true,
            label = { Text("Tipo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp) // altura máxima, ajusta según el alto de 5 items
        ) {
            typeList.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion,
                        fontWeight = if (opcion == type) FontWeight.Bold else FontWeight.Normal,
                        color = if (opcion == type) MaterialTheme.colorScheme.primary else Color.Unspecified) },
                    onClick = {
                        onTypeChange(opcion)
                        expanded = false
                    }
                )
            }
        }

    }
}

//ANIMACION DE CARGA
@Composable
fun LoadingDialog(
    message: String,
    onDismiss: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = { onDismiss?.invoke() }) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    message,
                    textAlign = TextAlign.Center,              // 👈 centra el texto
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
