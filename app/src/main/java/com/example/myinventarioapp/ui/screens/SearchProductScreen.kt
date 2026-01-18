package com.example.myinventarioapp.ui.screens


import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.isBlank

data class Products(
    val id: String = "", //← ID del documento en Firestore
    val nombre: String = "",
    val talla: String = "",
    val stock: Long = 0,
    val precio: Double =0.0,
    val codigo: String = "",
    val precioXMayor: Double =0.0,
    val costo: Double =0.0,
    val local: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchProductScreen(navController: NavHostController, codigoEscaneado: String = "", ventaViewModel: VentaViewModel,onToProductsVenta: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var listProduct by remember { mutableStateOf(listOf<Products>()) }
    var selectedProduct by remember { mutableStateOf<Products?>(null) } // 👈 producto seleccionado


    // Leer datos desde Firestore
    LaunchedEffect(Unit) {
        db.collection("productos").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                listProduct = it.documents.mapNotNull { doc ->
                    doc.toObject(Products::class.java)?.copy(id = doc.id)
                }
            }
        }
    }
    // Permiso de la camara para escanear qr
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                navController.navigate("scannerSearch")
            } else {
                Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    )
    var query by remember { mutableStateOf(codigoEscaneado) }
    val productosFiltrados = listProduct.filter {
        it.nombre.contains(query, ignoreCase = true) ||
                it.codigo.contains(query, ignoreCase = true)
    }
//    val productosFiltrados = listProduct.filter { producto ->
//        val coincideTexto = query.isBlank() ||
//                producto.nombre.contains(query, ignoreCase = true) ||
//                producto.codigo.contains(query, ignoreCase = true) ||
//                producto.modeloCod.contains(query, ignoreCase = true)
//
//        val coincideLocal = selectedLocal.isBlank() ||
//                producto.local.equals(selectedLocal, ignoreCase = true)
////        val coincideTalla = talla.isBlank() || producto.talla.equals(talla, ignoreCase = true)
////
////        val coincideColor = color.isBlank() || producto.color.contains(color, ignoreCase = true)
//
//        coincideTexto && coincideLocal &&
//
//    }

    //INTERFAZ
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccion de Productos") },
                actions = {
                    // ✅ Botón Guardar
                    IconButton(
                        onClick = {
                            selectedProduct?.let { producto ->
                                // Aquí lo mandas al ViewModel de venta
                                ventaViewModel.chooseProduct(
                                    producto.id,
                                    producto.nombre,
                                    producto.local,
                                    producto.talla,
                                    producto.stock,
                                    producto.precio,
                                    producto.codigo,
                                    producto.precioXMayor,
                                    producto.costo,
                                )
                                Log.d("productoSelect","igual:$producto")
//                                navController.popBackStack() // <- regresa a ProductsVenta
                                onToProductsVenta() //<- regresa a ProductsVenta

                            }
                        },
                        enabled = selectedProduct != null // solo si hay selección
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar selección", modifier = Modifier.size(40.dp))
                    }
                }

            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar por nombre o código") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear QR"
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(productosFiltrados) { producto ->
                    val isSelected = selectedProduct?.codigo == producto.codigo
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .then(if (isSelected) Modifier else Modifier)
                            .clickable { selectedProduct = producto },
                        elevation = CardDefaults.cardElevation(if (isSelected) 10.dp else 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 🧱 Izquierda: Info del producto + QR
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = producto.nombre,
                                    fontSize = 20.sp,               // más grande
                                    fontWeight = FontWeight.Bold,   // en negrita
                                    color = MaterialTheme.colorScheme.primary
                                ) // color llamativo
                                Text("Talla: ${producto.talla}")
                                Text("Stock: ${producto.stock}")
                                Text("SKU: ${producto.codigo}")
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

    }
}