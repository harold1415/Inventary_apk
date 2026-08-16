package com.example.myinventarioapp.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.myinventarioapp.ui.model.Local
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.viewmodel.TranferViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TranferViewModel = viewModel()
) {

    val locales by viewModel.locales.collectAsState()

    var selectedTransaction by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Movimientos",
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            /*
             * OPCIONES PRINCIPALES
             */

            item {
                TransactionCard(
                    title = "Transferencia",
                    description = "Mover productos entre locales",
                    icon = Icons.Default.CompareArrows,
                    selected = selectedTransaction == "transferencia"
                ) {
                    selectedTransaction = "transferencia"
                }
            }

            item {
                TransactionCard(
                    title = "Devolución",
                    description = "Registrar productos devueltos",
                    icon = Icons.Default.KeyboardReturn,
                    selected = selectedTransaction == "devolucion"
                ) {
                    selectedTransaction = "devolucion"
                }
            }

            item {
                TransactionCard(
                    title = "Cambio",
                    description = "Cambiar un producto por otro",
                    icon = Icons.Default.SwapHoriz,
                    selected = selectedTransaction == "cambio"
                ) {
                    selectedTransaction = "cambio"
                }
            }

            /*
             * CONTENIDO
             */

            item {

                when (selectedTransaction) {

                    "transferencia" -> {
                        TransferenciaOptions(
                            locales = locales
                        )
                    }

                    "devolucion" -> {
                        DevolucionOptions()
                    }

                    "cambio" -> {
                        CambioOptions()
                    }
                }
            }
        }
    }
}


/*
 * ============================================================
 * TRANSFERENCIA
 * ============================================================
 */

@Composable
fun TransferenciaOptions(
    locales: List<Local>
) {

    var productDetected by remember {
        mutableStateOf(false)
    }

    var selectedEmisor by remember {
        mutableStateOf<Local?>(null)
    }

    var selectedReceptor by remember {
        mutableStateOf<Local?>(null)
    }

    var cantidad by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        TransSectionTitle(
            title = "Transferencia"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /*
         * ESCANEAR PRODUCTO
         */

        Button(
            onClick = {
                // Temporalmente simulamos el escaneo
                productDetected = true
            },
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

        /*
         * INFORMACIÓN DEL PRODUCTO
         */

        if (productDetected) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            ProductInfoCard()

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /*
             * STOCK POR LOCAL
             */

            StockByLocalCard(
                locales = locales
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /*
             * EMISOR / RECEPTOR
             */

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                LocalSelector(
                    label = "Emisor",
                    selectedLocal = selectedEmisor,
                    locales = locales,
                    modifier = Modifier.weight(1f),
                    excludedLocal = selectedReceptor,
                    onLocalSelected = {
                        selectedEmisor = it
                    }
                )

                LocalSelector(
                    label = "Receptor",
                    selectedLocal = selectedReceptor,
                    locales = locales,
                    modifier = Modifier.weight(1f),
                    excludedLocal = selectedEmisor,
                    onLocalSelected = {
                        selectedReceptor = it
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /*
             * CANTIDAD + BOTÓN
             */

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { value ->

                        if (value.all { it.isDigit() }) {
                            cantidad = value
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = {
                        Text("Cantidad")
                    },
                    singleLine = true
                )

                Button(
                    onClick = {

                        println("Transferencia")
                        println("Emisor: ${selectedEmisor?.nombre}")
                        println("Receptor: ${selectedReceptor?.nombre}")
                        println("Cantidad: $cantidad")

                    },
                    enabled =
                        selectedEmisor != null &&
                                selectedReceptor != null &&
                                cantidad.toIntOrNull()?.let {
                                    it > 0
                                } == true,
                    modifier = Modifier.weight(1f)
                ) {

                    Text("Actualizar")
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }
}


/*
 * ============================================================
 * PRODUCTO
 * ============================================================
 */

@Composable
fun ProductInfoCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BrandWarmWhite
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = "Producto encontrado",
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                ProductData(
                    label = "Modelo",
                    value = "CAM-001"
                )

                ProductData(
                    label = "Talla",
                    value = "M"
                )

                ProductData(
                    label = "Color",
                    value = "Negro"
                )
            }
        }
    }
}


@Composable
fun ProductData(
    label: String,
    value: String
) {

    Column {

        Text(
            text = label,
            color = Color.Gray
        )

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = BrandBlack
        )
    }
}


/*
 * ============================================================
 * STOCK POR LOCAL
 * ============================================================
 */

@Composable
fun StockByLocalCard(
    locales: List<Local>
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BrandWarmWhite
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Text(
                text = "Stock por sucursal",
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (locales.isEmpty()) {

                Text(
                    text = "No hay sucursales disponibles",
                    color = Color.Gray
                )

            } else {

                locales.forEach { local ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = local.nombre,
                            color = BrandBlack
                        )

                        Text(
                            text = "—",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


/*
 * ============================================================
 * SELECTOR DE LOCAL
 * ============================================================
 */

@Composable
fun LocalSelector(
    label: String,
    selectedLocal: Local?,
    locales: List<Local>,
    modifier: Modifier = Modifier,
    excludedLocal: Local?,
    onLocalSelected: (Local) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
    ) {

        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = selectedLocal?.nombre ?: label,
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            locales
                .filter { it.id != excludedLocal?.id }
                .forEach { local ->

                    DropdownMenuItem(
                        text = {
                            Text(local.nombre)
                        },
                        onClick = {

                            onLocalSelected(local)

                            expanded = false
                        }
                    )
                }
        }
    }
}


/*
 * ============================================================
 * DEVOLUCIÓN
 * ============================================================
 */

@Composable
fun DevolucionOptions() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandWarmWhite
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Opciones de devolución",
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Aquí construiremos posteriormente el formulario de devolución.",
                color = Color.Gray
            )
        }
    }
}


/*
 * ============================================================
 * CAMBIO
 * ============================================================
 */

@Composable
fun CambioOptions() {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandWarmWhite
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Opciones de cambio",
                fontWeight = FontWeight.Bold,
                color = BrandBlack
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Aquí construiremos posteriormente el formulario de cambio.",
                color = Color.Gray
            )
        }
    }
}


/*
 * ============================================================
 * TÍTULO DE SECCIÓN
 * ============================================================
 */

@Composable
fun TransSectionTitle(
    title: String
) {

    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        color = BrandBlack
    )
}


/*
 * ============================================================
 * CARD PRINCIPAL DE MOVIMIENTO
 * ============================================================
 */

@Composable
fun TransactionCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                BrandWarmWhite
            } else {
                BrandWarmWhite
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) {
                7.dp
            } else {
                3.dp
            }
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandBlack,
                modifier = Modifier.size(30.dp)
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlack
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = description,
                    color = Color.Gray
                )
            }
        }
    }
}