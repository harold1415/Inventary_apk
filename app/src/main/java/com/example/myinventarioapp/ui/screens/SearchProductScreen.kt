package com.example.myinventarioapp.ui.screens


import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import com.google.firebase.firestore.FirebaseFirestore

// TODO: ViewModel — data class Products debería vivir en un SearchProductViewModel
// junto con la lógica de búsqueda y el listado de productos
data class Products(
    val id: String = "",
    val nombre: String = "",
    val talla: String = "",
    val stock: Long = 0,
    val precio: Double = 0.0,
    val color: String ="",
    val codigo: String = "",
    val precioXMayor: Double = 0.0,
    val costo: Double = 0.0,
    val local: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchProductScreen(
    navController: NavHostController,
    codigoEscaneado: String = "",
    ventaViewModel: VentaViewModel,
    onToProductsVenta: () -> Unit
) {
    // Controla los íconos de la Status Bar
    AjustarBarraEstado(darkIcons = false)

    val context = LocalContext.current

    // TODO: ViewModel — db debería instanciarse en SearchProductViewModel
    val db = FirebaseFirestore.getInstance()

    // TODO: ViewModel — listProduct debería ser un StateFlow en SearchProductViewModel
    var listProduct by remember { mutableStateOf(listOf<Products>()) }
    var selectedProduct by remember { mutableStateOf<Products?>(null) }

    // Indicador de carga mientras Firestore responde
    var isLoading by remember { mutableStateOf(true) }

    // TODO: ViewModel — esta consulta a Firestore debería estar en SearchProductViewModel
    LaunchedEffect(Unit) {
        db.collection("productos").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                listProduct = it.documents.mapNotNull { doc ->
                    doc.toObject(Products::class.java)?.copy(id = doc.id)
                }
                isLoading = false // 👈 ya llegaron los datos
            }
        }
    }

    // Permiso de cámara para escanear QR
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) navController.navigate("scannerSearch")
            else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    )

    var query by remember { mutableStateOf(codigoEscaneado) }

    // TODO: ViewModel — el filtrado también debería hacerse en SearchProductViewModel
    // como un StateFlow derivado que combine listProduct + query
    val productosFiltrados = listProduct.filter {
        it.nombre.contains(query, ignoreCase = true) ||
                it.codigo.contains(query, ignoreCase = true)
    }

    Scaffold(
        containerColor = BrandWarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🔍 Selección de producto",
                        color = BrandWarmWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack),
                actions = {
                    // Botón confirmar selección — solo activo si hay producto elegido
                    IconButton(
                        onClick = {
                            selectedProduct?.let { producto ->
                                // TODO: ViewModel — chooseProduct() ya está en VentaViewModel, está bien
                                ventaViewModel.chooseProduct(
                                    producto.id,
                                    producto.nombre,
                                    producto.local,
                                    producto.talla,
                                    producto.color,
                                    producto.stock,
                                    producto.precio,
                                    producto.codigo,
                                    producto.precioXMayor,
                                    producto.costo,
                                )
                                Log.d("productoSelect", "igual:$producto")
                                onToProductsVenta()
                            }
                        },
                        enabled = selectedProduct != null
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Confirmar selección",
                            tint = if (selectedProduct != null) BrandWoodMedium else BrandWoodMedium.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Buscador sticky — se pega arriba al hacer scroll
            stickyHeader {
                Surface(
                    color = BrandWarmBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Buscar por nombre o código…") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Buscar",
                                    tint = BrandWoodMedium
                                )
                            },
                            trailingIcon = {
                                Row {
                                    if (query.isNotEmpty()) {
                                        IconButton(onClick = { query = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Borrar", tint = BrandWoodMedium)
                                        }
                                    }
                                    IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR", tint = BrandWoodMedium)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(2.dp, RoundedCornerShape(28.dp)),
                            shape = RoundedCornerShape(28.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlack,
                                unfocusedBorderColor = BrandWoodMedium,
                                focusedContainerColor = BrandWarmWhite,
                                unfocusedContainerColor = BrandWarmWhite,
                                cursorColor = BrandBlack
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        // Aviso de cuántos resultados hay
                        if (!isLoading && query.isNotEmpty()) {
                            Text(
                                "${productosFiltrados.size} resultado${if (productosFiltrados.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandTextSecondary,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }

            // Estado de carga
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandBlack)
                    }
                } else if (productosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (query.isBlank()) "No hay productos disponibles"
                            else "No se encontró \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrandTextSecondary
                        )
                    }
                }
            }

            // Lista de productos
            items(productosFiltrados) { producto ->
                val isSelected = selectedProduct?.codigo == producto.codigo
                val stockBajo = producto.stock <= 3

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BrandBlack else BrandWoodLight,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedProduct = producto },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BrandBlack else BrandWarmWhite
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        // Nombre + precio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = producto.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BrandWarmWhite else BrandBlack,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text = "S/ ${"%.2f".format(producto.precio)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BrandWoodMedium else BrandBlack
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // Datos secundarios
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Talla: ${producto.talla}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) BrandWoodMedium else BrandTextSecondary
                            )
                            Text(
                                "SKU: ${producto.codigo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) BrandWoodMedium else BrandTextSecondary
                            )
                            Text(
                                "Local: ${producto.local}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) BrandWoodMedium else BrandTextSecondary
                            )
                        }

                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(
                            color = if (isSelected) BrandWoodMedium.copy(alpha = 0.3f)
                            else BrandWoodLight.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(6.dp))

                        // Fila inferior: stock + chip seleccionado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Chip de stock
                            Surface(
                                color = if (stockBajo)
                                    StockLowColor.copy(alpha = if (isSelected) 0.3f else 0.12f)
                                else
                                    BrandWoodLight.copy(alpha = if (isSelected) 0.2f else 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Stock: ${producto.stock}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (stockBajo) StockLowColor
                                    else if (isSelected) BrandWoodMedium
                                    else BrandTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Indicador de seleccionado
                            if (isSelected) {
                                Surface(
                                    color = BrandWoodMedium,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "✓ Seleccionado",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandBlack,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}