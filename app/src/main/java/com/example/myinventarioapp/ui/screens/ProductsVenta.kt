package com.example.myinventarioapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.myinventarioapp.ui.viewmodel.VentaViewModel


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsVenta(
    onToDetailVenta: (String) -> Unit,
    ventaViewModel: VentaViewModel,
    onSearch: () -> Unit
) {
    var nombreProduct by remember { mutableStateOf("") }
    var precioProduct by remember { mutableStateOf("") }
    var cantPro by remember { mutableStateOf("") }
    var total by remember { mutableDoubleStateOf(0.0) } // antes era Int
    var descuento by remember { mutableDoubleStateOf(0.0) }
    var ganancia by remember {mutableDoubleStateOf(0.0)}
    //condicion del error en el campo de cantidad
    var cantidaderror by remember { mutableStateOf(false) }


    // DATOS DEL PRODUCTO QUE ESCOGIMOS
    val productoSeleccionado = ventaViewModel.oneproduct.value// observa la lista
    var selectedDiscount by remember { mutableStateOf<String?>(null) }
    var selectedGeneralDiscount by remember { mutableStateOf("") }
    var selectedUnitDiscount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("\uD83D\uDCC4 Detalle del producto")},
                actions = {
                    // ✅ Botón Guardar
                    IconButton(
                        onClick = onSearch,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Buscar producto", modifier = Modifier.size(40.dp))
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
        ) {
            Spacer(Modifier.height(8.dp))

            LaunchedEffect(productoSeleccionado) {
                productoSeleccionado?.let {
                    nombreProduct = "${it.nombre} ${it.talla}"
                    precioProduct = it.precio.toString()
                }
            }
            // Mostrar campos solo si se seleccionó un producto
            if (productoSeleccionado != null) {
                OutlinedTextField(
                    value = nombreProduct,
                    onValueChange = {},
                    label = { Text("Nombre del producto") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = precioProduct,
                        onValueChange = {
                            precioProduct = it
                        },
                        label = { Text("Precio") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp), // pequeño espacio a la derecha
                        isError = cantidaderror
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantPro,
                        onValueChange = {
                            cantPro = it
                            cantidaderror = it.isEmpty() || it.toIntOrNull() == null
                        },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .weight(1f),
                        isError = cantidaderror
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
//            if(cantidadNum <=1){
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedDiscount == "unit",
                        onCheckedChange = { checked ->
                            selectedDiscount = if (checked) "unit" else null
                        }
                    )
                    Text("Descuento por prenda")
                }
                Spacer(Modifier.height(8.dp))
//            }else{
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedDiscount == "general",
                        onCheckedChange = { checked ->
                            selectedDiscount = if (checked) "general" else null
                        }
                    )
                    Text("Descuento General")
                }
//            }

            if (selectedDiscount == "unit") {
                OutlinedTextField(
                    value = selectedUnitDiscount,
                    onValueChange = {
                        selectedUnitDiscount = it
                    },
                    label = { Text("Descuento unitario") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
            if (selectedDiscount == "general") {
                OutlinedTextField(
                    value = selectedGeneralDiscount,
                    onValueChange = {
                        selectedGeneralDiscount = it
                    },
                    label = { Text("Descuento general") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
            //mensaje error de cantidad
            if (cantidaderror) {
                Text(
                    text = if (cantPro.isEmpty()) "El campo no puede estar vacio"
                    else "Debe de ingresar un numero valido",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            val cantidadNum = cantPro.toLongOrNull() ?: 0L
            val descuentoUnit = selectedUnitDiscount.toDoubleOrNull() ?: 0.0
            val descuentoGeneral = selectedGeneralDiscount.toDoubleOrNull() ?: 0.0

            if(productoSeleccionado != null) {
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
                Log.d("ganar",":$ganancia")
            }
            val totalFormateado = String.format("S/ %.2f", total)
            OutlinedTextField(
                value = totalFormateado,
                onValueChange = {
                    cantidaderror = it.isEmpty() || it.toIntOrNull() == null
                },
                readOnly = true,
                label = { Text("Total") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val cantidad = cantPro.toLong() // convertimos el string en int para guardalo
                    if (productoSeleccionado!= null && nombreProduct.isNotBlank() && cantPro.isNotBlank()) {
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
                        // ✅ limpiar el producto seleccionado
                        ventaViewModel.resetProduct()
                        val id = ventaViewModel.ventaActualId ?: "New"
                        onToDetailVenta(id) // ← regresa a DetailVenta
                    }
                },
                enabled = nombreProduct.isNotBlank() && cantPro.isNotBlank() && !cantidaderror,

                modifier = Modifier.fillMaxWidth()
            ) {
                Text("➕ Agregar producto")
            }
        }

    }
}

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
            val gana=((precio- costo)*cantidad)-desc
            val total = cantidad * (precio - descuentoUnit)
            Triple(desc, total,gana)
        }
        "general" -> {
            val gana=((precio- costo)*cantidad)-descuentoGeneral
            val total = (cantidad * precio) - descuentoGeneral
            Triple(descuentoGeneral, total,gana)
        }
        else -> {
            val gana = (precio-costo)*cantidad
            val total = cantidad * precio
            Triple(0.0, total, gana)
        }
    }
}


