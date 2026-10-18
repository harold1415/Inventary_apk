package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Locale
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.StockLowColor
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel

private data class ProductoRapido(
    val id: String,
    val nombre: String,
    val precio: Double,
    val cantidad: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VentaRapidaScreen(
    onVentaScreen: ()->Unit,
    ventaViewModel: VentaViewModel,
    ventaId: String = "New",
    navController: NavController
//    onGuardarVenta: (List<ProductoRapido>, Double) -> Unit
) {
    var productos by remember { mutableStateOf(listOf<ProductoRapido>()) }
    var descuentoTexto by remember { mutableStateOf("0") }
    var scannerVisible by remember { mutableStateOf(false) }

    val subtotal = productos.sumOf { it.precio * it.cantidad }
    val descuento = descuentoTexto.toDoubleOrNull()
        ?.coerceIn(0.0, subtotal) ?: 0.0
    val total = (subtotal - descuento).coerceAtLeast(0.0)

    // Agrupa por ID: un producto, una fila.
    fun agregarProducto(producto: ProductoRapido) {
        val existente = productos.any { it.id == producto.id }

        productos = if (existente) {
            productos.map {
                if (it.id == producto.id) {
                    it.copy(cantidad = it.cantidad + 1)
                } else it
            }
        } else {
            productos + producto.copy(cantidad = 1)
        }
    }

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
    ) {innerPadding  ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BrandWarmWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    IconButton(onClick = onVentaScreen) {
//                        Icon(
//                            Icons.AutoMirrored.Outlined.ArrowBack,
//                            contentDescription = "Regresar"
//                        )
//                    }
//
//                    Column {
//                        Text(
//                            "Venta rápida",
//                            style = MaterialTheme.typography.headlineSmall,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Text(
//                            "${productos.sumOf { it.cantidad }} unidades agregadas",
//                            color = Color.Gray,
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                }
                Text(
                    "${productos.sumOf { it.cantidad }} unidades agregadas",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { scannerVisible = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlack
                    )
                ) {
                    Icon(
                        Icons.Outlined.QrCodeScanner,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Escanear producto")
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Productos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                if (productos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Todavía no hay productos", color = Color.Gray)
                            Text(
                                "Pulsa Escanear producto para comenzar",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(productos, key = { it.id }) { producto ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                border = BorderStroke(
                                    1.dp, Color.LightGray.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            producto.nombre,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            "${moneda(producto.precio)} × ${producto.cantidad}",
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text(
                                        moneda(producto.precio * producto.cantidad),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = descuentoTexto,
                    onValueChange = {
                        descuentoTexto = it.filter { c ->
                            c.isDigit() || c == '.'
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descuento general (S/)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilaImporte("Subtotal", moneda(subtotal))
                        FilaImporte("Descuento", "- ${moneda(descuento)}")
                        HorizontalDivider()
                        FilaImporte(
                            "Total",
                            moneda(total),
                            destacado = true
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
//                    if (productos.isNotEmpty()) {
//                        onGuardarVenta(productos, descuento)
//                    }
                    },
                    enabled = productos.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlack
                    )
                ) {
                    Text(
                        "Guardar venta · ${moneda(total)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // La capa se dibuja encima de la pantalla,
            // sin navegar a otra pantalla.
            if (scannerVisible) {
//            ScannerOverlay(
//                onCerrar = { scannerVisible = false },
//                onConfirmar = { scannerVisible = false },
//                onProductoDetectado = { producto ->
//                    agregarProducto(producto)
//                }
//            )
            }
        }
    }
}

@Composable
private fun FilaImporte(
    titulo: String,
    importe: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            titulo,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            importe,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
            color = if (destacado) StockLowColor else Color.Unspecified
        )
    }
}

private fun moneda(valor: Double): String =
    "S/ " + String.format(Locale.US, "%.2f", valor)
