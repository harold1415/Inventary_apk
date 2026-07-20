package com.example.myinventarioapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myinventarioapp.ui.theme.AjustarBarraEstado
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.theme.StockLowColor
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.Color


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsVenta(
    onToDetailVenta: (String) -> Unit,
    ventaViewModel: VentaViewModel,
    onSearch: () -> Unit
) {
    // TODO: ViewModel — productoSeleccionado debería observarse como StateFlow
    //PRODUCTO SELECCIONADO
    val productoSeleccionado = ventaViewModel.oneproduct.value


    // Controla los íconos de la Status Bar
    AjustarBarraEstado(darkIcons = false)

    // ── Estados del formulario — igual que antes, sin cambios ──────────────
    var nombreProduct by remember { mutableStateOf("") }
    var precioProduct by remember { mutableStateOf("") }
    var cantPro by remember { mutableStateOf("1") }
    var total by remember { mutableDoubleStateOf(0.0) }
    var descuento by remember { mutableDoubleStateOf(0.0) }
    var ganancia by remember { mutableDoubleStateOf(0.0) }
    var cantidaderror by remember { mutableStateOf(false) }
    var mostrarGanancia by remember { mutableStateOf(false) }


    var selectedDiscount by remember { mutableStateOf<String?>(null) }
    var selectedGeneralDiscount by remember { mutableStateOf("") }
    var selectedUnitDiscount by remember { mutableStateOf("") }

    // Rellena nombre y precio cuando se selecciona un producto
    LaunchedEffect(productoSeleccionado) {
        productoSeleccionado?.let {
            nombreProduct = "${it.nombre} ${it.talla}"
            precioProduct = it.precio.toString()
        }
    }

    // ── Cálculo del total — lógica intacta ────────────────────────────────
    val cantidadNum = cantPro.toLongOrNull() ?: 0L
    val descuentoUnit = selectedUnitDiscount.toDoubleOrNull() ?: 0.0
    val descuentoGeneral = selectedGeneralDiscount.toDoubleOrNull() ?: 0.0

    if (productoSeleccionado != null) {
        val (desc, tot, gan) = calcularTotal(
            productoSeleccionado.precio,
            cantidadNum,
            selectedDiscount,
            descuentoUnit,
            descuentoGeneral,
            productoSeleccionado.costo
        )
        descuento = desc
        total = tot
        ganancia = gan
        Log.d("ganar", ":$ganancia")
    }

    Scaffold(
        containerColor = BrandWarmBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📄 Detalle del producto",
                        color = BrandWarmWhite
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack),
                actions = {
                    // Botón para ir a buscar otro producto
                    IconButton(onClick = onSearch) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar producto",
                            tint = BrandWarmWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Card del producto seleccionado ────────────────────────────
            if (productoSeleccionado != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BrandWoodLight, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = nombreProduct,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlack,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Talla: ${productoSeleccionado.talla}", style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                            Text("Stock: ${productoSeleccionado.stock}", style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                            Text("Sucursal: ${productoSeleccionado.local}", style = MaterialTheme.typography.bodySmall, color = BrandTextSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = BrandWoodLight.copy(alpha = 0.6f))
                        Spacer(Modifier.height(8.dp))
                        // Precio editable por si quieren cambiarlo
                        OutlinedTextField(
                            value = precioProduct,
                            onValueChange = { precioProduct = it },
                            label = { Text("Precio unitario (S/)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlack,
                                unfocusedBorderColor = BrandWoodMedium
                            )
                        )
                    }
                }
            }

            // ── Cantidad con botones +/- ──────────────────────────────────
            Text(
                "Cantidad",
                style = MaterialTheme.typography.labelMedium,
                color = BrandTextSecondary
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (cantidaderror) StockLowColor else BrandWoodLight, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón restar
                    val enabled = (cantPro.toLongOrNull() ?: 1L)>1
                    IconButton(
                        enabled = enabled,
                        onClick = {
                            val actual = cantPro.toLongOrNull() ?: 0L
                            if (actual > 1) {
                                cantPro = (actual - 1).toString()
                                cantidaderror = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Restar",
                            tint = if(enabled) BrandBlack else Color.Gray
                        )
                    }

                    // Campo de cantidad — puedes también escribir directamente
                    OutlinedTextField(
                        value = cantPro,
                        onValueChange = {
                            cantPro = it
                            cantidaderror = it.isEmpty() || it.toLongOrNull() == null || (it.toLongOrNull() ?: 0L)<1
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        isError = cantidaderror,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlack,
                            unfocusedBorderColor = BrandWoodMedium
                        ),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Botón sumar
                    IconButton(
                        onClick = {
                            val actual = cantPro.toLongOrNull() ?: 0L
                            cantPro = (actual + 1).toString()
                            cantidaderror = false
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Sumar", tint = BrandBlack)
                    }
                }
            }
            //MENSAJES QUE SALEN EN EL CONTENEDOR DE LA CANTIDAD
            val cantprenda = cantPro.toLongOrNull()
            if (cantidaderror) {
                Text(
                    text = when {
                        cantPro.isEmpty() ->"El campo no puede estar vacio"
                        cantprenda == null -> "Debe de ingresar un número valido"
                        cantprenda < 1 -> "Debe de tener como minimo una unidad"
                        else -> ""
                    },
                    color = StockLowColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // ── Tipo de descuento como chips ──────────────────────────────
            Text(
                "Tipo de descuento",
                style = MaterialTheme.typography.labelMedium,
                color = BrandTextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chip: Sin descuento
                DiscountChip(
                    label = "Sin descuento",
                    selected = selectedDiscount == null,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedDiscount = null
                        selectedUnitDiscount = ""
                        selectedGeneralDiscount = ""
                    }
                )
                // Chip: Por prenda (unitario)
                DiscountChip(
                    label = "Por prenda",
                    selected = selectedDiscount == "unit",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedDiscount = "unit"
                        selectedGeneralDiscount = ""
                    }
                )
                // Chip: General (sobre el total)
                DiscountChip(
                    label = "General",
                    selected = selectedDiscount == "general",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedDiscount = "general"
                        selectedUnitDiscount = ""
                    }
                )
            }

            // ── Campo de descuento — aparece según el tipo elegido ────────
            if (selectedDiscount == "unit") {
                OutlinedTextField(
                    value = selectedUnitDiscount,
                    onValueChange = { selectedUnitDiscount = it },
                    label = { Text("Descuento por prenda (S/)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlack,
                        unfocusedBorderColor = BrandWoodMedium
                    )
                )
            }
            if (selectedDiscount == "general") {
                OutlinedTextField(
                    value = selectedGeneralDiscount,
                    onValueChange = { selectedGeneralDiscount = it },
                    label = { Text("Descuento general (S/)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlack,
                        unfocusedBorderColor = BrandWoodMedium
                    )
                )
            }
            //-----------------------------------------
            // ── Card de total en negro ──────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandBlack),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandWoodMedium
                        )
                        Spacer(
                            Modifier.height(4.dp)
                        )
                        Text(
                            "S/ ${"%.2f".format(total)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandWarmWhite
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Ganancia",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandWoodMedium
                            )

                            IconButton(
                                onClick = {
                                    mostrarGanancia = !mostrarGanancia
                                }
                            ) {
                                Icon(
                                    imageVector = if (mostrarGanancia)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar ganancia",
                                    tint = BrandWoodMedium,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = if (mostrarGanancia)
                                "S/ ${"%.2f".format(ganancia)}"
                            else
                                "S/ ••••",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandWoodMedium
                        )
                    }
                }
            }

            // ── Botón agregar a la venta ──────────────────────────────────
            // TODO: ViewModel — agregarProducto() ya está en VentaViewModel, está bien
            Button(
                onClick = {
                    val cantidad = cantPro.toLong()
                    if (productoSeleccionado != null && nombreProduct.isNotBlank() && cantPro.isNotBlank()) {
                        ventaViewModel.agregarProducto(
                            productoSeleccionado.codigo,
                            productoSeleccionado.nombre,
                            productoSeleccionado.talla,
                            productoSeleccionado.local,
                            cantidad,
                            descuento,
                            productoSeleccionado.costo,
                            productoSeleccionado.precio,
                            ganancia,
                            total
                        )
                        ventaViewModel.resetProduct()
                        val id = ventaViewModel.ventaActualId ?: "New"
                        onToDetailVenta(id)
                    }
                },
                enabled = productoSeleccionado != null &&
                        nombreProduct.isNotBlank() &&
                        cantPro.isNotBlank() &&
                        !cantidaderror,
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
                    "➕ Agregar a la venta",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Componente chip de descuento ─────────────────────────────────────────────
@Composable
private fun DiscountChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) BrandBlack else BrandWarmWhite,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) BrandBlack else BrandWoodLight
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) BrandWarmWhite else BrandTextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            maxLines = 1
        )
    }
}

// ── calcularTotal — lógica intacta, sin ningún cambio ────────────────────────
fun calcularTotal(
    precio: Double,
    cantidad: Long,
    tipoDescuento: String?,
    descuentoUnit: Double,
    descuentoGeneral: Double,
    costo: Double
): Triple<Double, Double, Double> {
    return when (tipoDescuento) {
        "unit" -> {
            val desc = cantidad * descuentoUnit
            val gana = ((precio - costo) * cantidad) - desc
            val total = cantidad * (precio - descuentoUnit)
            Triple(desc, total, gana)
        }
        "general" -> {
            val gana = ((precio - costo) * cantidad) - descuentoGeneral
            val total = (cantidad * precio) - descuentoGeneral
            Triple(descuentoGeneral, total, gana)
        }
        else -> {
            val gana = (precio - costo) * cantidad
            val total = cantidad * precio
            Triple(0.0, total, gana)
        }
    }
}