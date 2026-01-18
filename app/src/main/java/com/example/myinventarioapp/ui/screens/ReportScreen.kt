package com.example.myinventarioapp.ui.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myinventarioapp.ui.viewmodel.ReportViewModel
import java.io.File
import kotlin.text.ifEmpty


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = viewModel(),
    navController: NavController //  recibido desde el NavHost
) {
    val locales by viewModel.locales.collectAsState() // recuperamos los datos del view model que nos da la BD
    val ventas by viewModel.ventas.collectAsState()
    var filtredLocal by remember { mutableStateOf(false) }
    var selectedLocal by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current //para quitar el foco de los componentes
    val context = LocalContext.current
    val excelGeneratedPath by viewModel.excelGenerated.collectAsState()   // ⭐ AGREGADO
    val reportTypes = listOf(
        "Ventas del día",
        "Ventas de ayer",
        "Últimos 7 días",
        "Última semana",
        "Productos más vendidos",
        "Productos menos vendidos",
        "Stock bajo",
        "Ranking de vendedores"
    )

    var selectedReport by remember { mutableStateOf(reportTypes.first()) }
    var reportMenuExpanded by remember { mutableStateOf(false) }

    // ⭐ Cuando el ViewModel avisa que el archivo está listo → lo abrimos
    LaunchedEffect(excelGeneratedPath) {
        excelGeneratedPath?.let { path ->
            openExcelFile(context, path)     // ⭐ ABRIR EXCEL
            viewModel.resetExcelGenerated()  // ⭐ LIMPIAR PARA NO REABRIR
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reporte",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
    ) { padding ->
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
            //Sucursales
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
                        .fillMaxWidth()
                        .menuAnchor(
                            type = MenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                )

                DropdownMenu(
                    expanded = filtredLocal,
                    onDismissRequest = { filtredLocal = false }
                ) {
                    // Opción para mostrar todos los locales
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
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = reportMenuExpanded,
                onExpandedChange = { reportMenuExpanded = !reportMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedReport,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar reporte") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = reportMenuExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                DropdownMenu(
                    expanded = reportMenuExpanded,
                    onDismissRequest = { reportMenuExpanded = false }
                ) {
                    reportTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedReport = type
                                reportMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Ventas")
            Row(
                modifier=Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 🔹 Espaciado entre botones
            ){
                Button(
                    onClick = {viewModel.getReportToday(selectedLocal)}
                ) {
                    Text("Hoy")
                }
                Button(
                    onClick = {viewModel.getReportYesterday(selectedLocal)}
                ) {
                    Text("Ayer")
                }
                Button(
                    onClick = {viewModel.getReportLast7Days(selectedLocal)}
                ) {
                    Text("Hace 7 días")
                }
                Button(
                    onClick = {viewModel.getReportLastWeek(selectedLocal)}
                ) {
                    Text("Hace una semana")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // ACCORDION DE REPORTES
            ReportAccordion(
                title = "Inventario",
                options = listOf("Todos los sucursales") + locales.map { "Sucursal ${it.nombre}" }
            ) { option ->

                val localSeleccionado =
                    if (option == "Todos los sucursales") ""
                    else option.removePrefix("Sucursal ")

                // ⭐ AQUÍ SE GENERA EL EXCEL
                viewModel.exportInventoryToExcel(context, localSeleccionado)

                Toast.makeText(context, "Generando Excel...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
@Composable
fun ReportAccordion(
    title: String,
    options: List<String>,
    onOptionClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Título del accordion
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .pointerInput(Unit) {
                    detectTapGestures { expanded = !expanded }
                },
            horizontalArrangement = Arrangement.Start
        ){
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            // Flecha animada
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .rotate(if (expanded) 180f else 0f)  // animación de rotación
            )
        }

        // Opciones internas
        if (expanded) {
            Column(Modifier.padding(start = 16.dp)) {
                options.forEach { option ->
                    Button(
                        onClick = { onOptionClick(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}

fun openExcelFile(context: Context, filePath: String) {
    val file = File(filePath)

    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No hay apps para abrir Excel", Toast.LENGTH_LONG).show()
    }
}
