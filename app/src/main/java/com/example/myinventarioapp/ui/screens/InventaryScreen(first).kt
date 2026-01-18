package com.example.myinventarioapp.ui.screens
//
//import android.widget.Toast
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.google.firebase.firestore.FirebaseFirestore
//import androidx.compose.material.icons.filled.Edit
//import android.graphics.Bitmap
//import com.google.zxing.BarcodeFormat
//import com.google.zxing.qrcode.QRCodeWriter
//import android.content.ContentValues
//import android.content.Context
//import android.os.Environment
//import android.provider.MediaStore
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import java.io.OutputStream
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.foundation.Image
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.material.icons.filled.QrCodeScanner
//import androidx.navigation.NavHostController
//import android.Manifest
//import androidx.compose.material.icons.filled.Visibility
//import com.google.firebase.storage.FirebaseStorage
//import java.util.UUID
//import java.io.ByteArrayOutputStream
//import coil.compose.AsyncImage
//import android.net.Uri
//import android.graphics.ImageDecoder
//import android.os.Build
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.window.Dialog
////USAMOS UN ALIAS PORQUE HAY CONFLICTO CON EL OTRO .Color de abajo
//import android.graphics.Color as AndroidColor
//import androidx.compose.ui.graphics.Color
////CREA EL BIT MAP PARA EL CODIGO QR
//import androidx.core.graphics.createBitmap
//import androidx.core.graphics.set
//
//
//
//
//data class Producto(
//    val id: String = "", // ← ID del documento en Firestore
//    val codigo: String = "",
//    val nombre: String = "",
//    val talla: String = "",
//    val stock: Int = 0,
//    val modeloCod: String ="",
//    val corte: String="",
//    val local: String="",
//    val costo: Int=0,
//    val precio: Int =0,
//    val imagenUrl: String = "" // 🔥 Nuevo campo para la imagen
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun InventarioScreen(navController: NavHostController, codigoEscaneado: String = "") {
//    val context = LocalContext.current
//    val db = FirebaseFirestore.getInstance()
//    var productos by remember { mutableStateOf(listOf<Producto>()) }
//    //DIALOGS
//    var showDialog by remember { mutableStateOf(false) }
//    var showEditDialog by remember { mutableStateOf(false) }
//    var showVerDialog by remember { mutableStateOf(false)}
//
//    var selectedProduct by remember { mutableStateOf<Producto?>(null) }
//    //para guardar la IMG
//    var miBitmapSeleccionado by remember { mutableStateOf<Bitmap?>(null) }
//    //is
//    var isUploading by remember { mutableStateOf(false)}
//
//
//    // Estados para el formulario
//    var nombre by remember { mutableStateOf("") }
//    var talla by remember { mutableStateOf("") }
//    var stock by remember { mutableStateOf("") }
//    var corte by remember { mutableStateOf("") }
//    var local by remember { mutableStateOf("") }
//    var costo by remember { mutableStateOf("") }
//    var precio by remember { mutableStateOf("") }
//
//    // Permiso de la camara para escanear qr
//    val cameraPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//        onResult = { granted ->
//            if (granted) {
//                navController.navigate("scanner")
//            } else {
//                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
//            }
//        }
//    )
//
//    // Leer datos desde Firestore
//    LaunchedEffect(Unit) {
//        db.collection("productos").addSnapshotListener { snapshot, _ ->
//            snapshot?.let {
//                productos = it.documents.mapNotNull { doc ->
//                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
//                }
//            }
//        }
//    }
//    // LACUNCHER QUE PERMITE
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                // API 28+: ImageDecoder
//                val source = ImageDecoder.createSource(context.contentResolver, it)
//                ImageDecoder.decodeBitmap(source)
//            } else {
//                // API <28: MediaStore
//                @Suppress("DEPRECATION")
//                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
//            }
//
//            miBitmapSeleccionado = bitmap
//        }
//    }
//
//
//    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text("Inventario de Productos") })
//        },
//        floatingActionButton = {
//            FloatingActionButton(onClick = { showDialog = true }) {
//                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
//            }
//        }
//    ) { padding ->
//        var query by remember { mutableStateOf(codigoEscaneado) }
//        val productosFiltrados = productos.filter {
//            it.nombre.contains(query, ignoreCase = true) ||
//                    it.codigo.contains(query, ignoreCase = true)
//        }
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp)
//        ) {
//            OutlinedTextField(
//                value = query,
//                onValueChange = { query = it },
//                label = { Text("Buscar por nombre o código") },
//                modifier = Modifier.fillMaxWidth(),
//                trailingIcon = {
//                    IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
//                        Icon(
//                            imageVector = Icons.Default.QrCodeScanner,
//                            contentDescription = "Escanear QR"
//                        )
//                    }
//                }
//            )
//            Spacer(modifier = Modifier.height(16.dp))
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(productosFiltrados) { producto ->
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        elevation = CardDefaults.cardElevation(4.dp)
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            // 🧱 Izquierda: Info del producto + QR
//                            Column(
//                                modifier = Modifier.weight(1f)
//                            ) {
//                                Text("Nombre: ${producto.nombre}")
//                                Text("Talla: ${producto.talla}")
//                                Text("Stock: ${producto.stock}")
//                                Text("Código: ${producto.codigo}")
//                                Spacer(modifier = Modifier.height(8.dp))
//
//                                //BOTOM PARA VER QR
//                                var mostrarQR by remember { mutableStateOf(false) }
//
//                                Button(onClick = { mostrarQR = true }) {
//                                    Text("Ver QR")
//                                }
//                                if (mostrarQR) {
//                                    val qrBitmap = remember(producto.codigo) {
//                                        generateQRBitmap(producto.codigo)
//                                    }
//                                    //VENTANA AL ABRIR EL -QR
//                                    AlertDialog(
//                                        onDismissRequest = { mostrarQR = false },
//                                        title = { Text("Código QR del producto") },
//                                        text = {
//                                            Column(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                horizontalAlignment = Alignment.CenterHorizontally
//                                            ) {
//                                                Image(
//                                                    bitmap = qrBitmap.asImageBitmap(),
//                                                    contentDescription = "QR",
//                                                    modifier = Modifier
//                                                        .size(200.dp)
//                                                        .padding(8.dp)
//                                                )
//
//                                                Spacer(modifier = Modifier.height(16.dp))
//
//                                                Row(
//                                                    modifier = Modifier.fillMaxWidth(),
//                                                    horizontalArrangement = Arrangement.Center
//                                                ) {
//                                                    Button(onClick = {
//                                                        val success = saveBitmapToGallery(
//                                                            context,
//                                                            qrBitmap,
//                                                            producto.codigo
//                                                        )
//                                                        Toast.makeText(
//                                                            context,
//                                                            if (success) "QR guardado en galería" else "Error al guardar QR",
//                                                            Toast.LENGTH_SHORT
//                                                        ).show()
//                                                    }) {
//                                                        Text("Guardar QR")
//                                                    }
//
//                                                    Spacer(modifier = Modifier.width(16.dp))
//
//                                                    TextButton(onClick = { mostrarQR = false }) {
//                                                        Text("Cerrar")
//                                                    }
//                                                }
//                                            }
//                                        },
//                                        confirmButton = {},
//                                        dismissButton = {}
//                                    )
//                                }
//                            }
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                verticalArrangement = Arrangement.spacedBy(12.dp)
//                            ) {
//                                //BOTON DE VER
//                                IconButton(onClick ={
//                                    showVerDialog = true
//                                    selectedProduct = producto
//                                }) {
//                                    Icon(Icons.Default.Visibility, contentDescription = "Ver",tint = MaterialTheme.colorScheme.primary)
//                                }
//                                //BOTON DE EDITAR
//                                IconButton(onClick = {
//                                    // Mostrar diálogo de edición
//                                    selectedProduct = producto
//                                    showEditDialog = true
//                                }) {
//                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
//                                }
//                                //BOTOM DE ELIMINAR
//                                var eliminarDialog by remember { mutableStateOf(false) }
//                                IconButton(onClick = { eliminarDialog = true }) {
//                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
//                                }
//                                //VENTANA DE ELIMINAR
//                                if (eliminarDialog) {
//                                    AlertDialog(
//                                        onDismissRequest = { eliminarDialog = false },
//                                        title = {
//                                            Text(
//                                                "ELIMINAR PRODUCTO",
//                                                modifier = Modifier.fillMaxWidth(),
//                                                textAlign = TextAlign.Center
//                                            )
//                                        },
//                                        text = {
//                                            Column(
//                                                modifier = Modifier.fillMaxWidth(),
//                                                horizontalAlignment = Alignment.CenterHorizontally
//                                            ) {
//                                                Text("¿Desea eliminar este producto?")
//                                                Spacer(modifier = Modifier.height(16.dp))
//
//                                                Row(
//                                                    modifier = Modifier.fillMaxWidth(),
//                                                    horizontalArrangement = Arrangement.Center
//                                                ) {
//                                                    Button(onClick = {
//                                                        // Eliminar el producto por su código
//                                                        db.collection("productos")
//                                                            .whereEqualTo("codigo", producto.codigo)
//                                                            .get()
//                                                            .addOnSuccessListener { snapshot ->
//                                                                for (doc in snapshot.documents) {
//                                                                    db.collection("productos")
//                                                                        .document(doc.id).delete()
//                                                                }
//                                                                Toast.makeText(
//                                                                    context,
//                                                                    "Producto eliminado",
//                                                                    Toast.LENGTH_SHORT
//                                                                ).show()
//                                                            }
//                                                    }) {
//                                                        Text("Eliminar")
//                                                    }
//
//                                                    Spacer(modifier = Modifier.width(16.dp))
//
//                                                    TextButton(onClick = {
//                                                        eliminarDialog = false
//                                                    }) {
//                                                        Text("Cerrar")
//                                                    }
//                                                }
//                                            }
//                                        },
//                                        confirmButton = {},
//                                        dismissButton = {}
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        //ALGORITMO QUE MUESTRA EL DETALLE DEL PRODUCTO
//        if(showVerDialog){
//            AlertDialog(
//                onDismissRequest = {showVerDialog = false},
//                title = {Text("Informacion del Producto")},
//                text = {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        // Mostrar imagen si existe
//                        if (!selectedProduct?.imagenUrl.isNullOrEmpty()) {
//                            AsyncImage(
//                                model = selectedProduct!!.imagenUrl,
//                                contentDescription = "Imagen de ${selectedProduct!!.nombre}",
//                                modifier = Modifier
//                                    .size(150.dp)
//                                    .padding(bottom = 12.dp)
//                            )
//                        }
//                        Text("Producto: ${selectedProduct!!.nombre}")
//                        Text("Talla: ${selectedProduct!!.talla}")
//                        Text("Stock: ${selectedProduct!!.stock}")
//                        Text("Modelo: ${selectedProduct!!.modeloCod}")
//                        Text("Corte: ${selectedProduct!!.corte}")
//                        Text("Local: ${selectedProduct!!.local}")
//                        Text("Costo: S/${selectedProduct!!.costo}.00")
//                        Text("Precio: S/${selectedProduct!!.precio}.00")
//                    }
//                },
//                confirmButton = {
//                    TextButton(onClick = { showVerDialog = false }) {
//                        Text("Cerrar")
//                    }
//                }
//            )
//        }
//
//        // ALGORITMO QUE INGRESA NUEVO PRODUCTO
//        if (showDialog) {
//            AlertDialog(
//                onDismissRequest = { showDialog = false },
//                confirmButton = {
//                    Button(onClick = {
//                        val stockInt = stock.toIntOrNull() ?: 0
//                        val costoInt = costo.toIntOrNull() ?: 0
//                        val precioInt = precio.toIntOrNull() ?: 0
//                        val codigo = "PRD" + System.currentTimeMillis().toString().takeLast(4) + (10..99).random() // Genera código único
//                        val modeloCod = "MOD"+ UUID.randomUUID().toString().take(4)
//
//                        // Si ya tienes un Bitmap cargado desde galería o cámara:
//                        if (miBitmapSeleccionado != null) {
//                            isUploading = true // 🔹 Mostrar animacion del proceso
//                            uploadImageToFirebase(miBitmapSeleccionado!!, "productos/${UUID.randomUUID()}.webp") { url ->
//                                if (url != null) {
//                                    val nuevoProducto = hashMapOf(
//                                        "codigo" to codigo,
//                                        "nombre" to nombre,
//                                        "talla" to talla,
//                                        "stock" to stockInt,
//                                        "modeloCod" to modeloCod,
//                                        "corte" to corte,
//                                        "local" to local,
//                                        "costo" to costoInt,
//                                        "precio" to precioInt,
//                                        "imagenUrl" to url // 🔥 Guarda la URL aquí
//                                    )
//
//                                    db.collection("productos").add(nuevoProducto)
//                                        .addOnSuccessListener {
//                                            Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show()
//                                            nombre = ""
//                                            talla = ""
//                                            stock = ""
//                                            corte = ""
//                                            local = ""
//                                            costo = ""
//                                            precio = ""
//                                            showDialog = false
//                                            miBitmapSeleccionado = null
//                                        }
//                                        .addOnFailureListener {
//                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
//                                        }
//                                } else {
//                                    Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
//                                }
//                            }
//                        } else {
//                            // Si no hay imagen, guardar normal
//                            val nuevoProducto = hashMapOf(
//                                "codigo" to codigo,
//                                "nombre" to nombre,
//                                "talla" to talla,
//                                "stock" to stockInt,
//                                "modeloCod" to modeloCod,
//                                "corte" to corte,
//                                "local" to local,
//                                "costo" to costoInt,
//                                "precio" to precioInt
//                            )
//                            db.collection("productos").add(nuevoProducto)
//                                .addOnSuccessListener {
//                                    Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show()
//                                    nombre = ""
//                                    talla = ""
//                                    stock = ""
//                                    corte = ""
//                                    local = ""
//                                    costo = ""
//                                    precio = ""
//                                    showDialog = false
//                                }
//                                .addOnFailureListener {
//                                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
//                                }
//                        }
//                        isUploading = false // 🔹 Cerramos animacion del proceso sin importar si se pudo o no guardar
//                    }) {
//                        Text("Guardar")
//                    }
//                    // 🔹 Mostrar diálogo con CircularProgressIndicator
//                    if (isUploading) {
//                        Dialog(onDismissRequest = { }) {
//                            Box(
//                                modifier = Modifier
//                                    .size(120.dp)
//                                    .background(Color.White, shape = RoundedCornerShape(16.dp)),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                CircularProgressIndicator()
//                            }
//                        }
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = { showDialog = false }) {
//                        Text("Cancelar")
//                    }
//                },
//                title = { Text("Nuevo producto") },
//                text = {
//                    Column {
//                        OutlinedTextField(
//                            value = nombre,
//                            onValueChange = { nombre = it },
//                            label = { Text("Nombre") })
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = talla,
//                            onValueChange = { talla = it },
//                            label = { Text("Talla") })
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = stock,
//                            onValueChange = { stock = it },
//                            label = { Text("Stock") })
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = corte,
//                            onValueChange = { corte = it },
//                            label = { Text("Corte") })
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = local,
//                            onValueChange = { local = it },
//                            label = { Text("Local") })
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = costo,
//                            onValueChange = { costo = it },
//                            label = { Text("Costo") })
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = precio,
//                            onValueChange = { precio = it },
//                            label = { Text("Precio") })
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Button(onClick = { launcher.launch("image/*") }) {
//                            Text("Seleccionar imagen")
//                        }
//                    }
//                }
//            )
//        }
//        //ALGORITMO QUE EDITA EL PRODUCTO
//        if (showEditDialog && selectedProduct != null) {
//            var editNombre by remember { mutableStateOf(selectedProduct!!.nombre) }
//            var editTalla by remember { mutableStateOf(selectedProduct!!.talla) }
//            var editStock by remember { mutableStateOf(selectedProduct!!.stock.toString()) }
//            var editCorte by remember { mutableStateOf(selectedProduct!!.corte)}
//            var editLocal by remember { mutableStateOf(selectedProduct!!.local)}
//            var editCosto by remember { mutableStateOf(selectedProduct!!.costo.toString())}
//            var editPrecio by remember { mutableStateOf(selectedProduct!!.precio.toString())}
//
//            AlertDialog(
//                onDismissRequest = { showEditDialog = false },
//                confirmButton = {
//                    Button(onClick = {
//                        val stockInt = editStock.toIntOrNull() ?: 0
//                        val costoInt = editCosto.toIntOrNull() ?:0
//                        val precioInt = editPrecio.toIntOrNull()?:0
//
//                        val actualizacion = mapOf(
//                            "nombre" to editNombre,
//                            "talla" to editTalla,
//                            "stock" to stockInt,
//                            "corte" to editCorte,
//                            "local" to editLocal,
//                            "costo" to costoInt,
//                            "precio" to precioInt
//                         )
//
//                        db.collection("productos").document(selectedProduct!!.id)
//                            .update(actualizacion)
//                            .addOnSuccessListener {
//                                Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT)
//                                    .show()
//                                showEditDialog = false
//                            }
//                            .addOnFailureListener {
//                                Toast.makeText(
//                                    context,
//                                    "Error al actualizar: ${it.message}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                    }) {
//                        Text("Guardar")
//                    }
//                },
//                dismissButton = {
//                    TextButton(onClick = { showEditDialog = false }) {
//                        Text("Cancelar")
//                    }
//                },
//                title = { Text("Editar producto") },
//                text = {
//                    Column {
//                        OutlinedTextField(
//                            value = editNombre,
//                            onValueChange = { editNombre = it },
//                            label = { Text("Nombre") }
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = editTalla,
//                            onValueChange = { editTalla = it },
//                            label = { Text("Talla") }
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = editStock,
//                            onValueChange = { editStock = it },
//                            label = { Text("Stock") }
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = editCorte,
//                            onValueChange = { editCorte = it },
//                            label = { Text("Corte") }
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = editLocal,
//                            onValueChange = { editLocal = it },
//                            label = { Text("Local") }
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = editCosto,
//                            onValueChange = { editCosto = it },
//                            label = { Text("Costo") }
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = editPrecio,
//                            onValueChange = { editPrecio = it },
//                            label = { Text("Precio") }
//                        )
//                    }
//                }
//            )
//        }
//    }
//}
//
////ALGORITMO QUE GENERA EL QR
//fun generateQRBitmap(content: String, size: Int = 512): Bitmap {
//    val writer = QRCodeWriter()
//    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
//    val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
//
//    for (x in 0 until size) {
//        for (y in 0 until size) {
//            bitmap[x, y] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
//        }
//    }
//    return bitmap
//}
//
//// ALGORITMO QUE GUARDA EL CODIGO QR
//fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): Boolean {
//    val resolver = context.contentResolver
//    val contentValues = ContentValues().apply {
//        put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.png")
//        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
//        put(
//            MediaStore.MediaColumns.RELATIVE_PATH,
//            Environment.DIRECTORY_PICTURES + "/QRInventario"
//        )
//    }
//
//    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        ?: return false
//
//    val stream: OutputStream? = resolver.openOutputStream(uri)
//    stream?.use {
//        return bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
//    }
//
//    return false
//}
//
//
//// 🔹 Convierte Bitmap a formato WebP comprimido
//fun bitmapToWebPBytes(bitmap: Bitmap, quality: Int = 80): ByteArray {
//    val stream = ByteArrayOutputStream()
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        // Android 11 (API 30) o superior: usa WEBP_LOSSY / WEBP_LOSSLESS
//        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, stream)
//    } else {
//        // Android 10 y anteriores: usa WEBP (equivalente a LOSSY)
//        @Suppress("DEPRECATION")
//        bitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream)
//    }
//
//    return stream.toByteArray()
//}
//
//// 🔹 Sube la imagen comprimida a Firebase Storage y devuelve la URL VERSION 1
////fun uploadImageToFirebase(bitmap: Bitmap, path: String, onResult: (String?) -> Unit) {
////    val storageRef = FirebaseStorage.getInstance().reference.child(path)
////
////    val webpBytes = bitmapToWebPBytes(bitmap, quality = 75)
////
////    val uploadTask = storageRef.putBytes(webpBytes)
////    uploadTask.addOnSuccessListener {
////        storageRef.downloadUrl.addOnSuccessListener { uri ->
////            onResult(uri.toString()) // ✅ Devuelve la URL lista para guardar en Firestore
////        }
////    }.addOnFailureListener {
////        onResult(null)
////    }
////}
//// LO MISMO QUE EL DE ARRIBA VERSION 2
//fun uploadImageToFirebase(bitmap: Bitmap, path: String, onResult: (String?) -> Unit) {
//    val storageRef = FirebaseStorage.getInstance().reference.child(path)
//    val webpBytes = bitmapToWebPBytes(bitmap, quality = 75)
//    Log.d("Upload3", "Subiendo a: $path con ${webpBytes.size} bytes")
//    val uploadTask = storageRef.putBytes(webpBytes)
//    uploadTask.addOnSuccessListener {
//        storageRef.downloadUrl
//            .addOnSuccessListener { uri ->
//                onResult(uri.toString())
//            }
//            .addOnFailureListener { e ->
//                // 🔹 Capturamos error si no se pudo obtener la URL
//                Log.e("Upload1", "Error al obtener downloadUrl: ${e.message}")
//                onResult(null)
//            }
//    }.addOnFailureListener { e ->
//        Log.e("Upload2", "Error al subir imagen: ${e.message}")
//        onResult(null)
//    }
//}
//// 🔹 Convierte Bitmap a Base64 (JPEG para pruebas)
//
