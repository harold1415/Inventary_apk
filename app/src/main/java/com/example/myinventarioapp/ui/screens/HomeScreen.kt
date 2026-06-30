package com.example.myinventarioapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myinventarioapp.ui.theme.BrandBlack
import com.example.myinventarioapp.ui.theme.BrandTextSecondary
import com.example.myinventarioapp.ui.theme.BrandWarmBackground
import com.example.myinventarioapp.ui.theme.BrandWarmWhite
import com.example.myinventarioapp.ui.theme.BrandWoodLight
import com.example.myinventarioapp.ui.theme.BrandWoodMedium
import com.example.myinventarioapp.ui.viewmodel.HomeViewModel
import com.example.myinventarioapp.ui.viewmodel.SucursalMetric
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 🎭 Datos simulados para el top de tipos de prenda (mockup visual)
private val topPrendasMock = listOf(
    "Casacas" to 1.0f,
    "Pantalones" to 0.72f,
    "Camisas" to 0.52f,
    "Polos" to 0.29f,
    "Chompas" to 0.18f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    userRole: String,
    userEmail: String = "",
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val locales by homeViewModel.locales.collectAsState()
    val sucursalSeleccionada by homeViewModel.sucursalSeleccionada.collectAsState()
    val metrics by homeViewModel.metrics.collectAsState()
    val topSucursales by homeViewModel.topSucursales.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val fechaSeleccionada by homeViewModel.fechaSeleccionada.collectAsState()

    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaTexto = remember(fechaSeleccionada) {
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val elegida = sdf.format(fechaSeleccionada)
        if (elegida == hoy) "Hoy" else elegida
    }

    var dropdownExpanded by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .fillMaxHeight(),
                drawerContainerColor = BrandBlack,
                drawerContentColor = BrandWarmWhite
            ) {
                Spacer(Modifier.height(48.dp))
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .padding(start = 24.dp)
                        .background(BrandWoodMedium, shape = RoundedCornerShape(36.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlack
                    )
                }
                Spacer(Modifier.height(16.dp))
                DrawerInfoRow(Icons.Default.Person, "Nombre", userName)
                DrawerInfoRow(Icons.Default.Shield, "Rol", userRole)
                if (userEmail.isNotBlank()) {
                    DrawerInfoRow(Icons.Default.Email, "Correo", userEmail)
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider(color = BrandWoodMedium.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { scope.launch { drawerState.close() }; onLogout() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = BrandWarmBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard", color = BrandWarmWhite) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { if (drawerState.isClosed) drawerState.open() else drawerState.close() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = BrandWarmWhite)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 👤 Saludo
                Column {
                    Text(
                        "Hola, $userName",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrandBlack
                    )
                    Text("Rol: $userRole", style = MaterialTheme.typography.bodyMedium, color = BrandTextSecondary)
                }

                // 🔽 Filtros: sucursal y fecha en la misma fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selector de sucursal
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = when (sucursalSeleccionada) {
                                "" -> "Todas"
                                else -> sucursalSeleccionada ?: "Todas"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sucursal", fontSize = 11.sp) },
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlack,
                                unfocusedBorderColor = BrandWoodMedium,
                                focusedContainerColor = BrandWarmWhite,
                                unfocusedContainerColor = BrandWarmWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas") },
                                onClick = { homeViewModel.seleccionarSucursal(""); dropdownExpanded = false }
                            )
                            locales.forEach { local ->
                                DropdownMenuItem(
                                    text = { Text(local.nombre) },
                                    onClick = { homeViewModel.seleccionarSucursal(local.nombre); dropdownExpanded = false }
                                )
                            }
                        }
                    }

                    // Selector de fecha
                    OutlinedTextField(
                        value = fechaTexto,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha", fontSize = 11.sp) },
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                val cal = Calendar.getInstance()
                                cal.time = fechaSeleccionada
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val nueva = Calendar.getInstance()
                                        nueva.set(y, m, d)
                                        homeViewModel.seleccionarFecha(nueva.time)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Elegir fecha", tint = BrandWoodMedium)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlack,
                            unfocusedBorderColor = BrandWoodMedium,
                            focusedContainerColor = BrandWarmWhite,
                            unfocusedContainerColor = BrandWarmWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 📊 Sección resumen
                SectionTitle("Resumen")

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandBlack)
                    }
                } else {
                    // Grid 2x2 de métricas
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            titulo = "Ventas totales",
                            valor = "S/ ${"%.2f".format(metrics.ventasTotales)}",
                            subtitulo = "${metrics.cantidadVentas} ventas"
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            titulo = "Ganancia",
                            valor = "S/ ${"%.2f".format(metrics.gananciaTotal)}",
                            subtitulo = if (metrics.ventasTotales > 0)
                                "margen ${"%.0f".format((metrics.gananciaTotal / metrics.ventasTotales) * 100)}%"
                            else null
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            titulo = "Ticket promedio",
                            valor = "S/ ${"%.2f".format(metrics.ticketPromedio)}",
                            subtitulo = null
                        )
                        MetricCard(
                            modifier = Modifier.weight(1f),
                            titulo = "Unidades vendidas",
                            valor = "${metrics.unidadesVendidas}",
                            subtitulo = null
                        )
                    }
                }

                // 🏪 Top sucursales (datos reales)
                if (topSucursales.isNotEmpty() && !isLoading) {
                    SectionTitle("Top sucursales")
                    RankingCard {
                        topSucursales.forEachIndexed { index, suc ->
                            RankingRow(
                                posicion = index + 1,
                                nombre = suc.nombre,
                                valor = "S/ ${"%.2f".format(suc.total)}",
                                porcentaje = suc.porcentaje,
                                color = when (index) {
                                    0 -> BrandBlack
                                    1 -> BrandWoodMedium
                                    else -> BrandWoodLight
                                }
                            )
                            if (index < topSucursales.lastIndex) {
                                HorizontalDivider(color = BrandWoodLight.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))
                            }
                        }
                    }
                }

                // 🎭 Top tipos de prenda (mockup visual — datos simulados)
                SectionTitle("Top tipos de prenda")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        // Badge que avisa que son datos de ejemplo
                        Surface(
                            color = BrandWoodLight.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Text(
                                "Próximamente con datos reales",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        topPrendasMock.forEachIndexed { index, (nombre, pct) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandTextSecondary,
                                    modifier = Modifier.width(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nombre, style = MaterialTheme.typography.bodySmall, color = BrandBlack)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .background(BrandWoodLight, RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(pct)
                                                .height(5.dp)
                                                .background(BrandBlack, RoundedCornerShape(4.dp))
                                        )
                                    }
                                }
                            }
                            if (index < topPrendasMock.lastIndex) {
                                HorizontalDivider(color = BrandWoodLight.copy(alpha = 0.5f))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Componentes reutilizables ───────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = BrandBlack
    )
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    subtitulo: String?
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = BrandTextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandBlack)
            subtitulo?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = MaterialTheme.typography.labelSmall, color = BrandWoodMedium)
            }
        }
    }
}

@Composable
private fun RankingCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWarmWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), content = content)
    }
}

@Composable
private fun RankingRow(
    posicion: Int,
    nombre: String,
    valor: String,
    porcentaje: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("$posicion", style = MaterialTheme.typography.labelMedium, color = BrandTextSecondary, modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre, style = MaterialTheme.typography.bodySmall, color = BrandBlack, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(BrandWoodLight, RoundedCornerShape(4.dp))) {
                Box(modifier = Modifier.fillMaxWidth(porcentaje).height(5.dp).background(color, RoundedCornerShape(4.dp)))
            }
        }
        Text(valor, style = MaterialTheme.typography.labelMedium, color = BrandBlack, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DrawerInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = BrandWoodMedium, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = BrandTextSecondary.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = BrandWarmWhite)
        }
    }
}