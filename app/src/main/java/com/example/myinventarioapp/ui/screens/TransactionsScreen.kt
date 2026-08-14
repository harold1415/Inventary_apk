package com.example.myinventarioapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen() {

    var selectedTransaction by remember {
        mutableStateOf<String?>(null)
    }

    // Estado del producto
    var productDetected by remember {
        mutableStateOf(false)
    }

    var selectedSucursal by remember {
        mutableStateOf<String?>(null)
    }

    var cantidad by remember {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Movimientos",
                        color = BrandWarmWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBlack
                )
            )
        },
        containerColor = BrandWarmBackground
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // -----------------------------
            // TRANSFERENCIA
            // -----------------------------
            TransactionCard(
                title = "Transferencia",
                description = "Mover productos entre locales",
                icon = Icons.Default.CompareArrows
            ) {

                selectedTransaction = "transferencia"

                // Reiniciar datos
                productDetected = false
                selectedSucursal = null
                cantidad = ""
            }


            // -----------------------------
            // DEVOLUCIÓN
            // -----------------------------
            TransactionCard(
                title = "Devolución",
                description = "Registrar productos devueltos",
                icon = Icons.Default.KeyboardReturn
            ) {

                selectedTransaction = "devolucion"

                productDetected = false
                selectedSucursal = null
                cantidad = ""
            }


            // -----------------------------
            // CAMBIO
            // -----------------------------
            TransactionCard(
                title = "Cambio",
                description = "Cambiar un producto por otro",
                icon = Icons.Default.SwapHoriz
            ) {

                selectedTransaction = "cambio"

                productDetected = false
                selectedSucursal = null
                cantidad = ""
            }


            // ==========================================
            // CONTENIDO SEGÚN LA OPCIÓN SELECCIONADA
            // ==========================================

            when (selectedTransaction) {

                "transferencia" -> {

                    TransactionOptions(
                        title = "Transferencia",
                        productDetected = productDetected,
                        selectedSucursal = selectedSucursal,
                        cantidad = cantidad,

                        onScanClick = {
                            // AQUÍ posteriormente irá el escáner real
                            productDetected = true
                        },

                        onSucursalSelected = {
                            selectedSucursal = it
                        },

                        onCantidadChange = {
                            cantidad = it
                        },

                        onAccept = {

                            // Aquí guardarás la transferencia
                            println("Transferencia aceptada")
                            println("Sucursal: $selectedSucursal")
                            println("Cantidad: $cantidad")
                        }
                    )
                }


                "devolucion" -> {

                    TransactionOptions(
                        title = "Devolución",
                        productDetected = productDetected,
                        selectedSucursal = selectedSucursal,
                        cantidad = cantidad,

                        onScanClick = {
                            productDetected = true
                        },

                        onSucursalSelected = {
                            selectedSucursal = it
                        },

                        onCantidadChange = {
                            cantidad = it
                        },

                        onAccept = {

                            println("Devolución aceptada")
                        }
                    )
                }


                "cambio" -> {

                    TransactionOptions(
                        title = "Cambio",
                        productDetected = productDetected,
                        selectedSucursal = selectedSucursal,
                        cantidad = cantidad,

                        onScanClick = {
                            productDetected = true
                        },

                        onSucursalSelected = {
                            selectedSucursal = it
                        },

                        onCantidadChange = {
                            cantidad = it
                        },

                        onAccept = {

                            println("Cambio aceptado")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionOptions(
    title: String,
    productDetected: Boolean,
    selectedSucursal: String?,
    cantidad: String,
    onScanClick: () -> Unit,
    onSucursalSelected: (String) -> Unit,
    onCantidadChange: (String) -> Unit,
    onAccept: () -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    val sucursales = listOf(
        "Sucursal Principal",
        "Sucursal Centro",
        "Sucursal Norte",
        "Sucursal Sur"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Opciones de $title",
            color = BrandBlack,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // =====================================
        // BOTÓN ESCANEAR
        // =====================================

        Button(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth()
        ) {

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Escanear"
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text("Escanear producto")
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // =====================================
        // PRODUCTO DETECTADO
        // =====================================

        if (productDetected) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = BrandWarmWhite
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Producto detectado",
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Producto: Producto de ejemplo",
                        color = BrandBlack
                    )

                    Text(
                        text = "Código: PROD-001",
                        color = Color.Gray
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // =====================================
            // SUCURSAL
            // =====================================

            Text(
                text = "Sucursal",
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedButton(
                    onClick = {
                        expanded = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = selectedSucursal
                            ?: "Seleccionar sucursal"
                    )
                }


                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    sucursales.forEach { sucursal ->

                        DropdownMenuItem(
                            text = {
                                Text(sucursal)
                            },
                            onClick = {

                                onSucursalSelected(sucursal)
                                expanded = false
                            }
                        )
                    }
                }
            }


            Spacer(
                modifier = Modifier.height(16.dp)
            )


            // =====================================
            // CANTIDAD
            // =====================================

            OutlinedTextField(
                value = cantidad,
                onValueChange = { value ->

                    // Solo permitir números
                    if (value.all { it.isDigit() }) {
                        onCantidadChange(value)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Cantidad")
                },
                singleLine = true
            )


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // =====================================
            // ACEPTAR
            // =====================================

            Button(
                onClick = onAccept,

                enabled = selectedSucursal != null &&
                        cantidad.isNotEmpty() &&
                        cantidad.toIntOrNull()?.let {
                            it > 0
                        } == true,

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Aceptar")
            }
        }
    }
}


// DISEÑO DEL CARD
@Composable
fun TransactionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = BrandWarmWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandBlack,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color.Gray
                )
            }
        }
    }
}